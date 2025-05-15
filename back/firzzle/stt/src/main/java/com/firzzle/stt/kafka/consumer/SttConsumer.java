package com.firzzle.stt.kafka.consumer;

import com.firzzle.stt.kafka.producer.SttConvertedProducer;
import com.firzzle.stt.service.SttService;
import com.firzzle.stt.dto.LlmRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SttConsumer {

    private final SttConvertedProducer sttProducer;
    private final SttService sttService;

    // 메시지 형식: "12345|https://youtube.com/watch?v=abcde"
    @KafkaListener(topics = "to-stt", groupId = "stt-group")
    public void consumeFromLearning(String message) {
        log.info("📥 Received raw message: {}", message);

        try {
            String[] parts = message.split("\\|", 2); // 구분자 | 기준으로 나눔
            Long userSeq = Long.parseLong(parts[0]);
            String url = parts[1];

            log.info("🔍 Parsed userSeq: {}, url: {}", userSeq, url);

            LlmRequest result = sttService.transcribeFromYoutube(userSeq,url);
            sttProducer.sendSttResult(result.getContentSeq(), result.getScript());
        } catch (Exception e) {
            log.error("❌ STT 처리 중 오류 또는 메시지 포맷 문제", e);
        }
    }
}
