package com.firzzle.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 커서 기반 페이징 응답 DTO
 * @param <T> 응답 항목의 타입
 */
@Schema(description = "커서 기반 페이징 응답")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorResponseDTO<T> {

    @Schema(description = "응답 항목 목록", required = true)
    private List<T> content;

    @Schema(description = "다음 페이지 존재 여부", required = true, example = "true")
    private boolean hasNextPage;

    @Schema(description = "다음 페이지 요청 시 사용할 커서", example = "1234")
    private Long nextCursor;
}