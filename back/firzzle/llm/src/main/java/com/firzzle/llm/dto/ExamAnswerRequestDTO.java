package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : ExamAnswerRequestDTO.java
 * @Description : 시험 문제 응답을 생성을 요청할 DTO 
 * @author Firzzle
 * @since 2025. 5. 16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "시험 모드 정답 제출 DTO")
public class ExamAnswerRequestDTO {
    @Schema(description = "문제 일련 번호", example = "1", required = true)
    private Long exam_seq;
	
    @Schema(description = "시험 모드 질문에 대한 응답", example = "판타지 장르는 18세기와 19세기 낭만주의 시기에 발전하였으며, 톨킨과 에드가 엘란 포 같은 작가들이 중요한 역할을 했다. ", required = true)
    private String answer;
}

