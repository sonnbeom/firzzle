package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

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
@Schema(description = "실시간 학습 챗봇 요청 정보")
public class LearningChatRequestDTO {
    @NotBlank(message = "질문 내용은 비워둘 수 없습니다.")
    @Size(max = 200, message = "질문은 최대 200자까지 입력할 수 있습니다.")
    @Schema(description = "현재 사용자 질문", example = "이 영상에서 DDD는 무슨 뜻인가요?", required = true)
    private String question;
}
