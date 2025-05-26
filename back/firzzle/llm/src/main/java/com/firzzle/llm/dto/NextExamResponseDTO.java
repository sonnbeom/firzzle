package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : RunningChatRequestDTO.java
 * @Description : 실시간 학습 대화 요청 DTO (질문 기반 챗봇용)
 * @author Firzzle
 * @since 2025. 5. 13.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시험 모드 질문 조회")
public class NextExamResponseDTO {
	@NotBlank
    @Schema(description = "문제 번호", example = "12")
    private Long exam_seq;
	
	@NotBlank
    @Schema(description = "질문 내용", example = "DDD(도메인 주도 설계)에 대해 설명해 보세요.")
    private String question;
    
}
