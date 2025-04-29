package com.firzzle.learning.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LearningConsumer {

    @KafkaListener(topics = "from-llm", groupId = "learning-group")
    public void consumeFromLlm(String message) {
        log.info("📥 Received from LLM: {}", message);
    }
}