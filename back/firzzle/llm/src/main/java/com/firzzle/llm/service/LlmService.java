package com.firzzle.llm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.llm.client.*;
import com.firzzle.llm.dto.*;
import com.firzzle.llm.prompt.*;
import com.firzzle.llm.repository.TestRepository;
import com.firzzle.llm.entity.*;
import com.firzzle.llm.util.*;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LlmService {

    private final OpenAiClient openAiClient;
    private final QdrantClient qdrantClient;
    private final SummaryPrompt summaryPrompt;
    private final RunnigChatPrompt runningChatPrompt;
    private final EmbeddingService embeddingService;
    private final OxQuizService oxQuizService;
    private final RagService ragService;
    private final SummaryService summaryService;
    private final DescriptiveQuiz descriptiveQuiz;
    private final TestRepository testRepository;

    private static final Logger logger = LoggerFactory.getLogger(LlmService.class);
    // 전체 자막 콘텐츠를 요약하는 비동기 함수
    @Async
    public CompletableFuture<String> summarizeContents(LlmRequest request) {
        String content = request.getScript();
        List<String> scriptLines = Arrays.asList(content.split("\n"));

        logger.info("🚀 전체 요약 시작");

        return extractTimeLine(content)
            .thenCompose(timelines -> summarizeByChunks(timelines, scriptLines)) // List<ContentBlock>
            .thenApply(blocks -> {
                blocks.forEach(block -> logger.info("🎯 요약 블록: {}", block.getTitle()));
                saveBlock(request.getContentSeq(), blocks); // ✅ List<ContentBlock> 저장
                return "✅ 요약 및 저장 완료: " + blocks.size() + "개";
            })
            .exceptionally(e -> {
                logger.error("❌ 전체 요약 처리 중 오류", e);
                return "GPT 응답 중 오류가 발생했습니다.";
            });
    }

    
    // 전체 자막 텍스트에서 주요 대주제를 추출하는 함수
    @Async
    private CompletableFuture<List<TimeLine>> extractTimeLine(String content) {
        String instruction = summaryPrompt.createInstruction();
    
        return openAiClient.getChatCompletionAsync(instruction, content, ModelType.TIMELINE)
                .thenApply(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String cleaned = ScriptUtils.extractJsonOnly(response);
                        return mapper.readValue(cleaned, new TypeReference<List<TimeLine>>() {});
                    } catch (Exception e) {
                        logger.error("❌ 대주제 JSON 파싱 실패: {}", response, e);
                        throw new RuntimeException("대주제 파싱 실패", e);
                    }
                });
    }
    
    // 주요 토픽별로 자막을 나누어 요약 요청을 보내는 함수
    @Async
    private CompletableFuture<List<ContentBlock>> summarizeByChunks(List<TimeLine> topics, List<String> scriptLines) {
        List<CompletableFuture<List<ContentBlock>>> futures = new ArrayList<>();

        for (int i = 0; i < topics.size(); i++) {
            String start = topics.get(i).getTime();
            String end = (i < topics.size() - 1) ? topics.get(i + 1).getTime() : "99999";
            String rawText = ScriptUtils.extractChunkText(scriptLines, start, end);

            if (rawText.strip().isEmpty()) {
                logger.warn("⚠️ {}~{} 범위에 자막이 없습니다. 건너뜀", start, end);
                continue;
            }

            String prompt = summaryPrompt.createInstruction2();

            // ✅ JSON 응답을 List<ContentBlock>으로 파싱
            CompletableFuture<List<ContentBlock>> future = openAiClient
                .getChatCompletionAsync(prompt, rawText, ModelType.SUMMARY)
                .thenApplyAsync(JsonParser::parseToContentBlockList); // 타입 명시 생략 가능

            futures.add(future);
        }

        return CompletableFuture
            .allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)  // ✅ List<List<ContentBlock>> → List<ContentBlock>
                .collect(Collectors.toList())
            );
    }
    
    @Async
    public CompletableFuture<Void> saveBlock(long contentSeq, List<ContentBlock> blocks) {
        try {
            Map<String, List<SectionDTO>> levelToSections = new HashMap<>();
            List<OxQuizDTO> oxQuizList = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            for (ContentBlock block : blocks) {
                int startTime = Integer.parseInt(block.getTime());

                // 🔹 Easy summary
                if (block.getSummary_Easy() != null && !block.getSummary_Easy().isBlank()) {
                    SectionDTO section = new SectionDTO();
                    section.setTitle(block.getTitle());
                    section.setStartTime(startTime);
                    section.setDetails(block.getSummary_Easy());
                    levelToSections.computeIfAbsent("E", k -> new ArrayList<>()).add(section);
                }

                // 🔹 High summary
                if (block.getSummary_High() != null && !block.getSummary_High().isBlank()) {
                    SectionDTO section = new SectionDTO();
                    section.setTitle(block.getTitle());
                    section.setStartTime(startTime);
                    section.setDetails(block.getSummary_High());
                    levelToSections.computeIfAbsent("H", k -> new ArrayList<>()).add(section);
                }

                // 🔹 OX 퀴즈 수집
                if (block.getOxQuiz() != null) {
                    OxQuizDTO ox = new OxQuizDTO();
                    ox.setContentSeq(contentSeq);
                    ox.setType("OX");
                    ox.setQuestion(block.getOxQuiz().getProblem());
                    ox.setCorrectAnswer(block.getOxQuiz().getAnswer());
                    ox.setExplanation(block.getOxQuiz().getExplanation());
                    ox.setStartTime(startTime);
                    ox.setDeleteYn("N");
                    oxQuizList.add(ox);
                }

                // 🔹 (선택) 서술형 퀴즈도 필요 시 추가 가능
            }

            // 🔹 요약 저장
            for (Map.Entry<String, List<SectionDTO>> entry : levelToSections.entrySet()) {
                SummaryDTO summary = new SummaryDTO();
                summary.setContentSeq(contentSeq);
                summary.setLevel(entry.getKey());
                summary.setIndate(LocalDateTime.now().format(formatter));

                summaryService.saveSummaryWithSections(summary, entry.getValue());
            }

            // 🔹 OX 퀴즈 저장
            if (!oxQuizList.isEmpty()) {
                oxQuizService.saveOxQuizzes(contentSeq, oxQuizList);
            }

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.error("❌ ContentBlock 저장 실패", e);
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    
    

    private void saveSummaryToDbAndVector(List<ContentBlock> blocks) {
        // 벡터 저장
//        List<Float> vector = embeddingService.embed(summary);
        
//		qdrantClient.upsertVector(QdrantCollections.SCRIPT, uuid.hashCode(), vector, summary)
//		    .doOnError(e -> logger.error("업서트 실패", e))
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
