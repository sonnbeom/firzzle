package com.firzzle.learning.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearningProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendToStt(String message) {
        log.info("request, {}", message);
        kafkaTemplate.send("to-stt", message);
    }

    public void sendToLlm(String message) {
        log.info("request, {}", message);
        kafkaTemplate.send("to-llm", message);
    }
}
