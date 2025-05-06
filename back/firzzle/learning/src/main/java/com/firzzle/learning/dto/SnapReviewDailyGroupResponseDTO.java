package com.firzzle.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Class Name : SnapReviewDailyGroupResponseDTO.java
 * @Description : 일별 그룹화된 스냅리뷰 목록 응답 DTO
 * @author Firzzle
 * @since 2025. 5. 05.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "일별 그룹화된 스냅리뷰 목록 응답 정보")
public class SnapReviewDailyGroupResponseDTO {

    @Schema(description = "일별 스냅리뷰 목록 (key: 날짜(YYYY-MM-DD), value: 해당 날짜의 스냅리뷰 목록)")
    private Map<String, List<SnapReviewListResponseDTO>> dailySnapReviews;

    @Schema(description = "총 날짜 수")
    private int totalDays;
}