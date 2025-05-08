package com.firzzle.llm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.llm.client.*;
import com.firzzle.llm.prompt.*;
import com.firzzle.llm.repository.TestRepository;
import com.firzzle.llm.entity.*;
import com.firzzle.llm.util.*;
import com.firzzle.llm.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final OpenAiClient openAiClient;
    private final QdrantClient qdrantClient;
    private final SummaryPrompt summaryPrompt;
    private final RunnigChatPrompt runningChatPrompt;
    private final EmbeddingService embeddingService;
    private final TestRepository testRepository;

    // 전체 자막 콘텐츠를 요약하는 비동기 함수
    @Async
    public CompletableFuture<String> summarizeContents(SummaryRequest request) {
        String content = request.getContent();
        List<String> scriptLines = Arrays.asList(content.split("\n"));
        long startTime = System.nanoTime();

        log.info("🚀 전체 요약 요청 시작");

        return extractMajorTopics(content)
            .thenCompose(topics -> summarizeByChunks(topics, scriptLines))
            .thenApply(summary -> {
                log.info("✅ 전체 요약 완료 ({}ms)", (System.nanoTime() - startTime) / 1_000_000);
                log.info(summary);

                saveSummaryToDbAndVector(summary, scriptLines); // ✅ 외부 함수로 분리
                return summary;
            })
            .exceptionally(e -> {
                log.error("❌ 전체 요약 처리 중 오류", e);
                return "GPT 응답 중 오류가 발생했습니다.";
            });
    }
    
    private void saveSummaryToDbAndVector(String summary, List<String> scriptLines) {
        // 벡터 저장
        List<Float> vector = embeddingService.embed(summary);
        
//		qdrantClient.upsertVector(QdrantCollections.SCRIPT, uuid.hashCode(), vector, summary)
//		    .doOnError(e -> log.error("업서트 실패", e))
//		    .subscribe(); // 비동기 처리
    }


    // RAG 기반 실시간 대화 응답 생성 (최근 대화 맥락 없이 context만 활용)
    @Async
    public CompletableFuture<String> runningChat(RunningChatRequest request) {
        String question = request.getQuestion();
        List<Float> vector = embeddingService.embed(question);
        CompletableFuture<List<String>> contents = qdrantClient.searchWithPayload(QdrantCollections.SCRIPT, vector, 10, 0.3).toFuture();

        String context = ((Collection<String>) contents).stream().limit(5).collect(Collectors.joining("\n"));
        String Prompt = runningChatPrompt.createPrompt(question, "", context);
        String instruction = runningChatPrompt.createInstruction();

        // TODO: 실제 GPT 호출 필요
        return null;
    }

    // TEST 컬렉션을 기반으로 GPT 답변을 생성하는 테스트용 비동기 함수
    @Async
    public CompletableFuture<String> testGptResponse(String question) {
        long startTime = System.nanoTime();
        log.info("\uD83D\uDE80 GPT 질문 수신: {}", question);

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
                log.info("\u2705 GPT 응답 완료 ({}ms)", (endTime - startTime) / 1_000_000);
                return result;
            })
            .exceptionally(e -> {
                log.error("\u274C GPT 처리 중 오류", e);
                return "GPT 응답 중 오류가 발생했습니다.";
            });
    }

    // 주요 토픽별로 자막을 나누어 요약 요청을 보내는 함수
    @Async
    private CompletableFuture<String> summarizeByChunks(List<MajorTopic> topics, List<String> scriptLines) {
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < topics.size() - 1; i++) {
            MajorTopic topicA = topics.get(i);
            MajorTopic topicB = topics.get(i + 1);
            String start = topicA.getTime();
            String end = topicB.getTime();

            String rawText = ScriptUtils.extractChunkText(scriptLines, start, end);

            if (rawText.strip().isEmpty()) {
                log.warn("⚠️ {}~{} 범위에 자막이 없습니다. 건너뜀", start, end);
                continue;
            }

            String chunkText = String.format(
                "[대주제]: %s\n[시작시각]: %s\n\n%s",
                topicA.getMajorTopic(), start, rawText
            );

            String instruction = summaryPrompt.createInstruction2();
            futures.add(openAiClient.getChatCompletionAsync(instruction, chunkText, ModelType.SUMMARY));
        }

        // 마지막 구간 (끝까지)
        if (!topics.isEmpty()) {
            MajorTopic lastTopic = topics.get(topics.size() - 1);
            String start = lastTopic.getTime();
            String end = "99999";
            String rawText = ScriptUtils.extractChunkText(scriptLines, start, end);

            if (!rawText.strip().isEmpty()) {
                String chunkText = String.format(
                    "[대주제]: %s\n[시작시각]: %s\n\n%s",
                    lastTopic.getMajorTopic(), start, rawText
                );
                futures.add(openAiClient.getChatCompletionAsync(summaryPrompt.createInstruction2(), chunkText, ModelType.SUMMARY));
            }
        }

        return CompletableFuture
            .allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.joining(",\n", "[", "]")));
    }


    // 전체 자막 텍스트에서 주요 대주제를 추출하는 함수
    @Async
    private CompletableFuture<List<MajorTopic>> extractMajorTopics(String content) {
        String instruction = summaryPrompt.createInstruction();

        return openAiClient.getChatCompletionAsync(instruction, content, ModelType.TIMELINE)
            .thenApply(response -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String cleaned = ScriptUtils.extractJsonOnly(response);
                    log.info(cleaned);
                    return mapper.readValue(cleaned, new TypeReference<List<MajorTopic>>() {});
                } catch (Exception e) {
                    log.error("\u274C 대주제 JSON 파싱 실패: {}", response, e);
                    throw new RuntimeException("대주제 파싱 실패", e);
                }
            });
    }

    // 새 콘텐츠를 벡터화하고 Qdrant 및 DB에 저장
    public void register(Integer id, String content) {
        List<Float> vector = embeddingService.embed(content);

        testRepository.save(TestEntity.builder()
            .id(id)
            .content(content)
            .createdAt(LocalDateTime.now())
            .build());

        vector.forEach(v -> {
            if (v == null || v.isNaN() || v.isInfinite()) {
                log.error("\u274C 벡터 값 오류: {}", v);
            }
        });

        qdrantClient.upsertVector(QdrantCollections.TEST, id, vector, content).block();
    }
}
