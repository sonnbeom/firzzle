package com.firzzle.llm.config;

import com.firzzle.llm.client.QdrantClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorDbConfig {

    @Value("${qdrant.host}")
    private String host;

    @Value("${qdrant.api-key}")
    private String apiKey;

    @Bean
    public QdrantClient vectorDbClient() {
        String baseUrl = String.format(host);  // 예: http://localhost:6333
        return new QdrantClient(baseUrl, apiKey);
    }
}