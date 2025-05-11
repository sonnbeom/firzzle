package com.firzzle.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // 기본 생성자만 자동 생성

/**
 * 커서 기반 페이징 요청 DTO
 */
@Schema(description = "커서 기반 페이징 요청 정보")
@Getter
@Setter
@NoArgsConstructor
public class CursorRequestDTO {

    @Schema(description = "다음 페이지 커서 (첫 페이지는 null)", example = "1234")
    private Long cursor = null;

    @Schema(description = "페이지 크기", example = "10", defaultValue = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100을 초과할 수 없습니다")
    private int size = 10;

    @Schema(description = "정렬 기준 필드명", example = "id_field", pattern = "^[a-zA-Z0-9_]*$")
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "정렬 기준은 영문, 숫자, 언더스코어만 허용됩니다")
    private String orderBy;

    @Schema(description = "정렬 방향", example = "DESC", allowableValues = { "ASC", "DESC" }, defaultValue = "DESC")
    @Pattern(regexp = "^(ASC|DESC)$", message = "정렬 방향은 ASC 또는 DESC만 가능합니다")
    private String direction = "DESC";

    /**
     * 가져올 레코드 수를 반환합니다.
     * @return 가져올 레코드 수 (실제로는 +1을 해서 더 있는지 확인)
     */
    @Schema(hidden = true)
    public int getLimit() {
        return size + 1; // 다음 페이지 존재 여부 확인을 위해 1개 더 요청
    }

    /**
     * MyBatis에서 사용할 ORDER BY 절 문자열을 생성합니다.
     * @return ORDER BY 절 문자열
     */
    @Schema(hidden = true)
    public String getOrderByClause() {
        if (orderBy != null && !orderBy.isEmpty()) {
            return String.format("ORDER BY %s %s", orderBy, direction);
        }
        return "";
    }
}