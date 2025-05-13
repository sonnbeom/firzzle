package com.firzzle.llm.service;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                        .resolver(DefaultAddressResolverGroup.INSTANCE) // 시스템 DNS resolver 명시
                ))
            .build();

    /**
     * OpenAI Embedding API 호출하여 벡터 반환
     * @param text 임베딩할 텍스트
     * @return float 벡터 리스트
     */
    @SuppressWarnings("unchecked")
    public List<Float> embed(String text) {
        try {
            log.info("📨 OpenAI 임베딩 요청 시작: {}", text);

            Map<String, Object> request = Map.of(
                "model", "text-embedding-3-small",
                "input", text,
                "encoding_format", "float"
            );

            Map<String, Object> response = webClient.post()
                .uri("/embeddings")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

            if (response == null || !response.containsKey("data")) {
                log.error("❌ OpenAI 응답 오류: data 없음 → {}", response);
                throw new IllegalStateException("OpenAI 임베딩 응답이 잘못되었습니다.");
            }

            // 1) 받은 embedding을 Double 리스트로 추출
            List<?> dataList = (List<?>) response.get("data");
            Map<?,?> firstMap = (Map<?,?>) dataList.get(0);
            List<Double> raw = (List<Double>) firstMap.get("embedding");

            // 2) Double → Float로 안전하게 변환
            List<Float> embedding = raw.stream()
                .map(Double::floatValue)
                .toList();

            log.info("✅ OpenAI 임베딩 완료, 길이: {}", embedding.size());
            return embedding;

        } catch (WebClientResponseException e) {
            log.error("❌ OpenAI API 호출 오류: body={}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("OpenAI API 호출 실패", e);
        } catch (Exception e) {
            log.error("❌ 임베딩 생성 중 예상치 못한 오류 발생", e);
            throw new RuntimeException("임베딩 처리 중 오류 발생", e);
        }
    }
}
