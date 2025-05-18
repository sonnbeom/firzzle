package com.firzzle.stt.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.stt.dto.LlmRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SttConvertedProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // ✅ 추가

    private static final String TOPIC_NAME = "stt-converted";

    public void sendSttResult(Long contentSeq, String script, String taskId) {
        try {
            LlmRequest request = new LlmRequest(contentSeq, script, taskId);
            String json = objectMapper.writeValueAsString(request);
            System.out.println(json);
            kafkaTemplate.send(TOPIC_NAME, json);
            System.out.println("✅ STT 결과 Kafka 전송 완료");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Kafka 전송용 JSON 직렬화 실패", e);
        }
    }
}