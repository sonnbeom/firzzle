package com.firzzle.llm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import com.firzzle.llm.dto.ModelType;

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
    
    @Value("${spring.ai.openai.runningchat.model}")
    private String runningChatModel;

    @Async("llmExecutor")
    public CompletableFuture<String> getChatCompletionAsync(
            String systemMessage,
            String userPrompt,
            ModelType type
    ) {
        String model = switch (type) {
            case TIMELINE -> timelineModel;
            case SUMMARY -> summaryModel;
            case RUNNINGCHAT -> runningChatModel;
        };

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemMessage != null && !systemMessage.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemMessage));
        }
        messages.add(Map.of("role", "user", "content", userPrompt));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        Map<?, ?> choice = ((List<Map<?, ?>>) response.get("choices")).get(0);
                        Map<?, ?> message = (Map<?, ?>) choice.get("message");
                        log.info("✅ OpenAI 응답 성공 ({}): {}", type, model);
                        return (String) message.get("content");
                    } catch (Exception e) {
                        log.error("❌ 응답 파싱 실패 ({}): {}", type, e.getMessage());
                        return "응답 파싱 오류";
                    }
                })
                .onErrorResume(e -> {
                    log.error("❌ OpenAI 요청 실패 ({}): {}", type, e.getMessage());
                    return Mono.just("OpenAI 응답 처리 중 오류가 발생했습니다.");
                })
                .toFuture();
    }
}
