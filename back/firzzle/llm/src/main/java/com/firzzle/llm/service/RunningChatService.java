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
import com.firzzle.llm.client.QdrantClient;
import com.firzzle.llm.dto.ModelType;
import com.firzzle.llm.dto.RunningChatRequest;
import com.firzzle.llm.entity.TestEntity;
import com.firzzle.llm.prompt.RunnigChatPrompt;
import com.firzzle.llm.repository.TestRepository;
import com.firzzle.llm.util.QdrantCollections;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RunningChatService {
    private final OpenAiClient openAiClient;
    private final QdrantClient qdrantClient;
    private final RunnigChatPrompt runningChatPrompt;
    private final EmbeddingService embeddingService;
    private final RagService ragService;
    private final TestRepository testRepository;

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
	
	 // RAG 기반 실시간 대화 응답 생성 (최근 대화 맥락 없이 context만 활용)
    @Async
    public CompletableFuture<String> runningChat(RunningChatRequest request) {
        String question = request.getQuestion();
        List<Float> vector = embeddingService.embed(question);
        Long contentSeq = request.getContentSeq();
        return ragService.searchTopPayloadsByContentSeq(QdrantCollections.SCRIPT, vector, contentSeq)
                .toFuture()
                .thenCompose(contents -> {
                    String context = contents.stream().limit(5).collect(Collectors.joining("\n"));
                    String prompt = runningChatPrompt.createPrompt(question, "", context);
                    String instruction = runningChatPrompt.createInstruction();

                    return openAiClient.getChatCompletionAsync(instruction, prompt, ModelType.RUNNINGCHAT);
                })
                .exceptionally(e -> {
                    logger.error("❌ runningChat 처리 중 오류", e);
                    return "답변 생성 중 오류가 발생했습니다.";
                });
    }

    // TEST 컬렉션을 기반으로 GPT 답변을 생성하는 테스트용 비동기 함수
    @Async
    public CompletableFuture<String> testGptResponse(String question) {
        long startTime = System.nanoTime();
        logger.info("\uD83D\uDE80 GPT 질문 수신: {}", question);

        return CompletableFuture.supplyAsync(() -> embeddingService.embed(question))
            .thenCompose(vector -> qdrantClient.searchWithPayload(QdrantCollections.TEST, vector, 10, 0.3).toFuture())
            .thenCompose(contents -> {
                String context = contents.stream().limit(5).collect(Collectors.joining("\n"));
                String prompt = "다음 문맥을 참고하여 질문에 답해주세요:\n" + context + "\n\n질문: " + question;
                String instruction = "문맥을 기반으로 명확하고 간결하게 답변해주세요.";

                return openAiClient.getChatCompletionAsync(instruction, prompt, ModelType.SUMMARY);
            })
            .thenApply(result -> {
                long endTime = System.nanoTime();
                logger.info("\u2705 GPT 응답 완료 ({}ms)", (endTime - startTime) / 1_000_000);
                return result;
            })
            .exceptionally(e -> {
                logger.error("\u274C GPT 처리 중 오류", e);
                return "GPT 응답 중 오류가 발생했습니다.";
            });
    }

    

    

    // 새 콘텐츠를 벡터화하고 Qdrant 및 DB에 저장
    public void register(Long id, String content) {
        List<Float> vector = embeddingService.embed(content);

        testRepository.save(TestEntity.builder()
            .id(id)
            .content(content)
            .createdAt(LocalDateTime.now())
            .build());

        vector.forEach(v -> {
            if (v == null || v.isNaN() || v.isInfinite()) {
                logger.error("\u274C 벡터 값 오류: {}", v);
            }
        });

        qdrantClient.upsertVector(QdrantCollections.TEST, id, vector, content).block();
    }
}
