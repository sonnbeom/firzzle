package com.firzzle.llm.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.llm.expert.dto.LinkedInEmbeddingRequestDTO;
import com.firzzle.llm.expert.mapper.LinkedInProfileEmbeddingMapper;
import com.firzzle.llm.expert.service.LinkedInVectorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LinkedIn 프로필 크롤링 이벤트 Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkedInProfileEventConsumer {

    private final ObjectMapper objectMapper;
    private final LinkedInVectorSearchService vectorSearchService;
    private final LinkedInProfileEmbeddingMapper profileEmbeddingMapper;

    /**
     * LinkedIn 프로필 크롤링 이벤트 수신 및 처리
     */
    @KafkaListener(topics = "linkedin-profile-crawled", groupId = "llm-service")
    public void consumeLinkedInProfileEvent(String message) {
        try {
            log.info("LinkedIn 프로필 이벤트 수신: {}", message);

            // 이벤트 메시지 파싱
            Map<String, Object> event = parseEventMessage(message);

            // 이벤트 타입 확인
            String eventType = (String) event.get("type");
            if (!"LINKEDIN_BATCH_PROCESSED".equals(eventType)) {
                log.warn("지원하지 않는 이벤트 타입: {}", eventType);
                return;
            }

            // 프로필 ID 목록 추출
            List<Integer> rawProfileSeqs = (List<Integer>) event.get("profileSeqs");
            List<Long> profileSeqs = rawProfileSeqs.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            String keyword = (String) event.get("keyword");
            int count = (int) event.get("count");

            log.info("LinkedIn 프로필 임베딩 처리 시작: 키워드={}, 프로필 수={}", keyword, count);

            // 프로필 정보를 DB에서 조회하여 임베딩 처리
            processProfilesFromDB(profileSeqs);

        } catch (Exception e) {
            log.error("LinkedIn 프로필 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * Kafka 메시지를 Map으로 파싱
     */
    private Map<String, Object> parseEventMessage(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, Map.class);
    }

    /**
     * DB에서 프로필 정보를 조회하여 처리
     */
    private void processProfilesFromDB(List<Long> profileSeqs) {
        int totalProfiles = profileSeqs.size();
        int successCount = 0;
        int failCount = 0;
        List<Long> failedProfiles = new ArrayList<>();

        log.info("DB에서 {} 개의 프로필 정보 조회 시작", totalProfiles);

        // 프로필 정보 조회 및 임베딩 처리
        for (Long profileSeq : profileSeqs) {
            try {
                // DB에서 프로필 정보 조회
                LinkedInEmbeddingRequestDTO profile = profileEmbeddingMapper.selectProfileForEmbedding(profileSeq);

                if (profile == null) {
                    log.warn("프로필 정보 없음: profileSeq={}", profileSeq);
                    failedProfiles.add(profileSeq);
                    failCount++;
                    continue;
                }

                // 스킬 정보 조회
                List<String> skills = profileEmbeddingMapper.selectSkillsByProfileSeq(profileSeq);
                profile.setSkills(skills);

                // 임베딩 생성 및 저장
                boolean success = vectorSearchService.saveProfileToVectorDb(profile);

                if (success) {
                    log.info("프로필 임베딩 완료: profileSeq={}, name={}", profileSeq, profile.getName());
                    successCount++;
                } else {
                    log.warn("프로필 임베딩 실패: profileSeq={}", profileSeq);
                    failedProfiles.add(profileSeq);
                    failCount++;
                }

            } catch (Exception e) {
                log.error("프로필 {} 처리 중 오류: {}", profileSeq, e.getMessage(), e);
                failedProfiles.add(profileSeq);
                failCount++;
            }
        }

        log.info("LinkedIn 프로필 임베딩 처리 완료: 총={}, 성공={}, 실패={}",
                totalProfiles, successCount, failCount);

        if (!failedProfiles.isEmpty()) {
            log.warn("임베딩 실패한 프로필: {}", failedProfiles);
        }
    }
}