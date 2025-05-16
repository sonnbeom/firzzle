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
public class ExamDTO {

    @Schema(description = "시험 일련 번호", example = "101")
    private Integer examSeq;

    @Schema(description = "콘텐츠 일련 번호", example = "1001")
    private Integer contentSeq;

    @Schema(description = "문제 내용", example = "다음 중 DDD의 핵심 개념이 아닌 것은?")
    private String questionContent;

    @Schema(description = "모델 답안", example = "C. 이벤트 스토밍")
    private String modelAnswer;

    @Schema(description = "영상 해당 부분 시간", example = "00:05:12")
    private String startTime;

    @Schema(description = "참고 내용", example = "이 내용은 DDD의 배경 설명에서 다뤄집니다.")
    private String referenceText;
}
