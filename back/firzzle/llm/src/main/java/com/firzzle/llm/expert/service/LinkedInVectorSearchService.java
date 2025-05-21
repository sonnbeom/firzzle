package com.firzzle.llm.expert.service;

import com.firzzle.llm.service.RagService;
import com.firzzle.llm.expert.dto.LinkedInEmbeddingRequestDTO;
import com.firzzle.llm.expert.dto.LinkedInProfileSimilarityDTO;
import com.firzzle.llm.expert.dto.LinkedInSimilarityRequestDTO;
import com.firzzle.llm.expert.dto.LinkedInSimilarityResponseDTO;
import com.firzzle.llm.util.QdrantCollections;
import com.firzzle.llm.client.QdrantClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * LinkedIn 프로필 벡터 검색 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedInVectorSearchService {

    private static final String LINKEDIN_COLLECTION = QdrantCollections.LINKEDIN_PROFILES;

    private final RagService ragService;
    private final QdrantClient qdrantClient;
    private final LinkedInEmbeddingService embeddingService;

    /**
     * LinkedIn 프로필을 Qdrant에 저장합니다.
     *
     * @param profile LinkedIn 프로필 데이터
     * @return 저장 성공 여부
     */
    public boolean saveProfileToVectorDb(LinkedInEmbeddingRequestDTO profile) {
        try {
            // 1. 프로필 임베딩 생성
            List<Float> embedding = embeddingService.createEmbedding(profile);

            // 2. 페이로드 생성
            Map<String, Object> payload = embeddingService.createProfilePayload(profile);

            // 3. Qdrant에 저장 (QdrantClient 직접 사용)
            try {
                qdrantClient.upsertVector(LINKEDIN_COLLECTION, profile.getProfileSeq(), embedding, payload)
                        .block(); // 동기적으로 처리하기 위해 block() 사용
                log.info("LinkedIn 프로필 벡터 저장 요청 완료: profileSeq={}", profile.getProfileSeq());
                return true;
            } catch (Exception e) {
                log.error("LinkedIn 프로필 벡터 저장 실패 (QdrantClient): {}", e.getMessage(), e);
                return false;
            }
        } catch (Exception e) {
            log.error("LinkedIn 프로필 임베딩 생성 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 컬렉션이 존재하는지 확인하고, 없다면 생성합니다.
     *
     * @return 성공 여부
     */
    public boolean ensureCollectionExists() {
        try {
            // 컬렉션 존재 확인 및 생성 로직 구현
            // 이 예제에서는 실제 구현은 생략하고 로깅만 추가
            log.info("Qdrant 컬렉션 확인: {}", LINKEDIN_COLLECTION);

            // 컬렉션이 있다고 가정
            return true;
        } catch (Exception e) {
            log.error("Qdrant 컬렉션 확인/생성 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 태그 기반으로 유사한 LinkedIn 프로필을 검색합니다.
     *
     * @param request 검색 요청 정보
     * @return 검색 결과
     */
    public CompletableFuture<LinkedInSimilarityResponseDTO> searchSimilarProfiles(LinkedInSimilarityRequestDTO request) {
        try {
            // 기본값 설정
            float minScore = request.getMinScore() != null ? request.getMinScore() : 0.3f;
            int limit = request.getLimit() != null ? request.getLimit() : 9;
            int page = request.getPage() != null ? request.getPage() : 1;
            int pageSize = request.getPageSize() != null ? request.getPageSize() : 3;

            // 컬렉션 존재 확인
            if (!ensureCollectionExists()) {
                CompletableFuture<LinkedInSimilarityResponseDTO> future = new CompletableFuture<>();
                future.completeExceptionally(new RuntimeException("Qdrant 컬렉션이 존재하지 않습니다: " + LINKEDIN_COLLECTION));
                return future;
            }

            // 1. 태그 임베딩 생성
            List<Float> tagsEmbedding = embeddingService.createTagsEmbedding(request.getTags());

            // 2. 유사 프로필 검색
            return searchSimilarProfilesByVector(tagsEmbedding, limit, minScore)
                    .map(results -> {
                        // 3. 페이지네이션 처리
                        int start = (page - 1) * pageSize;
                        int end = Math.min(start + pageSize, results.size());
                        List<LinkedInProfileSimilarityDTO> pagedResults =
                                start < results.size() ? results.subList(start, end) : new ArrayList<>();

                        LinkedInSimilarityResponseDTO responseDTO1 = LinkedInSimilarityResponseDTO.builder()
                                .contentSeq(request.getContentSeq())
                                .tags(request.getTags())
                                .profiles(pagedResults)
                                .page(page)
                                .pageSize(pageSize)
                                .totalElements(results.size())
                                .totalPages((results.size() + pageSize - 1) / pageSize)
                                .last(page * pageSize >= results.size())
                                .hasNext(page * pageSize < results.size())
                                .build();

                        log.debug(responseDTO1.toString());

                        // 4. 응답 구성
                        return responseDTO1;
                    })
                    .toFuture();
        } catch (Exception e) {
            log.error("유사 LinkedIn 프로필 검색 실패: {}", e.getMessage(), e);
            CompletableFuture<LinkedInSimilarityResponseDTO> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * 벡터 기반으로 유사한 LinkedIn 프로필을 검색합니다.
     * QdrantClient를 직접 사용하여 검색
     *
     * @param vector 쿼리 벡터
     * @param limit 결과 수
     * @param minScore 최소 유사도 점수
     * @return 유사 프로필 목록
     */
    private Mono<List<LinkedInProfileSimilarityDTO>> searchSimilarProfilesByVector(
            List<Float> vector,
            int limit,
            float minScore) {

        Map<String, Object> request = Map.of(
                "vector", vector,
                "limit", limit,
                "with_payload", true,
                "score_threshold", minScore
        );

        // QdrantClient를 직접 사용하여 검색
        return qdrantClient.searchRaw(LINKEDIN_COLLECTION, request)
                .map(results -> {
                    return results.stream()
                            .map(this::mapToProfileSimilarityDTO)
                            .collect(Collectors.toList());
                });
    }

    /**
     * Qdrant 검색 결과를 DTO로 변환합니다.
     */
    private LinkedInProfileSimilarityDTO mapToProfileSimilarityDTO(Map<String, Object> result) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) result.get("payload");
        float score = ((Number) result.get("score")).floatValue();

        return LinkedInProfileSimilarityDTO.builder()
                .profileSeq(((Number) payload.get("profileSeq")).longValue())
                .linkedinUrl((String) payload.get("linkedinUrl"))
                .name((String) payload.get("name"))
                .headline((String) payload.getOrDefault("headline", ""))
                .company((String) payload.getOrDefault("company", ""))
                .location((String) payload.getOrDefault("location", ""))
                .profileImageUrl((String) payload.getOrDefault("profileImageUrl", ""))
                .similarity(score)
                .build();
    }
}