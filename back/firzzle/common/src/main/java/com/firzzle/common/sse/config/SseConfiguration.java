package com.firzzle.common.sse.config;

import com.firzzle.common.sse.NoopSseEmitter;
import com.firzzle.common.sse.SseEmitter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Class Name : SseConfiguration
 * @Description : SSE 설정 클래스
 *
 * @author Firzzle
 * @since 2025. 5. 16.
 */
@Configuration
public class SseConfiguration {

    /**
     * SSE 기능이 비활성화된 경우 기본 No-op 구현체 제공
     */
    @Bean
    @ConditionalOnProperty(name = "firzzle.sse.enabled", havingValue = "false", matchIfMissing = true)
    public SseEmitter noopSseEmitter() {
        return new NoopSseEmitter();
    }

}