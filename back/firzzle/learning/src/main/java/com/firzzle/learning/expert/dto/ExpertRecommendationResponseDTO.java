package com.firzzle.learning.expert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Class Name : ExpertRecommendationResponseDTO.java
 * @Description : 전문가 추천 응답 DTO
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전문가 추천 응답 DTO")
public class ExpertRecommendationResponseDTO {

    @Schema(description = "콘텐츠 일련번호")
    private Long contentSeq;

    @Schema(description = "콘텐츠 태그")
    private String tags;

    @Schema(description = "추천 전문가 목록")
    @JsonProperty("profiles")
    private List<LinkedInExpertDTO> experts;

    @Schema(description = "현재 페이지")
    private Integer page;

    @Schema(description = "페이지 크기")
    private Integer pageSize;

    @Schema(description = "전체 항목 수")
    private Integer totalElements;

    @Schema(description = "전체 페이지 수")
    private Integer totalPages;

    @Schema(description = "마지막 페이지 여부")
    private Boolean last;

    @Schema(description = "다음 페이지 존재 여부")
    private Boolean hasNext;
}