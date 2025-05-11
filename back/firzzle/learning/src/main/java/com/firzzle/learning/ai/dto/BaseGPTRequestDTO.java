package com.firzzle.learning.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * GPT API 요청의 기본 DTO
 */
@Data
public class BaseGPTRequestDTO {

    @Schema(description = "사용할 모델명", example = "gpt-3.5-turbo", required = true)
    private String model;

    @Schema(description = "시퀀스 번호", example = "1", required = true)
    private String seq;

    @Schema(description = "응답의 다양성과 무작위성 조절 (0.0-2.0)", example = "0.7")
    private Float temperature;

    @Schema(description = "생성될 응답의 최대 길이", example = "1000")
    private Integer maxTokens;

    @Schema(description = "상위 확률 모델이 가장 확률 높은 토큰들 중에서만 선택 (0.0-1.0)", example = "0.9")
    private Float topP;

    @Schema(description = "빈도 패널티 - 자주 등장한 토큰에 패널티 부여 (-2.0-2.0)", example = "0.0")
    private Float frequencyPenalty;

    @Schema(description = "존재 패널티 - 이미 등장한 토큰에 패널티 부여 (-2.0-2.0)", example = "0.0")
    private Float presencePenalty;

    @Schema(description = "중지 시퀀스 - 응답 생성을 중지할 문자열 목록")
    private String[] stop;

    @Schema(description = "로짓 바이어스 - 특정 토큰의 출현 확률 수동 조정")
    private Map<String, Integer> logitBias;

    @Schema(description = "응답 형식 - 모델 응답의 형식 지정 (예: JSON)")
    private Map<String, String> responseFormat;
}