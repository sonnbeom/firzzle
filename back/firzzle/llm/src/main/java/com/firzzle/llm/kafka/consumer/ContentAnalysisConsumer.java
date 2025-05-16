package com.firzzle.llm.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.llm.dto.LlmRequestDTO;
import com.firzzle.llm.service.RegistrationService;
import com.firzzle.llm.sse.SseEmitterRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka 메시지 소비자
 */
@Component
public class ContentAnalysisConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ContentAnalysisConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private SseEmitterRepository sseEmitterRepository;

    /**
     * Kafka 메시지 소비
     */
    @KafkaListener(topics = "${app.kafka.topic.content-analysis}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            logger.info("Kafka 메시지 수신: key={}, partition={}, offset={}",
                    record.key(), record.partition(), record.offset());

            // JSON 메시지를 파싱
            JsonNode message = objectMapper.readTree(record.value());

            // 필요한 데이터 추출
            String taskId = message.path("taskId").asText();
            String contentId = message.path("contentId").asText();
            String content = message.path("content").asText();

            logger.info("요약 작업 요청 수신: taskId={}, contentId={}", taskId, contentId);

            // SSE 클라이언트에 작업 시작 알림
            if (sseEmitterRepository.exists(taskId)) {
                sseEmitterRepository.sendToClient(taskId, "start",
                        Map.of(
                                "message", "요약 작업을 시작합니다.",
                                "contentId", contentId,
                                "timestamp", System.currentTimeMillis()
                        ));
            }

            // LlmRequestDTO 생성
            LlmRequestDTO request = new LlmRequestDTO();
            request.setTaskId(taskId);
            request.setContentId(contentId);
            request.setContent(content);

            // 비동기 요약 처리 시작
            registrationService.summarizeContents(request);

        } catch (Exception e) {
            logger.error("Kafka 메시지 처리 중 오류: {}", e.getMessage(), e);

            // 오류 발생 시 taskId 추출 시도
            try {
                JsonNode message = objectMapper.readTree(record.value());
                String taskId = message.path("taskId").asText();

                if (sseEmitterRepository.exists(taskId)) {
                    sseEmitterRepository.completeWithError(taskId,
                            "메시지 처리 중 오류 발생: " + e.getMessage());
                }
            } catch (Exception ex) {
                logger.error("오류 처리 중 추가 예외 발생: {}", ex.getMessage());
            }
        }
    }
}