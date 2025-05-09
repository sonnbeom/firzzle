package com.firzzle.llm.dto;

import com.firzzle.common.request.PageRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "콘텐츠 검색 및 페이징 요청 정보")
public class ContentSearchDTO extends PageRequestDTO {

    @Schema(description = "검색 키워드", example = "영어 학습")
    private String keyword;

    @Schema(description = "카테고리", example = "EDUCATION")
    private String category;

    @Schema(description = "처리 상태 (Q: 대기중, P: 분석중, C: 완료, F: 실패)", example = "C")
    private String status;

    public ContentSearchDTO() {
        super();  // 상위 클래스의 기본값 사용 (p_pageno=1, p_pagesize=10)
    }

    public ContentSearchDTO(int p_pageno, int p_pagesize) {
        super(p_pageno, p_pagesize);
    }

    public ContentSearchDTO(int p_pageno, int p_pagesize, String p_order, String p_sortorder) {
        super(p_pageno, p_pagesize, p_order, p_sortorder);
    }
}