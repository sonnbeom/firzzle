package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "챗봇 대화 메시지 DTO")
public class ChatMessageDTO {
    @Schema(description = "메시지 일련번호", example = "2005")
    private Long messageId;

    @Schema(description = "메시지 타입 (user / assistant)", example = "user")
    private String sender;

    @Schema(description = "메시지 내용", example = "DDD가 뭔가요?")
    private String content;

    @Schema(description = "보낸 시간 (YYYYMMDDHHMMSS)", example = "20250515114523")
    private String sentAt;
}
