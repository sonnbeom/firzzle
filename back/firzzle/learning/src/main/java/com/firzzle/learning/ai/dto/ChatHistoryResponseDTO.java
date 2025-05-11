package com.firzzle.learning.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 채팅 내역 항목 응답 DTO
 */
@Schema(description = "채팅 내역 항목")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponseDTO {

    @Schema(description = "채팅 일련번호", required = true, example = "12345")
    private Long chatSeq;

    @Schema(description = "질문 내용", required = true, example = "자바스크립트란 무엇인가요?")
    private String question;

    @Schema(description = "답변 내용", required = true, example = "자바스크립트는 웹 페이지에서 동적인 기능을 구현하는 프로그래밍 언어입니다.")
    private String answer;

    @Schema(description = "생성 일시", required = true, example = "2025-05-11 12:30:45")
    private String indate;
}