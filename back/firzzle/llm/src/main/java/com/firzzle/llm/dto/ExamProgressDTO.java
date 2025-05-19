package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : ExamProgressDTO.java
 * @Description : 학습 진행 상황 저장
 * @author Firzzle
 * @since 2025. 5. 19.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamProgressDTO {

    @Schema(description = "진행 상황 일련 번호", example = "1", required = true)
    private Long progressSeq;

    @Schema(description = "내부 사용자 일련 번호", example = "4", required = true)
    private Long userSeq;

    @Schema(description = "콘텐츠 일련 번호", example = "15", required = true)
    private Long contentSeq;

    @Schema(description = "현재 풀이 중인 문제 일련 번호", example = "3")
    private Long examSeq;

    @Schema(description = "푼 문제 수", example = "5", required = true)
    private Integer solvedCount;

    @Schema(description = "완료 여부 (Y/N)", example = "N")
    private String isCompleted;
}
