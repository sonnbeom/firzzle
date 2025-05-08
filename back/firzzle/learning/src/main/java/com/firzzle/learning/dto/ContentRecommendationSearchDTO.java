package com.firzzle.learning.dto;

import com.firzzle.common.request.PageRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "콘텐츠 추천 검색 및 페이징 요청 정보")
public class ContentRecommendationSearchDTO extends PageRequestDTO {

    @Schema(description = "검색 키워드", example = "영어 학습")
    private String keyword;

    @Schema(description = "카테고리", example = "EDUCATION")
    private String category;

    @Schema(description = "처리 상태 (Q: 대기중, P: 분석중, C: 완료, F: 실패)", example = "C")
    private String status;

    public ContentRecommendationSearchDTO() {
        super();
        this.setP_pagesize(6); // 기본 페이지 크기를 6으로 설정
    }

    public ContentRecommendationSearchDTO(int p_pageno, int p_pagesize) {
        super(p_pageno, p_pagesize);
    }

    public ContentRecommendationSearchDTO(int p_pageno, int p_pagesize, String p_order, String p_sortorder) {
        super(p_pageno, p_pagesize, p_order, p_sortorder);
    }

    // 기존 PageRequestDTO의 메소드들을 상속받지만
    // 기본 페이지 크기가 6인 새 인스턴스를 생성하는 메소드들을 오버라이드

    @Schema(hidden = true)
    @Override
    public PageRequestDTO next() {
        return new ContentRecommendationSearchDTO(getP_pageno() + 1, getP_pagesize(), getP_order(), getP_sortorder());
    }

    @Schema(hidden = true)
    @Override
    public PageRequestDTO previous() {
        return new ContentRecommendationSearchDTO(Math.max(1, getP_pageno() - 1), getP_pagesize(), getP_order(), getP_sortorder());
    }

    @Schema(hidden = true)
    @Override
    public PageRequestDTO first() {
        return new ContentRecommendationSearchDTO(1, getP_pagesize(), getP_order(), getP_sortorder());
    }
}