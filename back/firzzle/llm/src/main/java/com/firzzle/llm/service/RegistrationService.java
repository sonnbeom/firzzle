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
public class RegistrationService {

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

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    
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
                saveBlock(request.getContentSeq(), blocks, scriptLines); // ✅ List<ContentBlock> 저장
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
    public CompletableFuture<Void> saveBlock(long contentSeq, List<ContentBlock> blocks, List<String> scriptLines) {
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
                    
                    // ✅ 벡터 DB 저장용 추가 처리
                    try {
                        List<Float> vector = embeddingService.embed(block.getSummary_Easy());
                        String originalScriptChunk = ScriptUtils.extractChunkText(scriptLines, block.getTime(), getNextBlockTime(blocks, block)); // 종료 시점 계산
                        Map<String, Object> payload = Map.of(
                        	    "contentSeq", contentSeq,
                        	    "content", originalScriptChunk
                        	);

                        ragService.saveToVectorDb(
                            QdrantCollections.SCRIPT,                      // 컬렉션명
                            contentSeq * 100000 + startTime,               // ID 생성 규칙: contentSeq + startTime
                            vector,
                            payload
                        );
                    } catch (Exception e) {
                        logger.error("❌ Qdrant 저장 중 오류 - summary_easy: {}", block.getSummary_Easy(), e);
                    }
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
    
    private String getNextBlockTime(List<ContentBlock> blocks, ContentBlock current) {
        int currentIndex = blocks.indexOf(current);
        if (currentIndex >= 0 && currentIndex < blocks.size() - 1) {
            return blocks.get(currentIndex + 1).getTime();
        }
        return "99999";
    }
}
