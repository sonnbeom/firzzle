package com.firzzle.learning.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * GPT 채팅 API 요청 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "GPT 채팅 API 요청 정보", example = "{\n" +
        "  \"model\": \"gpt-4o-mini\",\n" +
        "  \"seq\": \"1\",\n" +
        "  \"temperature\": 0.7,\n" +
        "  \"maxTokens\": 1000,\n" +
        "  \"topP\": 0.9,\n" +
        "  \"frequencyPenalty\": 0,\n" +
        "  \"presencePenalty\": 0,\n" +
        "  \"stop\": [\n" +
        "    \"###\"\n" +
        "  ],\n" +
        "  \"seqNum\": \"1\",\n" +
        "  \"messages\": [\n" +
        "    {\n" +
        "      \"role\": \"system\",\n" +
        "      \"content\": \"당신은 도움이 되는 AI 어시스턴트입니다.\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"role\": \"user\",\n" +
        "      \"content\": \"안녕하세요, 오늘 날씨가 어때요?\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"placeholders\": {\n" +
        "    \"system\": {\n" +
        "      \"name\": \"채팅 어시스턴트\",\n" +
        "      \"company\": \"Firzzle\"\n" +
        "    },\n" +
        "    \"user\": {\n" +
        "      \"location\": \"서울\"\n" +
        "    }\n" +
        "  }\n" +
        "}")
public class GPTChatRequestDTO extends BaseGPTRequestDTO {

    @Schema(description = "프롬프트가 여러 개일 경우 사용되는 순번", example = "1")
    private String seqNum;

    @Schema(description = "사용자와 AI 간의 대화 내역이 담긴 리스트")
    private List<Map<String, Object>> messages;

    @Schema(description = "시스템, 사용자, 어시스턴트 프롬프트에서 사용할 치환 문자")
    private Map<String, Object> placeholders;
}