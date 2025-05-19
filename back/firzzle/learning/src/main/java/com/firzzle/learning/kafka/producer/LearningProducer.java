package com.firzzle.learning.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearningProducer {

    private static final Logger logger = LoggerFactory.getLogger(LearningProducer.class);
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
