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

    private static final
    Logger logger = LoggerFactory.getLogger(LearningProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.content-analysis}")
    private String contentAnalysisTopic;

    public void sendToStt(String message) {
        log.info("request, {}", message);
        kafkaTemplate.send("to-stt", message);
    }

    public void sendToLlm(String message) {
        log.info("request, {}", message);
        kafkaTemplate.send("to-llm", message);
    }

    /**
     * SSE 지원 콘텐츠 분석 요청 메시지 전송 (외부에서 taskId를 제공받는 메서드)
     *
     * @param uuid 사용자 ID
     * @param url 컨텐츠 URL
     * @param contentSeq 컨텐츠 일련번호
     * @param taskId 작업 추적 ID
     */
    public void sendContentAnalysisWithTaskId(String uuid, String url, Long contentSeq, String taskId) {
        try {
            // JSON 형식 메시지 생성
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("uuid", uuid);
            messageMap.put("url", url);
            messageMap.put("contentSeq", contentSeq);
            messageMap.put("taskId", taskId);
            messageMap.put("timestamp", System.currentTimeMillis());
            messageMap.put("type", "summary");

            // JSON 문자열로 변환
            String jsonMessage = objectMapper.writeValueAsString(messageMap);

            // 메시지 키로 taskId 사용
            logger.info("요약 작업 요청 메시지 전송: taskId={}, contentSeq={}", taskId, contentSeq);
            kafkaTemplate.send(contentAnalysisTopic, taskId, jsonMessage);
        } catch (Exception e) {
            logger.error("Kafka 요약 요청 메시지 전송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Kafka 메시지 전송 실패", e);
        }
    }

}
