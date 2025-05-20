package com.firzzle.llm.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.llm.dto.LlmRequestDTO;
import com.firzzle.llm.service.RegistrationService;
import lombok.RequiredArgsConstructor;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SttConvertedConsumer {

    private final RegistrationService llmService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "stt-converted", groupId = "firzzle")
    public void consume(String message) {
        String preview = message.length() > 100 ? message.substring(0, 100) + "..." : message;
        System.out.println("✅ 수신한 텍스트(미리보기): " + preview);

        try {
            // ✅ JSON 문자열 → LlmRequest 객체로 역직렬화
            LlmRequestDTO requestObj = objectMapper.readValue(message, LlmRequestDTO.class);
            if (requestObj.getUserContentSeq() == null) {
                System.out.println("❌ userContentSeq가 null입니다. 메시지 처리 중단. message=" + message);
                return; // ❌ 처리 중단
            }
            LlmRequestDTO request = new LlmRequestDTO();
            request.setUserContentSeq(requestObj.getUserContentSeq());
            request.setContentSeq(requestObj.getContentSeq());
            request.setScript(requestObj.getScript()); // 🎯 스크립트만 추출
            request.setTaskId(requestObj.getTaskId()); // sse taskId

            llmService.summarizeContents(request)
                .thenAccept(result -> {
                    System.out.println("✅ 요약 완료");
                })
                .exceptionally(e -> {
                    System.err.println("❌ 요약 처리 중 오류: " + e.getMessage());
                    return null;
                });

        } catch (Exception e) {
            System.err.println("❌ Kafka 메시지 파싱 실패: " + e.getMessage());
        }
    }
}

