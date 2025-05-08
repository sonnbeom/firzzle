package com.firzzle.llm.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "llmExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // 동시 요청 처리 수
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(300);
        executor.setThreadNamePrefix("LLM-");
        executor.initialize();
        return executor;
    }
}
