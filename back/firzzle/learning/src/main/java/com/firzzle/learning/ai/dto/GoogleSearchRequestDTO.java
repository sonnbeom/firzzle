package com.firzzle.learning.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Google 검색 API 요청 DTO
 */
@Data
public class GoogleSearchRequestDTO {

    @NotEmpty(message = "검색어는 필수입니다.")
    @Schema(description = "검색할 쿼리 문자열", example = "최신 인공지능 기술 트렌드", required = true)
    private String query;
}