package com.firzzle.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시험모드 문제 응답 DTO")
public class ExamQuestionResponseDTO {

    @Schema(description = "상태", example = "success", required = true)
    private String status;

    @Schema(description = "문제 번호", example = "1")
    private Integer questionNumber;

    @Schema(description = "총 문제 수", example = "3")
    private Integer totalQuestions;

    @Schema(description = "문제", example = "인공지능의 주요 분야들을 설명하고, 각 분야의 대표적인 응용 사례를 하나씩 제시하시오.")
    private String question;
    
    @Schema(description = "마지막 문제 여부", example = "false", required = true)
    private Boolean isLastQuestion;
}