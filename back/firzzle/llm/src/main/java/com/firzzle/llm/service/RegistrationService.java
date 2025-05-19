package com.firzzle.llm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.llm.client.*;
import com.firzzle.llm.domain.ContentBlock;
import com.firzzle.llm.domain.TimeLine;
import com.firzzle.llm.domain.TimeLineWrapper;
import com.firzzle.llm.dto.*;
import com.firzzle.llm.mapper.ContentMapper;
import com.firzzle.llm.prompt.*;
import com.firzzle.llm.sse.SseEmitterRepository;
import com.firzzle.llm.util.*;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ContentMapper contentMapper;
    private final PromptFactory promptFactory;
    private final SseEmitterRepository sseEmitterRepository;

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    // ============================================
    // PUBLIC ENTRY POINT
    // ============================================

    /**
     * 전체 자막 콘텐츠를 기반으로 대주제를 추출하고
     * 각 대주제 구간을 요약 및 벡터화하여 저장하는 메서드입니다.
     */
    @Async
    public CompletableFuture<String> summarizeContents(LlmRequestDTO request) {
        String taskId = getOrGenerateTaskId(request);

        sendStart(taskId, request.getContentSeq());
        String content = request.getScript();
        List<String> scriptLines = Arrays.asList(content.split("\n"));
        logger.info("\uD83D\uDE80 전체 요약 시작: taskId={}", taskId);

        sendProgress(taskId, "대주제 추출 중...");

        return extractTimeLines(content)
        	    .thenCompose(wrapper -> {
        	        List<TimeLine> timelines = wrapper.getTimeline();
        	        List<String> keywords = wrapper.getKeywords();
        	        sendTimelineProgress(taskId, timelines);
        	        return summarizeByChunksWithTaskId(taskId, timelines, scriptLines)
        	            .thenApply(blocks -> Map.of("blocks", blocks, "keywords", keywords));
        	    })
        	    .thenApply(map -> {
        	        List<ContentBlock> blocks = (List<ContentBlock>) map.get("blocks");
        	        List<String> keywords = (List<String>) map.get("keywords");

        	        sendProgress(taskId, "요약 완료. 데이터 저장 중...", "blockCount", blocks.size());
        	        blocks.forEach(block -> logger.info("🎯 요약 블록: {}", block.getTitle()));
        	        saveBlock(request.getContentSeq(), blocks, scriptLines, keywords);
                sendResult(taskId, request.getUserContentSeq(), blocks);
                sendComplete(taskId);
                return "✅ 요약 및 저장 완료: " + blocks.size() + "개";
            })
            .exceptionally(e -> {
                logger.error("❌ 전체 요약 처리 중 오류: taskId={}", taskId, e);
                sendError(taskId, "요약 처리 중 오류가 발생했습니다: " + e.getMessage());
                return "GPT 응답 중 오류가 발생했습니다: " + e.getMessage();
            });
    }
    
    // ============================================
    // INTERNAL LOGIC POINT
    // ============================================

    /**
     * 요약된 블록들을 DB 및 벡터 DB에 저장하는 메서드입니다.
     */
    @Async
    @Transactional
    protected CompletableFuture<Void> saveBlock(long contentSeq, List<ContentBlock> blocks, List<String> scriptLines, List<String> keywords) {
        try {
            Map<String, List<SectionDTO>> levelToSections = new HashMap<>();
            List<OxQuizDTO> oxQuizList = new ArrayList<>();
            List<ExamsDTO> examList = new ArrayList<>();

            for (int i = 0; i < blocks.size(); i++) {
                ContentBlock block = blocks.get(i);
                String startTimeStr = block.getTime();
                int startTime = Integer.parseInt(startTimeStr);

                String endTimeStr = (i < blocks.size() - 1 && blocks.get(i + 1).getTime() != null)
                    ? blocks.get(i + 1).getTime()
                    : "99999"; // fallback

                handleSummary(block, startTime, endTimeStr, contentSeq, scriptLines, levelToSections);
                handleOxQuiz(block, startTime, contentSeq, oxQuizList);
                handleExam(block, startTime, contentSeq, examList);
            }

            saveSummaries(contentSeq, levelToSections);
            if (!oxQuizList.isEmpty()) oxQuizService.saveOxQuizzes(contentSeq, oxQuizList);
            if (!examList.isEmpty()) examsService.saveExams(contentSeq, examList);
            saveTitleSummaryVector(contentSeq, blocks, keywords);

            // 🔹 키워드 태그 저장 (중복 제거 후 저장)
            if (keywords != null && !keywords.isEmpty()) {
                List<String> uniqueTags = keywords.stream()
                        .map(String::trim)
                        .filter(tag -> !tag.isBlank())
                        .distinct()
                        .collect(Collectors.toList());
                if (!uniqueTags.isEmpty()) {
                    contentMapper.insertContentTags(contentSeq, uniqueTags);
                    logger.info("🏷️ 콘텐츠 태그 저장 완료: {}", uniqueTags);
                }
            }

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("❌ ContentBlock 저장 실패", e);
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    /**
     * 전체 자막에서 주요 타임라인(대주제)을 추출합니다.
     */
    @Async
    protected CompletableFuture<TimeLineWrapper> extractTimeLines(String content) {
        ChatCompletionRequestDTO timelinePrompt = promptFactory.createTimelineyRequest(content);

        return openAiClient.getChatCompletionAsync(timelinePrompt)
            .thenApply(response -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String cleaned = ScriptUtils.extractJsonOnly(response);
                    TimeLineWrapper wrapper = mapper.readValue(cleaned, TimeLineWrapper.class);
                    logger.info("🧩 추출된 키워드: {}", wrapper.getKeywords());
                    return wrapper;
                } catch (Exception e) {
                    logger.error("❌ 타임라인 파싱 실패: {}", response, e);
                    throw new RuntimeException("타임라인 파싱 실패", e);
                }
            });
    }


    /**
     * 각 대주제별 자막 텍스트를 요약하여 ContentBlock 리스트로 반환합니다.
     */
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

            sendSseEvent(taskId, "progress", Map.of(
                "message", "주제 " + (topicIndex + 1) + "/" + totalTopics + " 요약 중: 시간 " + start,
                "timestamp", System.currentTimeMillis(),
                "currentTime", start,
                "currentIndex", topicIndex + 1,
                "totalTopics", totalTopics
            ));

            ChatCompletionRequestDTO summaryPrompt = promptFactory.createSummaryRequest(rawText);
            CompletableFuture<List<ContentBlock>> future = openAiClient
                .getChatCompletionAsync(summaryPrompt)
                .thenApplyAsync(JsonParser::parseToContentBlockList);

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList())
            );
    }
    
    // ============================================
    // BLOCK 처리 로직
    // ============================================

    /**
     * Easy/High 요약 저장 및 Easy 요약 벡터화를 처리합니다.
     */
    private void handleSummary(
    	    ContentBlock block,
    	    int startTime,
    	    String endTime,
    	    long contentSeq,
    	    List<String> scriptLines,
    	    Map<String, List<SectionDTO>> levelToSections
    	) {
    	    String easy = block.getSummary_Easy();
    	    String high = block.getSummary_High();

    	    if (easy != null && !easy.isBlank()) {
    	        addSection("E", block.getTitle(), startTime, easy, levelToSections);

    	        logger.info("📌 saveVectorForBlock 호출 - contentSeq={}, startTime={}, endTime={}, summaryEasy.length={}",
    	                contentSeq, startTime, endTime, easy.length());

    	        saveVectorForBlock(contentSeq, startTime, easy, scriptLines, block.getTime(), endTime);
    	    }

    	    if (high != null && !high.isBlank()) {
    	        addSection("H", block.getTitle(), startTime, high, levelToSections);
    	    }
    	}


    /**
     * 섹션 DTO 객체를 요약 레벨별로 구분하여 추가합니다.
     */
    private void addSection(String level, String title, int startTime, String details, Map<String, List<SectionDTO>> sectionMap) {
        SectionDTO section = new SectionDTO();
        section.setTitle(title);
        section.setStartTime(startTime);
        section.setDetails(details);
        sectionMap.computeIfAbsent(level, k -> new ArrayList<>()).add(section);
    }
    

    /**
     * Easy 요약 텍스트를 벡터로 변환하여 벡터 DB에 저장합니다.
     */
    private void saveVectorForBlock(
    	    long contentSeq,
    	    int startTime,
    	    String text,
    	    List<String> scriptLines,
    	    String blockStartTime,
    	    String blockEndTime 
    	) {
        try {
            logger.info("📥 [벡터 저장 시작] contentSeq={}, startTime={}, blockStartTime={}, blockEndTime={}, text.length={}",
                    contentSeq,
                    startTime,
                    blockStartTime,
                    blockEndTime,
                    text != null ? text.length() : "null");
            
            List<Float> vector = embeddingService.embed(text);
            String chunk = ScriptUtils.extractChunkText(scriptLines, blockStartTime, blockEndTime);

            Map<String, Object> payload = Map.of(
                "contentSeq", contentSeq,
                "content", chunk
            );

            ragService.saveToVectorDb(
                QdrantCollections.SCRIPT,
                contentSeq * 100000 + startTime,
                vector,
                payload
            );
        } catch (Exception e) {
            logger.error("❌ Qdrant 저장 중 오류 - summary_easy: {}", text, e);
        }
    }

    /**
     * OX 퀴즈 정보를 수집하여 리스트에 추가합니다.
     */
    private void handleOxQuiz(ContentBlock block, int startTime, long contentSeq, List<OxQuizDTO> oxQuizList) {
        if (block.getOxQuiz() == null) return;
        var quiz = block.getOxQuiz();

        OxQuizDTO ox = new OxQuizDTO();
        ox.setContentSeq(contentSeq);
        ox.setType("OX");
        ox.setQuestion(quiz.getProblem());
        ox.setCorrectAnswer(quiz.getAnswer());
        ox.setExplanation(quiz.getExplanation());
        ox.setStartTime(startTime);
        ox.setDeleteYn("N");
        oxQuizList.add(ox);
    }

    /**
     * 서술형 퀴즈 정보를 수집하여 리스트에 추가합니다.
     */
    private void handleExam(ContentBlock block, int startTime, long contentSeq, List<ExamsDTO> examList) {
        if (block.getExam() == null) return;
        var exam = block.getExam();

        examList.add(ExamsDTO.builder()
            .contentSeq(contentSeq)
            .questionContent(exam.getQuestion())
            .modelAnswer(exam.getAnswer())
            .startTime(startTime)
            .referenceText(block.getSummary_Easy())
            .build());
    }
    
    
    /**
     * 저장할 요약 정보를 SummaryService를 통해 저장합니다.
     */
    private void saveSummaries(long contentSeq, Map<String, List<SectionDTO>> sectionMap) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        for (var entry : sectionMap.entrySet()) {
            SummaryDTO summary = new SummaryDTO();
            summary.setContentSeq(contentSeq);
            summary.setLevel(entry.getKey());
            summary.setIndate(now);
            summaryService.saveSummaryWithSections(summary, entry.getValue());
        }
    }


    /**
     * 전체 소주제 타이틀을 문자열로 연결하고 벡터화하여 저장합니다.
     * 저장 위치: QdrantCollections.CONTENT (추천 시스템 전용 벡터 컬렉션)
     */
    private void saveTitleSummaryVector(long contentSeq, List<ContentBlock> blocks, List<String> keywords) {
        try {
            String joinedTitles = blocks.stream()
                .map(ContentBlock::getTitle)
                .filter(Objects::nonNull)
                .filter(t -> !t.isBlank())
                .collect(Collectors.joining(" | "));

            if (!joinedTitles.isBlank()) {
                List<Float> vector = embeddingService.embed(joinedTitles);

                Map<String, Object> payload = new HashMap<>();
                payload.put("contentSeq", contentSeq);

                // ✅ 키워드도 payload에 저장
                if (keywords != null && !keywords.isEmpty()) {
                    List<String> cleanedKeywords = keywords.stream()
                            .map(String::trim)
                            .filter(k -> !k.isBlank())
                            .distinct()
                            .collect(Collectors.toList());

                    if (!cleanedKeywords.isEmpty()) {
                        payload.put("keywords", cleanedKeywords);
                        logger.info("🏷️ 저장할 키워드: {}", cleanedKeywords);
                    }
                }

                ragService.saveToVectorDb(
                    QdrantCollections.CONTENT,
                    contentSeq * 100000L + 1L,
                    vector,
                    payload
                );

                logger.info("🧠 전체 소주제 타이틀 벡터 저장 완료: {}", joinedTitles);
            }
        } catch (Exception e) {
            logger.error("❌ 전체 소주제 타이틀 벡터 저장 중 오류", e);
        }
    }
    // ============================================
    // SSE 유틸리티 및 상태 전송
    // ============================================

    /**
     * taskId가 없을 경우 새로 생성하고 반환합니다.
     */
    private String getOrGenerateTaskId(LlmRequestDTO request) {
        String taskId = request.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            taskId = UUID.randomUUID().toString();
            request.setTaskId(taskId);
        }
        return taskId;
    }

    /**
     * SSE - 작업 시작 이벤트 전송
     */
    private void sendStart(String taskId, long contentSeq) {
        sendSseEvent(taskId, "start", Map.of(
            "message", "자막 요약 작업을 시작합니다.",
            "contentSeq", contentSeq,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * SSE - 일반 진행 상태 전송
     */
    private void sendProgress(String taskId, String message) {
        sendSseEvent(taskId, "progress", Map.of(
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
    }


    /**
     * SSE - 진행 상태 + 추가 데이터 전송
     */
    private void sendProgress(String taskId, String message, String key, Object value) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("timestamp", System.currentTimeMillis());
        data.put(key, value);
        sendSseEvent(taskId, "progress", data);
    }

    /**
     * SSE - 타임라인 진행 상태 전송
     */
    private void sendTimelineProgress(String taskId, List<TimeLine> timelines) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "타임라인 " + timelines.size() + "개 추출 완료. 세부 요약 생성 중...");
        data.put("timestamp", System.currentTimeMillis());
        data.put("timePoints", timelines.stream().map(TimeLine::getTime).collect(Collectors.toList()));
        sendSseEvent(taskId, "progress", data);
    }


    /**
     * SSE - 결과 데이터 전송
     */
    private void sendResult(String taskId, long contentSeq, List<ContentBlock> blocks) {
        Map<String, Object> result = new HashMap<>();
        result.put("contentSeq", contentSeq);
        result.put("blockCount", blocks.size());
        result.put("blocks", blocks);
        result.put("timestamp", System.currentTimeMillis());
        sendSseEvent(taskId, "result", result);
    }

    /**
     * SSE - 완료 상태 전송
     */
    private void sendComplete(String taskId) {
        sendSseEvent(taskId, "complete", Map.of(
            "message", "요약 작업이 완료되었습니다.",
            "timestamp", System.currentTimeMillis()
        ));
    }


    /**
     * SSE - 오류 발생 시 전송
     */
    private void sendError(String taskId, String errorMessage) {
        sendSseEvent(taskId, "error", Map.of(
            "message", errorMessage,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 실제 SSE 전송 로직
     */
    private void sendSseEvent(String taskId, String eventName, Map<String, Object> data) {
        if (sseEmitterRepository.exists(taskId)) {
            sseEmitterRepository.sendToClient(taskId, eventName, data);
        } else {
            logger.warn("⚠️ SSE 클라이언트가 연결되어 있지 않음: taskId={}, event={}", taskId, eventName);
        }
    }

    /**
     * 현재 블록 기준 다음 블록의 시작 시간 계산
     */
    private String getNextBlockTime(List<ContentBlock> blocks, ContentBlock current) {
        int currentIndex = blocks.indexOf(current);
        if (currentIndex >= 0 && currentIndex < blocks.size() - 1) {
            return blocks.get(currentIndex + 1).getTime();
        }
        return "99999";
    }
}
