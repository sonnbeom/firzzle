package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

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
@Schema(description = "실시간 학습 챗봇 응답 정보")
public class learningChatResponseDTO {

    @NotBlank
    @Schema(description = "챗봇의 응답 내용", example = "DDD는 도메인 주도 설계를 의미하며, 복잡한 문제를 도메인 모델로 해결하려는 방식입니다.")
    private String answer;

    @Schema(description = "응답의 참고 시간 정보 (초 단위 형식)", example = "125")
    private Integer referenceTime;
}
