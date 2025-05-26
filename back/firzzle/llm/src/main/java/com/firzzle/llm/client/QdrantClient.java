package com.firzzle.llm.client;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.firzzle.llm.dto.*;
import reactor.core.publisher.Mono;

/**
 * Qdrant와 통신하는 클라이언트 클래스입니다.
 * - 벡터 삽입(Upsert)
 * - 벡터 유사도 검색(Search)
 */
public class QdrantClient {

    private static final Logger log = LoggerFactory.getLogger(QdrantClient.class);
    private final WebClient webClient;

    /**
     * QdrantClient 생성자
     * @param baseUrl Qdrant 서버의 Base URL (예: http://localhost:6333)
     */
    public QdrantClient(String baseUrl, String apiKey) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("api-key", apiKey) // 인증 헤더 추가
            .build();
        log.info("QdrantClient initialized with baseUrl: {}", baseUrl);
    }

    /**
     * Qdrant에 벡터를 업서트(Insert or Update)합니다.
     * @param collection 컬렉션 이름
     * @param id 벡터 ID
     * @param vector 벡터 데이터 (Float 리스트)
     * @return Mono<Void> (응답 없음)
     */
    public Mono<Void> upsertVector(String collection, Long id, List<Float> vector, String content) {
        // 실제 요청 본문을 로깅
        Map<String, Object> requestBody = Map.of(
            "points", List.of(Map.of(
                "id", id,  // 문자열 ID 사용
                "vector", vector,
                "payload", Map.of(
                		"tag", "default",
                		"content", content)
            ))
        );
        
        log.info("📤 Qdrant 업서트 요청 본문: {}", requestBody);
        log.info("벡터 길이: {}", vector.size());
        
        return webClient.put()
            .uri("/collections/{collection}/points", collection)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> log.info("Successfully upserted vector with id={} into collection={}", id, collection))
            .doOnError(e -> {
                log.error("Failed to upsert vector: {}", e.getMessage(), e);
                if (e.getMessage().contains("response body")) {
                    log.error("Response body: {}", e.getMessage());
                }
            });
    }
    
    public Mono<Void> upsertVector(String collection, Long id, List<Float> vector, Map<String, Object> payload) {
        Map<String, Object> requestBody = Map.of(
            "points", List.of(Map.of(
                "id", id,
                "vector", vector,
                "payload", payload
            ))
        );

        log.info("📤 Qdrant 업서트 요청 본문: {}", requestBody);

        return webClient.put()
            .uri("/collections/{collection}/points", collection)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> log.info("✅ Qdrant 저장 완료: id={} collection={}", id, collection))
            .doOnError(e -> log.error("❌ Qdrant 저장 실패", e));
    }

    /**
     * Qdrant에서 유사 벡터를 검색합니다.
     * @param collection 컬렉션 이름
     * @param vector 검색 기준 벡터
     * @param limit 검색할 결과 수
     * @return 유사 벡터 결과 목록 (List<Map<String, Object>>)
     */
    public Mono<List<Map<String, Object>>> search(String collection, List<Float> vector, int limit) {
        log.debug("Searching similar vectors: collection={}, vector={}, limit={}", collection, vector, limit);

        return webClient.post()
        	    .uri("/collections/{collection}/points/search", collection)
        	    .bodyValue(Map.of(
        	        "vector", vector,
        	        "limit", limit,
        	        "with_payload", true  // 💡 이거 추가
        	    ))
        	    .retrieve()
        	    .bodyToMono(QdrantSearchResponseDTO.class)
        	    .map(QdrantSearchResponseDTO::getResult)
        	    .doOnSuccess(result -> log.info("Search completed for collection={} with {} results", collection, result.size()))
        	    .doOnError(e -> log.error("Search failed: {}", e.getMessage(), e));

    }
    
    /**
     * 유사도 기준 이상인 ID만 필터링해서 반환하는 메서드
     * @param collection Qdrant 컬렉션 이름
     * @param vector 검색 기준 벡터
     * @param limit 최대 결과 수
     * @param scoreThreshold 유사도 점수 기준 (예: 0.8)
     * @return 유사한 벡터들의 ID 리스트
     */
    public Mono<List<String>> searchIds(String collection, List<Float> vector, int limit, double scoreThreshold) {
        return search(collection, vector, limit)
            .map(results -> results.stream()
                .filter(result -> {
                    Object scoreObj = result.get("score");
                    return scoreObj instanceof Number && ((Number) scoreObj).doubleValue() >= scoreThreshold;
                })
                .map(result -> result.get("id").toString())
                .toList()
            );
    }
    
    
    public Mono<List<String>> searchWithPayload(
            String collection,
            List<Float> vector,
            int limit,
            double scoreThreshold
    ) {
        return search(collection, vector, limit)
            .map(results -> results.stream()
                .filter(result -> {
                    Object score = result.get("score");
                    return score instanceof Number
                        && ((Number) score).doubleValue() >= scoreThreshold;
                })
                .map(result -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> payload =
                        (Map<String, Object>) result.get("payload");
                    return payload != null
                        ? payload.getOrDefault("content", "").toString()
                        : "";
                })
                .filter(content -> !content.isBlank())
                .collect(Collectors.toList())      // ← use collect instead of toList()
            );
    }

    
    public Mono<List<Map<String, Object>>> searchRaw(String collection, Map<String, Object> requestBody) {
        // 요청 바디 로깅
        log.debug("🔍 Qdrant searchRaw 요청 바디: {}", requestBody);

        return webClient.post()
            .uri("/collections/{collection}/points/search", collection)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(QdrantSearchResponseDTO.class)
            .map(QdrantSearchResponseDTO::getResult)
            .doOnSuccess(result ->
                log.info("🔍 필터 포함 검색 성공: {}개", result.size())
            )
            .doOnError(WebClientResponseException.class, ex -> {
                // HTTP 에러일 때 상태 코드와 응답 본문, 요청 바디 모두 로깅
                log.error("❌ Qdrant 검색 실패: status={} body={}",
                    ex.getRawStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex
                );
                log.error("   요청 바디: {}", requestBody);
            });
    }
    
    public Mono<List<Map<String,Object>>> scrollRaw(
            String collection,
            Map<String,Object> requestBody
    ) {
        log.debug("🌀 Qdrant scrollRaw 요청 바디: {}", requestBody);

        return webClient.post()
            .uri("/collections/{collection}/points/scroll", collection)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(QdrantScrollResponseDTO.class)
            // ← 여기에 제네릭 힌트를 추가하세요
            .<List<Map<String,Object>>>map(dto -> {
                Object raw = dto.getResult().get("points");
                if (raw instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String,Object>> points = (List<Map<String,Object>>) raw;
                    return points;
                }
                return Collections.<Map<String,Object>>emptyList();
            })
            .doOnSuccess(r -> log.info("🌀 scroll 성공: {}개", r.size()))
            .doOnError(e -> log.error("❌ scroll 실패", e));
    }
}
