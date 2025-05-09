package com.firzzle.llm.kafka.consumer;

import com.firzzle.llm.dto.SummaryRequest;
import com.firzzle.llm.service.LlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SttConvertedConsumer {

    private final LlmService llmService; // ✅ 이제 WebClient는 필요 없음!

    @KafkaListener(topics = "stt-converted", groupId = "firzzle")
    public void consume(String message) {
        String preview = message.length() > 100 ? message.substring(0, 100) + "..." : message;
        System.out.println("✅ 수신한 텍스트(미리보기): " + preview);

        SummaryRequest request = new SummaryRequest();
        request.setContent(message); // ✅ 원본 전체를 SummaryRequest에 세팅

        // ✅ 요약 서비스 직접 호출
        llmService.summarizeContents(request)
            .thenAccept(result -> {
                System.out.println("✅ 요약 완료: ");
            })
            .exceptionally(e -> {
                System.err.println("❌ 요약 처리 중 오류: " + e.getMessage());
                return null;
            });
    }
}
