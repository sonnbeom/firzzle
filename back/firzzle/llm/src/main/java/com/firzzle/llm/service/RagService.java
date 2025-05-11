package com.firzzle.llm.service;

import com.firzzle.llm.client.QdrantClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
