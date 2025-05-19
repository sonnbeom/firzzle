package com.firzzle.llm.service;

import com.firzzle.llm.client.QdrantClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RagService {

    private final QdrantClient qdrantClient;

    /**
     * 벡터와 콘텐츠를 Qdrant에 저장합니다.
     * @param collection 컬렉션 이름
     * @param id 고유 ID (Long 형)
     * @param vector 벡터 데이터
     * @param content 연관된 텍스트 콘텐츠
     */
    public void saveToVectorDb(String collection, Long id, List<Float> vector, Map<String, Object> payload) {
        qdrantClient.upsertVector(collection, id, vector, payload)
            .doOnSuccess(v -> log.info("✅ Qdrant 저장 완료: id={} collection={}", id, collection))
            .doOnError(e -> log.error("❌ Qdrant 저장 실패", e))
            .subscribe();
    }
    

	/**
	 * Qdrant에서 주어진 contentSeq에 해당하는 벡터 중,  
	 * 입력 벡터와 유사도가 높은 상위 3개의 payload.content 값을 반환합니다.
	 * 
	 * @param collection Qdrant 컬렉션 이름
	 * @param vector 기준이 되는 임베딩 벡터
	 * @param contentSeq 검색 대상 contentSeq 값 (payload에서 필터링)
	 * @return 상위 3개의 유사한 벡터의 payload.content 리스트 (Mono 비동기 결과)
	 * 
	 * <p>
	 * 이 메서드는 Qdrant의 filter + 유사도 검색을 조합하여  
	 * 같은 콘텐츠 그룹(contentSeq) 내에서 의미 있는 유사 컨텍스트를 빠르게 찾는 데 유용합니다.
	 * </p>
	 */
    public Mono<List<String>> searchTopPayloadsByContentSeq(String collection, List<Float> vector, Long contentSeq) {
        // Qdrant 검색 요청 구성
        Map<String, Object> request = Map.of(
            "vector", vector,
            "limit", 20,  // 넉넉하게 검색하고
            "with_payload", true,
            "filter", Map.of(
                "must", List.of(
                    Map.of("key", "contentSeq", "match", Map.of("value", contentSeq))
                )
            )
        );

        return qdrantClient.searchRaw(collection, request) // 아래에 함께 정의
            .map(results -> results.stream()
                .sorted((a, b) -> {
                    Double sa = ((Number) a.get("score")).doubleValue();
                    Double sb = ((Number) b.get("score")).doubleValue();
                    return -Double.compare(sa, sb); // 내림차순 정렬
                })
                .limit(5)
                .map(result -> {
                    Map<String, Object> payload = (Map<String, Object>) result.get("payload");
                    return payload != null ? payload.getOrDefault("content", "").toString() : "";
                })
                .filter(content -> !content.isBlank())
                .collect(Collectors.toList())
            );
    }
    
    /**
     * Qdrant에서 임의의 조건 없이 유사도 기준으로 상위 N개의 payload.content 값을 반환합니다.
     *
     * @param collection Qdrant 컬렉션 이름
     * @param vector 기준 벡터
     * @param limit 최대 개수 (보통 5~10 정도)
     * @param scoreThreshold 유사도 필터링 기준 (예: 0.8)
     * @return payload.content 값 리스트 (유사도 기준 정렬)
     */
    public Mono<List<String>> searchTopPayloads(String collection, List<Float> vector, int limit, double scoreThreshold) {
        return qdrantClient.searchWithPayload(collection, vector, limit, scoreThreshold)
            .doOnSuccess(result -> log.info("✅ Qdrant 일반 유사도 검색 결과 {}개", result.size()))
            .doOnError(e -> log.error("❌ Qdrant 일반 유사도 검색 실패", e));
    }
    
    /**
     * contentSeq에 해당하는 벡터와 키워드(payload)를 함께 조회합니다.
     *
     * @param collection Qdrant 컬렉션 이름
     * @param contentSeq 조회할 콘텐츠 ID
     * @return vector + payload를 포함한 Map (예: {"vector": ..., "payload": ...})
     */
    public Mono<Map<String, Object>> getVectorWithPayloadByContentSeq(
            String collection,
            Long contentSeq
    ) {
        Map<String, Object> body = Map.of(
            "limit",        1,
            "with_vector",  true,
            "with_payload", true,
            "filter", Map.of(
                "must", List.of(
                    Map.of(
                        "key",   "contentSeq",
                        "match", Map.of("value", contentSeq)
                    )
                )
            )
        );

        // searchRaw 대신 scrollRaw 사용
        return qdrantClient.scrollRaw(collection, body)
            .map(results -> {
                if (results.isEmpty()) {
                    throw new IllegalStateException(
                      "contentSeq=" + contentSeq + " 를 찾을 수 없습니다."
                    );
                }
                var hit = results.get(0);
                return Map.of(
                    "vector",  hit.get("vector"),
                    "payload", hit.get("payload")
                );
            });
    }

    /**
     * 기준 벡터를 기반으로 contentSeq가 일치하지 않는 유사 콘텐츠를 검색합니다.
     * 키워드 필터링은 하지 않으며, 자기 자신만 제외하고 유사도 기반으로 추천합니다.
     *
     * @param collection Qdrant 컬렉션 이름
     * @param baseVector 추천 기준 벡터
     * @param limit 최대 결과 수
     * @param minScore 유사도 하한선
     * @param excludeContentSeq 제외할 콘텐츠 ID
     * @return 유사 콘텐츠 payload 리스트 (score 포함)
     */
    public Mono<List<Map<String,Object>>> searchSimilarByVectorExcludingSelf(
            String collection,
            List<Float> baseVector,
            int limit,
            double minScore,
            Long excludeContentSeq
    ) {
        Map<String, Object> filter = Map.of(
            "must_not", List.of(
                Map.of("key", "contentSeq", "match", Map.of("value", excludeContentSeq))
            )
        );

        Map<String, Object> body = Map.of(
            "vector_name", "vector",
            "vector",      baseVector,
            "limit",       limit,
            "with_payload", true,
            "filter",      filter
        );

        return qdrantClient.searchRaw(collection, body)
            .map(results ->
                results.stream()
                       .filter(r -> ((Number) r.get("score")).doubleValue() >= minScore)
                       .collect(Collectors.toList())
            );
    }




}
