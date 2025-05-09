package com.firzzle.llm.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendToLearning(String message) {
        log.info("request = {} ", message);
        kafkaTemplate.send("from-llm", message);
    }
}
