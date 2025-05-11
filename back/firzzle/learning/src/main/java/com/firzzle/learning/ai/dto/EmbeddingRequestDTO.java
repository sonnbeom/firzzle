package com.firzzle.learning.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 임베딩 API 요청 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmbeddingRequestDTO extends BaseGPTRequestDTO {

    @Schema(description = "프롬프트가 여러 개일 경우 사용되는 순번", example = "1")
    private String seqNum;

    @Schema(description = "임베딩에 사용할 메시지")
    private List<Map<String, Object>> messages;

    @Schema(description = "치환 문자")
    private Map<String, Object> placeholders;

    @Schema(description = "임베딩하려는 입력 텍스트", example = "The food was delicious and the service was excellent.")
    private String input;
}