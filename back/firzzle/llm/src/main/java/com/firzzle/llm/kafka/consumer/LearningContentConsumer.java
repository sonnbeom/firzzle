package com.firzzle.llm.kafka.consumer;


import com.firzzle.llm.service.LlmService;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class LearningContentConsumer {
    private final LlmService llmService;


//    @KafkaListener(topics = "learning-content-ready", groupId = "firzzle")
//    public void consume(String message) {
//        String preview = message.length() > 100 ? message.substring(0, 100) + "..." : message;
//        System.out.println("✅ 수신한 텍스트(미리보기): " + preview);
//
//        // ✅ 퀴즈 생성 서비스 직접 호출
//        llmService.generateQuiz(message)
//            .thenAccept(result -> {
//                System.out.println("✅ 퀴즈 생성 완료: " + result);
//            })
//            .exceptionally(e -> {
//                System.err.println("❌ 퀴즈 처리 중 오류: " + e.getMessage());
//                return null;
//            });
//
//        // ✅ 질문 서비스 직접 호출
//        llmService.generateQuestion(message)
//            .thenAccept(result -> {
//                System.out.println("✅ 질문 생성 완료: " + result);
//            })
//            .exceptionally(e -> {
//                System.err.println("❌ 질문 처리 중 오류: " + e.getMessage());
//                return null;
//            });
//    }

}
