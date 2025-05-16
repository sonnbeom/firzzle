package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @Class Name : AiExamAnswerDTO.java
 * @Description : AI 시험 문제 답변 DTO (MyBatis 매핑 및 API 응답용 DTO) 
 * @author Firzzle
 * @since 2025. 5. 16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI 시험 문제 답변 정보")
public class ExamAnswerDTO {

    @Schema(description = "답변 일련 번호", example = "301")
    private Integer answerSeq;

    @Schema(description = "시험 일련 번호", example = "101")
    private Integer examSeq;

    @Schema(description = "내부 사용자 일련 번호", example = "202501")
    private Integer userSeq;

    @Schema(description = "답변 내용", example = "C. 이벤트 스토밍")
    private String answerContent;

    @Schema(description = "응답 내용", example = "이벤트 스토밍은 DDD의 핵심 개념이 아니며, 보조적 방법입니다.")
    private String explanationContent;

    @Schema(description = "등록 일시 (YYYYMMDDHHMMSS)", example = "20250516094530")
    private String indate;
}
