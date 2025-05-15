package com.firzzle.llm.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public CompletableFuture<String> learningChat(Long contentSeq, learningChatRequestDTO request, String userId) {
        String question = request.getQuestion();
        logger.info("📥 [learningChat 시작] contentSeq={}, userId={}, question={}", contentSeq, userId, question);

        List<Float> vector = embeddingService.embed(question);

        return ragService.searchTopPayloadsByContentSeq(QdrantCollections.SCRIPT, vector, contentSeq)
                .toFuture()
                .thenCompose(contents -> {
                    logger.debug("🔍 [벡터 검색 결과] top contents count={}", contents.size());

                    String context = contents.stream().limit(5).collect(Collectors.joining("\n"));

                    if (context.isEmpty()) {
                        logger.info("⚠️ [context 없음] 기본 응답 반환");
                        return CompletableFuture.completedFuture(
                                "해당 내용은 영상에서 언급되지 않았어요. 다른 질문이 있으신가요? 궁금한 점을 말씀해 주시면 최대한 도와드릴게요!"
                        );
                    }

                    ChatCompletionRequest chatRequest = promptFactory.createLearningChatRequest(question, context);
                    logger.debug("📤 [OpenAI 요청 전] 생성된 prompt context 일부=\n{}", context.substring(0, Math.min(context.length(), 300)));

                    return openAiClient.getChatCompletionAsync(chatRequest);
                })
                .exceptionally(e -> {
                    logger.error("❌ learningChat 처리 중 오류", e);
                    return "답변 생성 중 오류가 발생했습니다.";
                });
    }
}
