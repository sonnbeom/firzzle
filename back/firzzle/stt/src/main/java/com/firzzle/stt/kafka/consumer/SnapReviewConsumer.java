package com.firzzle.stt.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.stt.dto.SnapReviewRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapReviewConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "snap-review", groupId = "snap-review-group")
    public void consumeSnapReview(ConsumerRecord<String, String> record) {
        String message = record.value();
        log.info("📥 SnapReview 메시지 수신: {}", message);

        try {
            SnapReviewRequestDTO dto = objectMapper.readValue(message, SnapReviewRequestDTO.class);
            handleSnapReview(dto);
        } catch (Exception e) {
            log.error("❌ SnapReview 메시지 처리 중 오류", e);
        }
    }

    private void handleSnapReview(SnapReviewRequestDTO dto) {
        log.info("🛠️ SnapReview 처리 시작 - contentSeq: {}, timeline: {}",
                dto.getContentSeq(), dto.getTimeline());

        // ✅ 실제 처리 로직은 여기에 구현
        // 예: 썸네일 캡처, 리뷰 생성, DB 저장 등
    }
}
