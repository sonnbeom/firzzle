package com.firzzle.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "페이지 요청 정보")
@Getter
@Setter
public class PageRequestDTO {

	@Schema(description = "페이지 번호 (1부터 시작)", example = "1", defaultValue = "1", minimum = "1")
	@Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
	private int p_pageno = 1;

	@Schema(description = "페이지 크기", example = "10", defaultValue = "10", minimum = "1", maximum = "100")
	@Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
	@Max(value = 100, message = "페이지 크기는 100을 초과할 수 없습니다")
	private int p_pagesize = 10;

	@Schema(description = "정렬 기준 필드명", example = "indate", pattern = "^[a-zA-Z0-9_]*$")
	@Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "정렬 기준은 영문, 숫자, 언더스코어만 허용됩니다")
	private String p_order;

	@Schema(description = "정렬 방향", example = "DESC", allowableValues = { "ASC", "DESC" }, defaultValue = "DESC")
	@Pattern(regexp = "^(ASC|DESC)$", message = "정렬 방향은 ASC 또는 DESC만 가능합니다")
	private String p_sortorder = "DESC";

	public PageRequestDTO() {
	}

	public PageRequestDTO(int p_pageno, int p_pagesize) {
		this.p_pageno = p_pageno;
		this.p_pagesize = p_pagesize;
	}

	public PageRequestDTO(int p_pageno, int p_pagesize, String p_order, String p_sortorder) {
		this.p_pageno = p_pageno;
		this.p_pagesize = p_pagesize;
		this.p_order = p_order;
		this.p_sortorder = p_sortorder;
	}

	/**
	 * MyBatis에서 사용할 오프셋 값을 계산합니다.
	 * @return 현재 페이지의 오프셋
	 */
	@Schema(hidden = true)
	public int getP_startNumber() {
		return (p_pageno - 1) * p_pagesize;
	}

	/**
	 * MyBatis에서 사용할 LIMIT 값을 반환합니다.
	 * @return 가져올 레코드 수
	 */
	@Schema(hidden = true)
	public int getP_limitNumber() {
		return p_pagesize;
	}

	/**
	 * SQL ORDER BY 절 문자열을 생성합니다.
	 * @return ORDER BY 절 문자열
	 */
	@Schema(hidden = true)
	public String getOrderByClause() {
		if (p_order != null && !p_order.isEmpty()) {
			return String.format("ORDER BY %s %s", p_order, p_sortorder);
		}
		return "";
	}

	/**
	 * 다음 페이지의 요청 DTO를 생성합니다.
	 * @return 다음 페이지 요청 DTO
	 */
	@Schema(hidden = true)
	public PageRequestDTO next() {
		return new PageRequestDTO(p_pageno + 1, p_pagesize, p_order, p_sortorder);
	}

	/**
	 * 이전 페이지의 요청 DTO를 생성합니다.
	 * @return 이전 페이지 요청 DTO
	 */
	@Schema(hidden = true)
	public PageRequestDTO previous() {
		return new PageRequestDTO(Math.max(1, p_pageno - 1), p_pagesize, p_order, p_sortorder);
	}

	/**
	 * 첫 페이지의 요청 DTO를 생성합니다.
	 * @return 첫 페이지 요청 DTO
	 */
	@Schema(hidden = true)
	public PageRequestDTO first() {
		return new PageRequestDTO(1, p_pagesize, p_order, p_sortorder);
	}
}