package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : ContentDTO.java
 * @Description : 콘텐츠 정보 DTO (MyBatis 매핑 및 API 응답용 DTO)
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "시험모드 문제 정보")
public class ExamDTO {
    @Schema(description = "시험 문제 일련 번호", example = "1001")
    private Long ExamSeq;

    @Schema(description = "유튜브 영상 일련 번호", example = "1001")
    private Long ContentSeq;

    @Schema(description = "질문 내용", example = "도메인 주도 설계(DDD)란 무엇인가요?")
    private String QuestionContent;

    @Schema(description = "모범 답인", example = "DDD란 무엇입니다.")
    private String ModelAnswer;
}
