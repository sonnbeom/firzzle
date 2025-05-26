package com.firzzle.stt.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.stt.dto.LlmRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SttConvertedProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // ✅ 추가

    private static final String TOPIC_NAME = "stt-converted";

    public void sendSttResult(LlmRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            log.info(json);
            kafkaTemplate.send(TOPIC_NAME, json);
            log.info("✅ STT 결과 Kafka 전송 완료");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Kafka 전송용 JSON 직렬화 실패", e);
        }
    }
}