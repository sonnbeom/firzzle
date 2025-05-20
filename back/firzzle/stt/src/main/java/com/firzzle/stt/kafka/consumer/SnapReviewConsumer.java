package com.firzzle.stt.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.stt.dto.SnapReviewRequestDTO;
import com.firzzle.stt.service.SnapReviewService;
import com.firzzle.stt.service.SttService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapReviewConsumer {

    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(SttService.class);
    private final SnapReviewService snapReviewService;

    @KafkaListener(topics = "snap-review", groupId = "snap-review-group")
    public void consumeSnapReview(ConsumerRecord<String, String> record) {
        String message = record.value();
        log.info("📥 SnapReview 메시지 수신: {}", message);

        try {
            SnapReviewRequestDTO dto = objectMapper.readValue(message, SnapReviewRequestDTO.class);
            handleSnapReview(dto);
        } catch (Exception e) {
            logger.error("❌ SnapReview 메시지 처리 중 오류", e);
        }
    }

    private void handleSnapReview(SnapReviewRequestDTO dto) {
        Long contentSeq = dto.getContentSeq();
        try {
            logger.info("🛠️ SnapReview 처리 시작 - contentSeq: {}, timeline: {}", contentSeq, dto.getTimeline());
            snapReviewService.generateSnapReview(contentSeq, dto.getTimeline());
            logger.info("✅ SnapReview 처리 완료 - contentSeq: {}", contentSeq);
        } catch (Exception e) {
            logger.error("❌ SnapReview 처리 실패 - contentSeq: {}", contentSeq, e);
        }
        
    }
}
