package com.firzzle.llm.dto;

import com.firzzle.common.request.PageRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @Class Name : SnapReviewSearchDTO.java
 * @Description : 스냅리뷰 검색 및 페이징 요청 DTO
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Getter
@Setter
@Schema(description = "스냅리뷰 검색 및 페이징 요청 정보")
public class SnapReviewSearchDTO extends PageRequestDTO {

    @Schema(description = "검색 키워드", example = "프로그래밍")
    private String keyword = "";

    @Schema(description = "카테고리", example = "교육/강의")
    private String category = "";

    /**
     * 기본 생성자
     */
    public SnapReviewSearchDTO() {
        super();  // 상위 클래스의 기본값 사용 (p_pageno=1, p_pagesize=10)
    }

    /**
     * 페이지 정보 설정 생성자
     *
     * @param p_pageno 페이지 번호
     * @param p_pagesize 페이지 크기
     */
    public SnapReviewSearchDTO(int p_pageno, int p_pagesize) {
        super(p_pageno, p_pagesize);
    }

    /**
     * 페이지 정보 및 정렬 설정 생성자
     *
     * @param p_pageno 페이지 번호
     * @param p_pagesize 페이지 크기
     * @param p_order 정렬 기준
     * @param p_sortorder 정렬 방향
     */
    public SnapReviewSearchDTO(int p_pageno, int p_pagesize, String p_order, String p_sortorder) {
        super(p_pageno, p_pagesize, p_order, p_sortorder);
    }
}