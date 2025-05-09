package com.firzzle.learning.dto;

import com.firzzle.common.response.PageResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 전문가 추천 응답 DTO
 */
@Getter
@Setter
@Schema(description = "전문가 추천 응답 정보")
public class ExpertRecommendationResponseDTO extends PageResponseDTO<ExpertResponseDTO> {

    @Schema(description = "오리지널 컨텐츠 태그")
    private String originTags;

    @Builder(builderMethodName = "recommendationBuilder")
    public ExpertRecommendationResponseDTO(List<ExpertResponseDTO> content, Integer p_pageno, Integer p_pagesize, Integer totalElements, String originTags) {
        super(content, p_pageno, p_pagesize, totalElements);
        this.originTags = originTags;
    }
}