package com.firzzle.learning.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.dto.ExpertResponseDTO;
import com.firzzle.learning.service.ExpertRecommendationService;
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
import java.util.List;

/**
 * @Class Name : ExpertRecommendationController.java
 * @Description : 전문가 추천 API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 3.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
@Tag(name = "전문가 추천 API (AI)", description = "콘텐츠 관련 전문가 추천 API")
public class ExpertRecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(ExpertRecommendationController.class);

    private final ExpertRecommendationService expertService;

    /**
     * 추천 전문가 조회
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    @GetMapping(value = "/{contentSeq}/expert-recommendations", produces = "application/json;charset=UTF-8")
    @Operation(summary = "추천 전문가 조회", description = "콘텐츠와 관련된 전문가 추천 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 전문가 조회 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<List<ExpertResponseDTO>>> getExpertRecommendations(
            @Parameter(description = "조회할 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            HttpServletRequest request) {

        logger.info("추천 전문가 조회 요청 - 콘텐츠 일련번호: {}", userContentSeq);

        try {
            // RequestBox로 변환
            RequestBox box = RequestManager.getBox(request);
            box.put("contentSeq", userContentSeq);

            // 서비스 호출
            List<DataBox> expertDataBoxes = expertService.getRecommendedExperts(box);

            // DataBox 목록이 없는 경우 204 No Content 반환
            if (expertDataBoxes.size()==0) {
                logger.debug("전문가 추천 조회 {}건", expertDataBoxes.size());
                return ResponseEntity.noContent().build();
            }

            // DataBox 목록을 DTO 목록으로 변환
            List<ExpertResponseDTO> experts = new ArrayList<>();
            for (DataBox expertDataBox : expertDataBoxes) {
                // 전문 분야 목록 추출
                @SuppressWarnings("unchecked")
                List<DataBox> expertiseDataBoxes = (List<DataBox>) expertDataBox.getObject("expertises");
                List<String> expertises = new ArrayList<>();

                if (expertiseDataBoxes != null) {
                    for (DataBox expertiseBox : expertiseDataBoxes) {
                        expertises.add(expertiseBox.getString("d_expertise"));
                    }
                }

                experts.add(ExpertResponseDTO.builder()
                        .expertSeq(expertDataBox.getLong2("d_expert_seq"))
                        .name(expertDataBox.getString("d_name"))
                        .title(expertDataBox.getString("d_title"))
                        .company(expertDataBox.getString("d_company"))
                        .profileImageUrl(expertDataBox.getString("d_profile_image_url"))
                        .linkedinUrl(expertDataBox.getString("d_linkedin_url"))
                        .relevance(Float.parseFloat(expertDataBox.getString("d_relevance")))
                        .expertise(expertises)
                        .build());
            }

            Response<List<ExpertResponseDTO>> response = Response.<List<ExpertResponseDTO>>builder()
                    .status(Status.OK)
                    .data(experts)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("추천 전문가 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("추천 전문가 조회 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "추천 전문가 조회 중 오류가 발생했습니다.");
        }
    }
}