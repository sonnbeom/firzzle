package com.firzzle.llm.dto;

import com.firzzle.llm.domain.ModelType;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "OpenAI 챗 요청 정보")
public class ChatCompletionRequestDTO {

    @Schema(description = "System 메시지", example = "당신은 똑똑한 학습 도우미입니다.")
    private String systemMessage;

    @Schema(description = "사용자 질문", required = true, example = "DDD가 뭐예요?")
    private String userPrompt;

    @Schema(description = "모델 타입", example = "RUNNINGCHAT")
    private ModelType modelType;

    @Schema(description = "temperature 값", example = "0.7")
    private Double temperature;

    @Schema(description = "top_p 값", example = "1.0")
    private Double topP;

    @Schema(description = "max_tokens 값", example = "1024")
    private Integer maxTokens;
}