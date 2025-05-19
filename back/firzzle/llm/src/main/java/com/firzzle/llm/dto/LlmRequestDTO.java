package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : LlmRequestDTO.java
 * @Description : LLM 요청을 위한 스크립트 기반 데이터 DTO (예: 요약/질문 생성 등 처리용)
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "LLM 처리 요청 DTO")
public class LlmRequestDTO {	
    @Schema(description = "사용자 콘텐츠 일련 번호", example = "1001", required = true)
    private Long userContentSeq;
	

    @Schema(description = "콘텐츠 일련 번호", example = "1001", required = true)
    private Long contentSeq;

    @Schema(description = "영상 전체 스크립트", example = "안녕하세요. 이 영상에서는 도메인 주도 설계, 즉 DDD에 대해 설명합니다...", required = true)
    private String script;

    @Schema(description = "작업 ID (SSE 연결용)", example = "a1b2c3d4-5678-90ab-cdef-123456789012")
    private String taskId;
}