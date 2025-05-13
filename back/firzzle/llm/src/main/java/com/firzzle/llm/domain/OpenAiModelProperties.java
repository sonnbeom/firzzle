package com.firzzle.llm.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "spring.ai.openai")
@Data
public class OpenAiModelProperties {
    private ModelConfig timeline;
    private ModelConfig summary;

    @Data
    public static class ModelConfig {
        private String apiKey;
        private String baseUrl;
        private String model;
    }
}