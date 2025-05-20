package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : LlmRequestDTO.java
 * @Description : LLM 요청을 위한 스크립트 기반 데이터 DTO (예: 요약/질문 생성 등 처리용)
 * @author Firzzle
 * @since 2025. 5. 16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "시험 모드 응답")
public class ExamAnswerResponseDTO {
    @Schema(description = "시험 모드 응답 및 해설", example = "안녕하세요. 이 영상에서는 도메인 주도 설계, 즉 DDD에 대해 설명합니다...", required = true)
    private String Explanation;

    @Schema(description = "생성 시간", example = "20250519165647", required = true)
    private String indate;
}
