package com.firzzle.learning.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "학습모드 질문 요청 DTO")
public class LearningChatRequestDTO {

    @NotEmpty(message = "질문은 필수입니다.")
    @Size(max = 200, message = "질문은 200자 이내로 입력해주세요.")
    @Schema(description = "사용자 질문", example = "AI 학습의 기본 원리는 무엇인가요?", required = true)
    private String question;
}