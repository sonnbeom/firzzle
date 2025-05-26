package com.firzzle.learning.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.utils.DateUtil;
import com.firzzle.learning.expert.event.LinkedInProfileCrawledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearningProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendToStt(String message) {
        log.info("request, {}", message);
        kafkaTemplate.send("to-stt", message);
    }

    public void sendToLlm(String message) {
        log.info("request, {}", message);
        kafkaTemplate.send("to-llm", message);
    }

    /**
     * LinkedIn 프로필 일괄 처리 신호를 Kafka로 전송합니다.
     *
     * @param profileSeqs 처리된 프로필 일련번호 목록
     * @param keyword 검색 키워드
     */
    public void sendLinkedInBatchSignal(List<Long> profileSeqs, String keyword) {
        try {
            // 간단한 시그널 메시지 구성
            Map<String, Object> signal = new HashMap<>();
            signal.put("type", "LINKEDIN_BATCH_PROCESSED");
            signal.put("profileSeqs", profileSeqs);
            signal.put("keyword", keyword);
            signal.put("timestamp", FormatDate.getDate("yyyyMMddHHmmss"));
            signal.put("count", profileSeqs.size());

            String message = objectMapper.writeValueAsString(signal);
            log.info("LinkedIn 프로필 일괄 처리 신호 전송: {}", message);

            // 'linkedin-profile-crawled' 토픽으로 메시지 전송
            kafkaTemplate.send("linkedin-profile-crawled", message);
        } catch (Exception e) {
            log.error("LinkedIn 일괄 처리 신호 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}