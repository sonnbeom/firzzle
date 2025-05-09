package com.firzzle.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageResponseDTO<T> {
    // 컨텐츠
    private List<T> content;

    // 페이지 정보 (일부는 기존 변수명 유지)
    private int p_pageno;        // 현재 페이지 번호
    private int p_pagesize;      // 페이지 크기
    private long totalElements;  // 전체 요소 수
    private int totalPages;      // 총 페이지 수

    // 페이지 상태
    private boolean last;        // 마지막 페이지 여부
    private boolean hasNext;     // 다음 페이지 존재 여부

    @Builder
    public PageResponseDTO(List<T> content, int p_pageno, int p_pagesize, long totalElements) {
        this.content = content;
        this.p_pageno = p_pageno;
        this.p_pagesize = p_pagesize;
        this.totalElements = totalElements;

        // 총 페이지 수 계산
        this.totalPages = totalElements == 0 ? 1 : (int) Math.ceil((double) totalElements / p_pagesize);

        // 페이지 상태 계산 (1-based)
        this.last = p_pageno >= totalPages;
        this.hasNext = p_pageno < totalPages;
    }
}