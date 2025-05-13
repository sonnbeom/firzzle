package com.firzzle.llm.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.firzzle.llm.client.OpenAiClient;
import com.firzzle.llm.dto.ChatCompletionRequest;
import com.firzzle.llm.dto.learningChatRequestDTO;
import com.firzzle.llm.prompt.PromptFactory;
import com.firzzle.llm.util.QdrantCollections;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class learningChatService {
    private final OpenAiClient openAiClient;
    private final EmbeddingService embeddingService;
    private final RagService ragService;
    private final PromptFactory promptFactory;

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
	
	 // RAG 기반 실시간 대화 응답 생성 (최근 대화 맥락 없이 context만 활용)
    @Async
    public CompletableFuture<String> runningChat(Long contentSeq,learningChatRequestDTO request, String userId) {
        String question = request.getQuestion();
        List<Float> vector = embeddingService.embed(question);

        return ragService.searchTopPayloadsByContentSeq(QdrantCollections.SCRIPT, vector, contentSeq)
                .toFuture()
                .thenCompose(contents -> {
                	String context = contents.stream().limit(5).collect(Collectors.joining("\n"));

                    // ✅ context가 없으면 바로 기본 응답 반환
                    if (context.isEmpty()) {
                        return CompletableFuture.completedFuture(
                                "해당 내용은 영상에서 언급되지 않았어요. 다른 질문이 있으신가요? 궁금한 점을 말씀해 주시면 최대한 도와드릴게요!"
                        );
                    }

                    ChatCompletionRequest chatRequest =
                            promptFactory.createRunningChatRequest(question, context);

                    return openAiClient.getChatCompletionAsync(chatRequest);
                })
                .exceptionally(e -> {
                    logger.error("❌ runningChat 처리 중 오류", e);
                    return "답변 생성 중 오류가 발생했습니다.";
                });
    }

}
