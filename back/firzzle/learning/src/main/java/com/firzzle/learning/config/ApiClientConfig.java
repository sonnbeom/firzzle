package com.firzzle.learning.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * @Class Name : ApiClientConfig.java
 * @Description : API 클라이언트 설정
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Configuration
public class ApiClientConfig {

    /**
     * RestTemplate Bean 설정
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }
}