package com.firzzle.stt.kafka.consumer;

import com.firzzle.stt.kafka.producer.SttConvertedProducer;
import com.firzzle.stt.service.SttService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SttConsumer {
    private final SttService sttService;

    @KafkaListener(topics = "to-stt", groupId = "stt-group")
    public void consumeFromLearning(String message) {
        log.info("📥 Received raw message: {}", message);
        handleMessageAsync(message);
    }

    @Async("taskExecutor") // application에 taskExecutor 빈 등록 필요
    public void handleMessageAsync(String message) {
        try {
            // 1) 구분자를 3개로 자르기
            String[] parts = message.split("\\|", 3);

            // 2) 최소 3개가 아닐 경우 포맷 오류 처리
            if (parts.length < 3) {
                log.error("❌ STT 메시지 포맷 오류: 예상된 필드 3개, 실제 필드 수={}", parts.length);
                return;
            }
            
            String uuid = parts[0];
            String url = parts[1];
            String taskId = parts[2];

            log.info("🔍 Parsed uuid: {}, url: {}", uuid, url);

            sttService.transcribeFromYoutube(uuid, url, taskId); // 비동기 처리 (sendSttResult 포함)
        } catch (Exception e) {
            log.error("❌ STT 처리 중 오류 또는 메시지 포맷 문제", e);
        }
    }
}
