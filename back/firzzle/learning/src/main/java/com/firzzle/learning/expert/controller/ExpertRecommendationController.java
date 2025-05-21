package com.firzzle.learning.expert.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.dto.ExpertRecommendationResponseDTO2;
import com.firzzle.learning.expert.service.ContentTagService;
import com.firzzle.learning.expert.service.ExpertRecommendationApiService;
import com.firzzle.learning.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @Class Name : ExpertRecommendationController.java
 * @Description : 콘텐츠 기반 전문가 추천 API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
@Tag(name = "전문가 추천 API (LinkedIn)", description = "콘텐츠 관련 LinkedIn 전문가 추천 API")
public class ExpertRecommendationController {

    private final ContentService contentService;
    private final ContentTagService contentTagService;
    private final ExpertRecommendationApiService recommendationService;

    /**
     * 콘텐츠 관련 LinkedIn 전문가 추천
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    @GetMapping("/{contentSeq}/expert-recommendations")
    @Operation(summary = "LinkedIn 전문가 추천", description = "콘텐츠 태그 기반으로 유사한 LinkedIn 전문가를 추천합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전문가 추천 성공"),
            @ApiResponse(responseCode = "204", description = "추천 전문가 없음"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<ExpertRecommendationResponseDTO2>> getRecommendedLinkedInExperts(
            @Parameter(description = "콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(name = "p_pageno", defaultValue = "1") Integer page,
            @Parameter(description = "페이지 크기", example = "3") @RequestParam(name = "p_pagesize", defaultValue = "3") Integer pageSize,
            @Parameter(description = "최소 유사도 점수 (0-1)", example = "0.3") @RequestParam(defaultValue = "0.3") Float minScore,
            HttpServletRequest request) {

        log.info("LinkedIn 전문가 추천 요청 - 콘텐츠: {}, 페이지: {}/{}, 최소 유사도: {}",
                userContentSeq, page, pageSize, minScore);

        try {
            RequestBox box = RequestManager.getBox(request);
            String uuid = box.getString("uuid");

            // 1. 콘텐츠 존재 여부 확인
            RequestBox contentBox = new RequestBox("contentBox");
            contentBox.put("userContentSeq", userContentSeq);
            contentBox.put("uuid", uuid);
            DataBox contentData = contentService.selectContentByUserContentSeq(contentBox);

            if (contentData == null) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "콘텐츠를 찾을 수 없습니다: " + userContentSeq);
            }

            Long contentSeq = contentData.getLong2("d_content_seq");

            // 2. 콘텐츠 태그 조회
            String tags = contentTagService.getContentTagsAsString(contentSeq);

            if (tags == null || tags.isEmpty()) {
                log.warn("콘텐츠에 태그가 없습니다: {}", contentSeq);
                return ResponseEntity.noContent().build();
            }

            // 3. LinkedIn 전문가 추천 요청 - API 명세에 맞는 응답 형식 사용
            ExpertRecommendationResponseDTO2 responseDTO = recommendationService.getRecommendedLinkedInExpertsForAPI(
                    contentSeq, tags, page, pageSize, minScore);

            // 4. 응답
            if (responseDTO == null || responseDTO.getContent().isEmpty()) {
                log.info("추천할 LinkedIn 전문가가 없습니다: 콘텐츠={}, 태그={}", contentSeq, tags);
                return ResponseEntity.noContent().build();
            }

            Response<ExpertRecommendationResponseDTO2> response = Response.<ExpertRecommendationResponseDTO2>builder()
                    .status(Status.OK)
                    .data(responseDTO)
                    .build();

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            log.error("LinkedIn 전문가 추천 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("LinkedIn 전문가 추천 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 전문가 추천 중 오류가 발생했습니다.");
        }
    }
}