package com.firzzle.llm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.firzzle.llm.domain.ModelType;
import com.firzzle.llm.dto.ChatCompletionRequestDTO;

import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private WebClient webClient;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private void setBaseUrl(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
            .resolver(DefaultAddressResolverGroup.INSTANCE); // 시스템 DNS 사용

        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Value("${spring.ai.openai.timeline.model}")
    private String timelineModel;

    @Value("${spring.ai.openai.summary.model}")
    private String summaryModel;
    
    @Value("${spring.ai.openai.learningchat.model}")
    private String learningChatModel;

    @Async("llmExecutor")
    public CompletableFuture<String> getChatCompletionAsync(ChatCompletionRequestDTO chatCompletionRequest) {
        // ✅ 모델 선택
        String model = switch (chatCompletionRequest.getModelType()) {
            case TIMELINE -> timelineModel;
            case SUMMARY -> summaryModel;
            case LEARNINGCHAT -> learningChatModel;
        };

        // ✅ 메시지 구성
        List<Map<String, String>> messages = new ArrayList<>();
        String systemMessage = chatCompletionRequest.getSystemMessage();
        String userPrompt = chatCompletionRequest.getUserPrompt();

        if (systemMessage != null && !systemMessage.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemMessage));
        }
        messages.add(Map.of("role", "user", "content", userPrompt));

        // ✅ 파라미터 추출 (기본값 포함)
        double temperature = Optional.ofNullable(chatCompletionRequest.getTemperature()).orElse(0.7);
        double topP = Optional.ofNullable(chatCompletionRequest.getTopP()).orElse(1.0);
        int maxTokens = Optional.ofNullable(chatCompletionRequest.getMaxTokens()).orElse(4096);

        // ✅ 요청 본문 구성
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", temperature);
        body.put("top_p", topP);
        body.put("max_tokens", maxTokens);

        // ✅ 요청 전송 및 응답 처리
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        List<Map<?, ?>> choices = (List<Map<?, ?>>) response.get("choices");
                        if (choices == null || choices.isEmpty()) {
                            log.warn("⚠️ OpenAI 응답에 choices 없음");
                            return "OpenAI 응답 없음";
                        }
                        Map<?, ?> message = (Map<?, ?>) choices.get(0).get("message");
                        String content = (String) message.get("content");

                        log.info("✅ OpenAI 응답 성공 [model={}, temp={}, top_p={}, tokens={}]", model, temperature, topP, maxTokens);
                        return content;

                    } catch (Exception e) {
                        log.error("❌ 응답 파싱 실패 [model={}]: {}", model, e.getMessage());
                        return "응답 파싱 오류";
                    }
                })
                .onErrorResume(e -> {
                    log.error("❌ OpenAI 요청 실패 [model={}]: {}", model, e.getMessage());
                    return Mono.just("OpenAI 응답 처리 중 오류가 발생했습니다.");
                })
                .toFuture();
    }

}
