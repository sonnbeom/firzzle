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
@Schema(description = "시험모드 평가 응답 DTO")
public class ExamEvaluationResponseDTO {

    @Schema(description = "결과", example = "correct", required = true)
    private String result;

    @Schema(description = "피드백", example = "정답입니다! 잘 이해하고 있어요. 다음 질문을 받고 싶으면 질문 생성 버튼을 눌러주세요", required = true)
    private String feedback;

    @Schema(description = "마지막 문제 여부", example = "false", required = true)
    private Boolean isLastQuestion;

    @Schema(description = "모범답안", example = "인공지능의 주요 분야에는 기계학습, 자연어처리, 컴퓨터 비전 등이 있습니다...")
    private String modelAnswer;
}