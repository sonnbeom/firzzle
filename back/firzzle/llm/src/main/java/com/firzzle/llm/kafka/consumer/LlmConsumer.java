package com.firzzle.llm.kafka.consumer;

import com.firzzle.llm.kafka.producer.LlmProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmConsumer {

    private final LlmProducer llmProducer; // LLM 처리 후 Learning 서버로 메시지 보낼 것

    @KafkaListener(topics = "to-llm", groupId = "llm-group")
    public void consumeFromLearningOrStt(String message) {
        log.info("📥 Received message for LLM processing: {}", message);

        // TODO: 여기서 LLM 모델 호출해서 결과 생성하기
        String processedMessage = message + " - processed by LLM";

        llmProducer.sendToLearning(processedMessage); // 처리 결과를 Learning 서버로 전송
    }
}