package com.firzzle.learning.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.dto.ContentResponseDTO;
import com.firzzle.learning.service.ContentRecommendationService;
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

    /**
     * 콘텐츠 추천 조회
     */
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    @GetMapping(value = "/{contentSeq}/recommendations", produces = "application/json;charset=UTF-8")
    @Operation(summary = "콘텐츠 추천 조회", description = "현재 콘텐츠와 관련된 추천 콘텐츠 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 콘텐츠 조회 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<List<ContentResponseDTO>>> getContentRecommendations(
            @Parameter(description = "조회할 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            HttpServletRequest request) {

        logger.info("콘텐츠 추천 조회 요청 - 유저 콘텐츠 일련번호: {}", userContentSeq);

        try {
            // RequestBox로 변환
            RequestBox box = RequestManager.getBox(request);
            logger.debug(box.toString());
            box.put("userContentSeq", userContentSeq);

            // 서비스 호출
            List<DataBox> recommendationDataBoxes = recommendationService.getRecommendations(box);

            // DataBox 목록을 DTO 목록으로 변환
            List<ContentResponseDTO> recommendations = new ArrayList<>();
            for (DataBox dataBox : recommendationDataBoxes) {
                recommendations.add(convertToContentResponseDTO(dataBox));
            }

            Response<List<ContentResponseDTO>> response = Response.<List<ContentResponseDTO>>builder()
                    .status(Status.OK)
                    .data(recommendations)
                    .build();

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