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

	@Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0", minimum = "0")
	@Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
	private int pageNumber = 0;

	@Schema(description = "페이지 크기", example = "10", defaultValue = "10", minimum = "1", maximum = "100")
	@Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
	@Max(value = 100, message = "페이지 크기는 100을 초과할 수 없습니다")
	private int pageSize = 10;

	@Schema(description = "정렬 기준 필드명", example = "createdAt", pattern = "^[a-zA-Z0-9_]*$")
	@Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "정렬 기준은 영문, 숫자, 언더스코어만 허용됩니다")
	private String sortBy;

	@Schema(description = "정렬 방향", example = "DESC", allowableValues = { "ASC", "DESC" }, defaultValue = "DESC")
	@Pattern(regexp = "^(ASC|DESC)$", message = "정렬 방향은 ASC 또는 DESC만 가능합니다")
	private String sortDirection = "DESC";

	public PageRequestDTO() {
	}

	public PageRequestDTO(int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
	}

	public PageRequestDTO(int pageNumber, int pageSize, String sortBy, String sortDirection) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.sortBy = sortBy;
		this.sortDirection = sortDirection;
	}

	/**
	 * MyBatis에서 사용할 오프셋 값을 계산합니다.
	 * @return 현재 페이지의 오프셋
	 */
	@Schema(hidden = true)
	public int getOffset() {
		return pageNumber * pageSize;
	}

	/**
	 * MyBatis에서 사용할 LIMIT 값을 반환합니다.
	 * @return 가져올 레코드 수
	 */
	@Schema(hidden = true)
	public int getLimit() {
		return pageSize;
	}

	/**
	 * SQL ORDER BY 절 문자열을 생성합니다.
	 * @return ORDER BY 절 문자열
	 */
	@Schema(hidden = true)
	public String getOrderByClause() {
		if (sortBy != null && !sortBy.isEmpty()) {
			return String.format("ORDER BY %s %s", sortBy, sortDirection);
		}
		return "";
	}

	/**
	 * 다음 페이지의 요청 DTO를 생성합니다.
	 * @return 다음 페이지 요청 DTO
	 */
	@Schema(hidden = true)
	public PageRequestDTO next() {
		return new PageRequestDTO(pageNumber + 1, pageSize, sortBy, sortDirection);
	}

	/**
	 * 이전 페이지의 요청 DTO를 생성합니다.
	 * @return 이전 페이지 요청 DTO
	 */
	@Schema(hidden = true)
	public PageRequestDTO previous() {
		return new PageRequestDTO(Math.max(0, pageNumber - 1), pageSize, sortBy, sortDirection);
	}

	/**
	 * 첫 페이지의 요청 DTO를 생성합니다.
	 * @return 첫 페이지 요청 DTO
	 */
	@Schema(hidden = true)
	public PageRequestDTO first() {
		return new PageRequestDTO(0, pageSize, sortBy, sortDirection);
	}
}