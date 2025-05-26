package com.firzzle.learning.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "시험모드 답변 요청 DTO")
public class ExamAnswerRequestDTO {

    @NotEmpty(message = "답변은 필수입니다.")
    @Size(max = 1000, message = "답변은 1000자 이내로 입력해주세요.")
    @Schema(description = "사용자 답변", example = "인공지능의 주요 분야에는 기계학습, 자연어처리, 컴퓨터 비전 등이 있으며...", required = true)
    private String answer;
}