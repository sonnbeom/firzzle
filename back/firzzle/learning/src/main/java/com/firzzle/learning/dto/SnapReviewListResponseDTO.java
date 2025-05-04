package com.firzzle.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : SnapReviewListResponseDTO.java
 * @Description : 스냅리뷰 목록 응답 DTO
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "스냅리뷰 목록 응답 정보")
public class SnapReviewListResponseDTO {

    @Schema(description = "콘텐츠 일련번호")
    private Long contentSeq;

    @Schema(description = "콘텐츠 제목")
    private String contentTitle;

    @Schema(description = "콘텐츠 카테고리")
    private String category;

    @Schema(description = "썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "대표 프레임 이미지 URL")
    private String representativeImageUrl;

    @Schema(description = "생성 일시")
    private String indate;

    @Schema(description = "포맷된 생성 일시 (YYYY-MM-DD)")
    private String formattedIndate;

    @Schema(description = "프레임 수")
    private Integer frameCount;
}