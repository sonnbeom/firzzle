package com.firzzle.stt.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class SttConvertedProducer {
	
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC_NAME = "stt-converted";

    public void sendSttResult(String message) {
        kafkaTemplate.send(TOPIC_NAME, message);
        System.out.println("✅ STT 결과 Kafka 전송 완료");
    }
}
