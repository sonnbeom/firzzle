package com.firzzle.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 시험 내역 항목 응답 DTO
 */
@Schema(description = "시험 내역 항목")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamHistoryResponseDTO {

    @Schema(description = "시험 일련번호", required = true, example = "12345")
    private Long examSeq;

    @Schema(description = "문제 번호", required = true, example = "1")
    private int questionNumber;

    @Schema(description = "문제 내용", required = true, example = "자바스크립트에서 변수를 선언하는 방법은?")
    private String questionContent;

    @Schema(description = "사용자 답변 내용", example = "var, let, const 키워드를 사용합니다.")
    private String answerContent;

    @Schema(description = "모범 답안", example = "var, let, const 키워드를 사용하여 변수를 선언할 수 있습니다.")
    private String modelAnswer;

    @Schema(description = "평가 코드", example = "E", allowableValues = {"E", "F", "P"})
    private String evaluation;

    @Schema(description = "평가 결과", example = "correct", allowableValues = {"correct", "incorrect", "irrelevant"})
    private String evaluationResult;

    @Schema(description = "생성 일시", required = true, example = "2025-05-11 12:30:45")
    private String indate;
}