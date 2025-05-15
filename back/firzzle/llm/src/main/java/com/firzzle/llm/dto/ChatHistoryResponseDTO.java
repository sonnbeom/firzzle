package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : ChatHistoryResponseDTO.java
 * @Description : 사용자-챗봇 대화 데이터 전송 객체
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자와 AI 챗봇 간의 대화 정보")
public class ChatHistoryResponseDTO {
    @Schema(description = "대화 일련 번호", example = "101")
    private Long chatSeq;

    @Schema(description = "사용자 질문", example = "DDD는 무슨 뜻인가요?")
    private String question;

    @Schema(description = "AI 챗봇 응답", example = "DDD는 도메인 주도 설계(DDD, Domain-Driven Design)를 의미합니다.")
    private String answer;

    @Schema(description = "생성 일시 (YYYYMMDDHHMMSS)", example = "20250515103045")
    private String indate;
}
