package com.firzzle.llm.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningContentProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "learning-content-ready";

    public void sendLearningContent(String message) {
        try {
            kafkaTemplate.send(TOPIC, message);
            log.info("✅ Kafka로 학습 콘텐츠 전송 완료");
        } catch (Exception e) {
            log.error("❌ Kafka 학습 콘텐츠 전송 실패", e);
        }
    }
}
