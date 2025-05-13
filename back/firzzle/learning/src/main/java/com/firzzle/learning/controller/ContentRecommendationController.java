package com.firzzle.learning.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.logging.dto.UserActionLog;
import com.firzzle.common.logging.service.LoggingService;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.dto.ContentRecommendationResponseDTO;
import com.firzzle.learning.dto.ContentRecommendationSearchDTO;
import com.firzzle.learning.dto.ContentResponseDTO;
import com.firzzle.learning.service.ContentRecommendationService;
import com.firzzle.learning.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.firzzle.common.logging.dto.UserActionLog.userPreferenceLog;
import static com.firzzle.common.logging.service.LoggingService.*;

/**
 * @Class Name : ContentRecommendationController.java
 * @Description : 콘텐츠 추천 API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 3.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
@Tag(name = "콘텐츠 추천 API (AI)", description = "콘텐츠 추천 관련 API")
public class ContentRecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(ContentRecommendationController.class);

    private final ContentRecommendationService recommendationService;
    private final ContentService contentService;

    /**
     * 콘텐츠 추천 조회
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    @GetMapping(value = "/{contentSeq}/recommendations", produces = "application/json;charset=UTF-8")
    @Operation(summary = "콘텐츠 추천 조회", description = "현재 콘텐츠와 관련된 추천 콘텐츠 목록을 페이지네이션하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 콘텐츠 조회 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<ContentRecommendationResponseDTO<ContentResponseDTO>>> getContentRecommendations(
            @Parameter(description = "조회할 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            @Parameter(description = "검색 및 페이지 요청 정보") ContentRecommendationSearchDTO searchDTO,
            HttpServletRequest request) {

        logger.info("콘텐츠 추천 조회 요청 - 유저 콘텐츠 일련번호: {}, 페이지: {}, 사이즈: {}",
                userContentSeq, searchDTO.getP_pageno(), searchDTO.getP_pagesize());

        try {
            // RequestBox로 변환
            RequestBox box = RequestManager.getBox(request);
            box.put("userContentSeq", userContentSeq);

            // 원본 콘텐츠 태그 조회
            DataBox originContent = contentService.selectContent(box);
            String originTags = originContent.getString("d_tags");

            // 태그를 쉼표 + 공백으로 구분하고 최대 3개만 유지
            String formattedTags = formatTags(originTags, 3);

            // 추천 콘텐츠 목록 조회
            List<DataBox> recommendationDataBoxes = recommendationService.getRecommendations(box);

            // 총 추천 콘텐츠 수 조회
            int totalCount = recommendationService.getRecommendationsCount(box);

            // DataBox 목록을 DTO 목록으로 변환
            List<ContentResponseDTO> recommendations = new ArrayList<>();
            for (DataBox dataBox : recommendationDataBoxes) {
                recommendations.add(convertToContentResponseDTO(dataBox));
            }

            // ContentRecommendationResponseDTO 생성
            ContentRecommendationResponseDTO<ContentResponseDTO> responseDTO = ContentRecommendationResponseDTO
                    .<ContentResponseDTO>recommendationBuilder()
                    .content(recommendations)
                    .p_pageno(searchDTO.getP_pageno())
                    .p_pagesize(searchDTO.getP_pagesize())
                    .totalElements(totalCount)
                    .originTags(formattedTags)
                    .build();

            Response<ContentRecommendationResponseDTO<ContentResponseDTO>> response = Response
                    .<ContentRecommendationResponseDTO<ContentResponseDTO>>builder()
                    .status(Status.OK)
                    .data(responseDTO)
                    .build();

            //컨텐츠 추천 로깅 => ELK
            String referer = box.getString("referer");
            String userId = box.getString("uuid");
            log(userPreferenceLog(userId, referer.toUpperCase(), "RECOMMEND"));

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("콘텐츠 추천 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠 추천 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 추천 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 태그 문자열을 포맷팅하고 최대 개수 제한
     * @param tagString 원본 태그 문자열 (쉼표로 구분)
     * @param maxCount 최대 태그 개수
     * @return 포맷팅된 태그 문자열 (쉼표 + 공백으로 구분)
     */
    private String formatTags(String tagString, int maxCount) {
        if (tagString == null || tagString.isEmpty()) {
            return "";
        }

        String[] tags = tagString.split(",");
        String result = Arrays.stream(tags)
                .limit(maxCount)
                .collect(Collectors.joining(", "));

        return result;
    }

    /**
     * DataBox를 ContentResponseDTO로 변환
     */
    private ContentResponseDTO convertToContentResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            return null;
        }

        return ContentResponseDTO.builder()
                .contentSeq(dataBox.getLong2("d_content_seq"))
                .title(dataBox.getString("d_title"))
                .description(dataBox.getString("d_description"))
                .contentType(dataBox.getString("d_category"))
                .videoId(dataBox.getString("d_video_id"))
                .url(dataBox.getString("d_url"))
                .thumbnailUrl(dataBox.getString("d_thumbnail_url"))
                .duration(dataBox.getInt2("d_duration"))
                .processStatus(dataBox.getString("d_process_status"))
                .tags(dataBox.getString("d_tags"))
                .indate(formatDateTime(dataBox.getString("d_indate")))
                .completedAt(formatDateTime(dataBox.getString("d_completed_at")))
                .deleteYn(dataBox.getString("d_delete_yn"))
                .build();
    }

    /**
     * YYYYMMDDHHMMSS 형식의 날짜 문자열을 포맷된 문자열로 변환
     */
    private String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        try {
            return FormatDate.getFormatDate(dateTimeStr, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            logger.error("날짜 변환 중 오류 발생: {}", e.getMessage());
            return dateTimeStr;
        }
    }
}