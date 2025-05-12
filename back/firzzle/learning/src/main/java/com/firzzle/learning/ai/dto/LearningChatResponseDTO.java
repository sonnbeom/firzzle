package com.firzzle.learning.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "학습모드 응답 DTO")
public class LearningChatResponseDTO {

    @Schema(description = "채팅 일련번호", example = "123")
    private Long chatSeq;

    @Schema(description = "답변 내용", example = "AI 학습의 기본 원리는 데이터로부터 패턴을 찾아내는 것입니다. 이는 통계적 방법론과 알고리즘을 활용하여 이루어집니다.")
    private String answer;
}