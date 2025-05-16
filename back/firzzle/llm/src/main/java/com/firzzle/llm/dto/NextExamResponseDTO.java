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
    @Schema(description = "질문 내용", example = "DDD(도메인 주도 설계)에 대해 설명해 보세요.")
    private String question;
    
    @NotBlank
    @Schema(description = "전체 문제 개수", example = "10")
    private int totalCount;

    @NotBlank
    @Schema(description = "현재 문제 인덱스 (1부터 시작)", example = "3")
    private int currentIndex;
}
