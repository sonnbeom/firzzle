package com.firzzle.llm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.llm.client.*;
import com.firzzle.llm.domain.ContentBlock;
import com.firzzle.llm.domain.TimeLine;
import com.firzzle.llm.dto.*;
import com.firzzle.llm.prompt.*;
import com.firzzle.llm.sse.SseEmitterRepository;
import com.firzzle.llm.util.*;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final EmbeddingService embeddingService;
    private final OxQuizService oxQuizService;
    private final RagService ragService;
    private final SummaryService summaryService;
    private final ExamsService examsService;
    private final PromptFactory promptFactory;
    private final SseEmitterRepository sseEmitterRepository;

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    /**
     * 요약 작업 ID 생성
     */
    public String createSummaryTask(LlmRequestDTO request) {
        String taskId = request.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            taskId = UUID.randomUUID().toString();
        }
        logger.info("📋 요약 작업 ID 생성: {}", taskId);
        return taskId;
    }

    // 전체 자막 콘텐츠를 요약하는 비동기 함수
    @Async
    public CompletableFuture<String> summarizeContents(LlmRequestDTO request) {
        String taskId = request.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            taskId = createSummaryTask(request);
            request.setTaskId(taskId);
        }

        // SSE 클라이언트에 작업 시작 이벤트 전송
        sendSseEvent(taskId, "start", Map.of(
                "message", "자막 요약 작업을 시작합니다.",
                "contentSeq", request.getContentSeq(),
                "timestamp", System.currentTimeMillis()
        ));

        String content = request.getScript();
        List<String> scriptLines = Arrays.asList(content.split("\n"));

        logger.info("🚀 전체 요약 시작: taskId={}", taskId);

        // 진행 상황 이벤트 전송
        sendSseEvent(taskId, "progress", Map.of(
                "message", "대주제 추출 중...",
                "timestamp", System.currentTimeMillis()
        ));

        final String finalTaskId = taskId;
        return extractTimeLines(content)
                .thenCompose(timelines -> {
                    // 진행 상황 이벤트 전송
                    Map<String, Object> progressData = new HashMap<>();
                    progressData.put("message", "대주제 " + timelines.size() + "개 추출 완료. 세부 요약 생성 중...");
                    progressData.put("timestamp", System.currentTimeMillis());

                    // TimeLine 객체에서 time 정보만 수집
                    List<String> timePoints = new ArrayList<>();
                    for (TimeLine timeline : timelines) {
                        timePoints.add(timeline.getTime());
                    }
                    progressData.put("timePoints", timePoints);

                    sendSseEvent(finalTaskId, "progress", progressData);

                    return summarizeByChunksWithTaskId(finalTaskId, timelines, scriptLines);
                })
                .thenApply(blocks -> {
                    // 진행 상황 이벤트 전송
                    sendSseEvent(finalTaskId, "progress", Map.of(
                            "message", "요약 완료. 데이터 저장 중...",
                            "timestamp", System.currentTimeMillis(),
                            "blockCount", blocks.size()
                    ));

                    blocks.forEach(block -> logger.info("🎯 요약 블록: {}", block.getTitle()));

                    // 블록 저장
                    saveBlock(request.getContentSeq(), blocks, scriptLines);

                    // 결과 이벤트 전송
                    Map<String, Object> resultData = new HashMap<>();
                    resultData.put("contentSeq", request.getContentSeq());
                    resultData.put("blockCount", blocks.size());
                    resultData.put("blocks", blocks);
                    resultData.put("timestamp", System.currentTimeMillis());

                    sendSseEvent(finalTaskId, "result", resultData);

                    // 완료 이벤트 전송
                    sendSseEvent(finalTaskId, "complete", Map.of(
                            "message", "요약 작업이 완료되었습니다.",
                            "timestamp", System.currentTimeMillis()
                    ));

                    return "✅ 요약 및 저장 완료: " + blocks.size() + "개";
                })
                .exceptionally(e -> {
                    logger.error("❌ 전체 요약 처리 중 오류: taskId={}", finalTaskId, e);

                    // 오류 이벤트 전송
                    sendSseEvent(finalTaskId, "error", Map.of(
                            "message", "요약 처리 중 오류가 발생했습니다: " + e.getMessage(),
                            "timestamp", System.currentTimeMillis()
                    ));

                    return "GPT 응답 중 오류가 발생했습니다: " + e.getMessage();
                });
    }

    // SSE 이벤트 전송 유틸리티 메서드
    private void sendSseEvent(String taskId, String eventName, Map<String, Object> data) {
        if (sseEmitterRepository.exists(taskId)) {
            sseEmitterRepository.sendToClient(taskId, eventName, data);
        } else {
            logger.warn("⚠️ SSE 클라이언트가 연결되어 있지 않음: taskId={}, event={}", taskId, eventName);
        }
    }

    // 전체 자막 텍스트에서 주요 대주제를 추출하는 함수 - @Async 메서드는 public 또는 protected 가시성 필요
    @Async
    protected CompletableFuture<List<TimeLine>> extractTimeLines(String content) {
        ChatCompletionRequestDTO timelinePrompt = promptFactory.createTimelineyRequest(content);

        return openAiClient.getChatCompletionAsync(timelinePrompt)
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

    // 주요 토픽별로 자막을 나누어 요약 요청을 보내는 함수 - @Async 메서드는 public 또는 protected 가시성 필요
    @Async
    protected CompletableFuture<List<ContentBlock>> summarizeByChunksWithTaskId(String taskId, List<TimeLine> topics, List<String> scriptLines) {
        List<CompletableFuture<List<ContentBlock>>> futures = new ArrayList<>();
        int totalTopics = topics.size();

        for (int i = 0; i < topics.size(); i++) {
            final int topicIndex = i;
            String start = topics.get(i).getTime();
            String end = (i < topics.size() - 1) ? topics.get(i + 1).getTime() : "99999";
            String rawText = ScriptUtils.extractChunkText(scriptLines, start, end);

            if (rawText.strip().isEmpty()) {
                logger.warn("⚠️ {}~{} 범위에 자막이 없습니다. 건너뜀", start, end);
                continue;
            }

            // 세부 진행 상황 이벤트 전송
            sendSseEvent(taskId, "progress", Map.of(
                    "message", "주제 " + (topicIndex+1) + "/" + totalTopics + " 요약 중: 시간 " + start,
                    "timestamp", System.currentTimeMillis(),
                    "currentTime", start,
                    "currentIndex", topicIndex + 1,
                    "totalTopics", totalTopics
            ));

            ChatCompletionRequestDTO summaryPrompt = promptFactory.createSummaryRequest(rawText);

            // ✅ JSON 응답을 List<ContentBlock>으로 파싱
            CompletableFuture<List<ContentBlock>> future = openAiClient
                    .getChatCompletionAsync(summaryPrompt)
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
            List<ExamsDTO> examList = new ArrayList<>();
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

                // 🔹 서술형 퀴즈 수집
                if (block.getExam() != null) {
                    ExamsDTO exam = ExamsDTO.builder()
                            .contentSeq(contentSeq)
                            .questionContent(block.getExam().getQuestion())
                            .modelAnswer(block.getExam().getAnswer())
                            .startTime(startTime) // 예: "00:05:12" 형식
                            .referenceText(block.getSummary_Easy()) // 또는 다른 기준 설명
                            .build();
                    examList.add(exam);
                }
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

            // 🔹 서술형 퀴즈 저장
            if (!examList.isEmpty()) {
                examsService.saveExams(contentSeq, examList);
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