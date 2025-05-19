package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : ExamAnswerDTO.java
 * @Description : 시험 모드 응답 정보보 DTO (MyBatis 매핑 및 API 응답용 DTO)
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시험 응답 정보 DTO")
public class ExamAnswerDTO {

    @Schema(description = "시험 모드 응답 일련 번호", example = "1001")
    private Long answerSeq;

    @Schema(description = "시험 모드 질문 일련 번호", example = "2001")
    private Long examSeq;

    @Schema(description = "사용자 일련 번호", example = "3001")
    private Long userSeq;

    @Schema(description = "사용자의 답변 내용", example = "세포 호흡 과정에서 ATP가 생성되기 때문입니다.")
    private String answerContent;

    @Schema(description = "AI가 생성한 해설", example = "세포 호흡은 포도당을 산화시켜 ATP를 생성하는 과정입니다. 이 과정은 생명 유지에 필수적입니다.")
    private String explanationContent;

    @Schema(description = "등록 일시 (YYYYMMDDHHMMSS)", example = "20250518194530")
    private String indate;
}
