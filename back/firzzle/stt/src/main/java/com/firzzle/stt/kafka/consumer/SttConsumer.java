package com.firzzle.stt.kafka.consumer;

import com.firzzle.stt.kafka.producer.SttProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SttConsumer {

    private final SttProducer sttProducer; // STT 처리 후 LLM 서버로 메시지 보낼 것

    @KafkaListener(topics = "to-stt", groupId = "stt-group")
    public void consumeFromLearning(String message) {
        log.info("📥 Received message for STT processing: {}", message);

        // TODO: 여기서 음성 → 텍스트 변환 로직 추가
        String processedMessage = message.toUpperCase(); // 예시: 텍스트 변환한 결과

        sttProducer.sendToLlm(processedMessage); // 처리 결과를 LLM 서버로 전송
    }
}