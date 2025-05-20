package com.firzzle.llm.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.llm.dto.SnapReviewRequestDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapReviewProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // ✅ 추가

    private static final String TOPIC_NAME = "snap-review";

    /**
     * 스냅리뷰 생성 요청을을 Kafka로 전송합니다.
     * @param contentSeq 콘텐츠 번호
     * @param timeline 스냅리뷰 타임라인
     */
    public void sendSnapReviewRequest(Long contentSeq, List<String> timeline) {
        try {
            SnapReviewRequestDTO snapReview = new SnapReviewRequestDTO(contentSeq, timeline);
            String json = objectMapper.writeValueAsString(snapReview);
            log.info("📤 SnapReview JSON: {}", json);
            kafkaTemplate.send(TOPIC_NAME, json);
            log.info("✅ SnapReview Kafka 전송 완료");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Kafka 전송용 JSON 직렬화 실패", e);
        }
    }

}