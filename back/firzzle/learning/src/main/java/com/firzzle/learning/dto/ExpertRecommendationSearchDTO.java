package com.firzzle.learning.dto;

import com.firzzle.common.request.PageRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 전문가 추천 검색 및 페이징 요청 정보
 */
@Getter
@Setter
@Schema(description = "전문가 추천 검색 및 페이징 요청 정보")
public class ExpertRecommendationSearchDTO extends PageRequestDTO {

    @Schema(description = "전문 분야 필터")
    private String expertise;

    @Schema(description = "회사 필터")
    private String company;

    public ExpertRecommendationSearchDTO() {
        super();
        // 기본 정렬 기준 설정
        this.setP_order("relevance");
        this.setP_sortorder("DESC");
        this.setP_pagesize(3); // 기본 페이지 사이즈 변경
    }

    public ExpertRecommendationSearchDTO(int p_pageno, int p_pagesize) {
        super(p_pageno, p_pagesize);
        this.setP_order("relevance");
        this.setP_sortorder("DESC");
    }

    public ExpertRecommendationSearchDTO(int p_pageno, int p_pagesize, String p_order, String p_sortorder) {
        super(p_pageno, p_pagesize, p_order, p_sortorder);
    }
}