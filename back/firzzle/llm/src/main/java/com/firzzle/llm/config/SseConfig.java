package com.firzzle.llm.config;

import com.firzzle.common.sse.SseEmitter;
import com.firzzle.llm.sse.LlmSseEmitter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "firzzle.sse.enabled", havingValue = "true")
public class SseConfig {

    @Bean
    @Primary
    public SseEmitter llmSseEmitter() {
        return new LlmSseEmitter();
    }
}
