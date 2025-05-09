package com.firzzle.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "콘텐츠 요약 정보 요청")
public class ContentSummaryRequestDTO {

    @Schema(description = "콘텐츠 일련번호", required = true)
    private Long contentSeq;

    @Schema(description = "요약 난이도 (E: easy, H: high)", defaultValue = "E")
    private String level;
}