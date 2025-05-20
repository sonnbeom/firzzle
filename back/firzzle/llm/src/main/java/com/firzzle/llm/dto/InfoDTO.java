package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
/**
 * @Class Name : AiExamDTO.java
 * @Description : AI 시험 문제 DTO (MyBatis 매핑 및 API 응답용 DTO)
 * @author Firzzle
 * @since 2025. 5. 16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI 시험 문제 정보")
public class InfoDTO {
    @Schema(description = "currentExamSeq", example = "1")
    private Long currentExamSeq; // 진행 중 문제 번호 (없으면 null)

    @Schema(description = "solvedCount", example = "5")
    private int solvedCount; // 푼 문제 수
}
