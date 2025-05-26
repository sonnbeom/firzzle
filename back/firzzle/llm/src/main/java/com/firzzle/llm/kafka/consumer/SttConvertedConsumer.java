package com.firzzle.llm.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.llm.dto.LlmRequestDTO;
import com.firzzle.llm.service.RegistrationService;
import com.firzzle.llm.sse.SseEmitterRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SttConvertedConsumer {

    private final RegistrationService llmService;
    private final ObjectMapper objectMapper;
    private final SseEmitterRepository sseEmitterRepository; // SseEmitterRepository 주입 추가

    @KafkaListener(topics = "stt-converted", groupId = "firzzle")
    public void consume(String message) {
        String preview = message.length() > 100 ? message.substring(0, 100) + "..." : message;
        log.info("✅ 수신한 텍스트(미리보기): " + preview);

        try {
            // ✅ JSON 문자열 → LlmRequest 객체로 역직렬화
            LlmRequestDTO requestObj = objectMapper.readValue(message, LlmRequestDTO.class);

            // userContentSeq가 null인 경우 STT 처리 중 오류가 발생한 것으로 간주
            if (requestObj.getUserContentSeq() == null) {
                log.error("❌ userContentSeq가 null입니다. STT 모듈에서 오류 발생. message=" + message);

                // taskId가 있다면 SSE로 오류 전송
                if (requestObj.getTaskId() != null) {
                    String errorMessage = "STT 처리 중 오류가 발생했습니다.";
                    if(requestObj.getErrorMessage()!=null && !requestObj.getErrorMessage().isEmpty()) {
                        errorMessage = requestObj.getErrorMessage();
                    }
                    sseEmitterRepository.completeWithError(requestObj.getTaskId(), errorMessage);
                    log.error("❌ SSE 오류 전송 완료: taskId={}, message={}", requestObj.getTaskId(), errorMessage);
                }

                return; // 처리 중단
            }

            llmService.summarizeContents(requestObj)
                    .thenAccept(result -> {
                        log.info("✅ 요약 완료");
                    })
                    .exceptionally(e -> {
                        // 로그만 기록하고 SSE 에러 처리는 서비스에 맡깁니다
                        log.error("❌ 요약 처리 중 오류: {} (SSE 에러는 서비스에서 처리됨)", e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            log.error("❌ Kafka 메시지 파싱 실패: " + e.getMessage(), e);

            // 파싱 단계에서 오류가 발생하면 taskId를 추출할 수 없으므로
            // 이 경우 처리 불가능 - 로그만 남김
        }
    }
}