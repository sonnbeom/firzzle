package com.firzzle.stt.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SttProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendToLlm(String message) {
        kafkaTemplate.send("to-llm", message);
    }
}