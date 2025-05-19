package com.firzzle.llm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.llm.client.OpenAiClient;
import com.firzzle.llm.dto.ChatCompletionRequestDTO;
import com.firzzle.llm.dto.ChatDTO;
import com.firzzle.llm.dto.ChatHistoryResponseDTO;
import com.firzzle.llm.dto.ExamAnswerDTO;
import com.firzzle.llm.dto.ExamAnswerRequestDTO;
import com.firzzle.llm.dto.ExamAnswerResponseDTO;
import com.firzzle.llm.dto.ExamHistoryResponseDTO;
import com.firzzle.llm.dto.ExamsDTO;
import com.firzzle.llm.dto.LearningChatRequestDTO;
import com.firzzle.llm.dto.LearningChatResponseDTO;
import com.firzzle.llm.dto.NextExamResponseDTO;
import com.firzzle.llm.dto.UserContentDTO;
import com.firzzle.llm.mapper.ChatMapper;
import com.firzzle.llm.mapper.ExamsMapper;
import com.firzzle.llm.mapper.UserContentMapper;
import com.firzzle.llm.mapper.UserMapper;
import com.firzzle.llm.prompt.PromptFactory;
import com.firzzle.llm.util.QdrantCollections;
import com.firzzle.llm.util.TimeUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LearningChatService {
    private final OpenAiClient openAiClient;
    private final EmbeddingService embeddingService;
    private final RagService ragService;
    private final PromptFactory promptFactory;
    private final ChatMapper chatMapper;
    private final UserMapper userMapper;
    private final ExamsMapper examsMapper;
    private final UserContentMapper userContentMapper;

    private static final Logger logger = LoggerFactory.getLogger(LearningChatService.class);

    // RAG 기반 실시간 대화 응답 생성 및 DB 저장
    @Async
    @Transactional
    public CompletableFuture<LearningChatResponseDTO> learningChat(String uuid, Long userContentSeq, LearningChatRequestDTO request) {
        String question = request.getQuestion();
        logger.info("📥 [learningChat 시작] userContentSeq={}, userId={}, question={}", userContentSeq, question);
        if (question == null || question.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "질문이 비어 있습니다.");
        }

        Long actualUserSeq = userMapper.selectUserSeqByUuid(uuid);

        UserContentDTO userContent = userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);
        Long userSeq = userContent.getUserSeq();
        Long contentSeq = userContent.getContentSeq();

        if (!actualUserSeq.equals(userSeq)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS, "해당 콘텐츠에 대한 접근 권한이 없습니다.");
        }

        List<Float> vector;
        try {
            vector = embeddingService.embed(question);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.VECTOR_EMBEDDING_FAILED, "질문 임베딩 중 오류가 발생했습니다.");
        }

        List<ChatDTO> previousChats = chatMapper.selectChatsByCursor(contentSeq, userContent.getUserSeq(), null, 2);
        String previousMessages = previousChats.stream()
            .sorted((a, b) -> a.getIndate().compareTo(b.getIndate()))
            .map(chat -> "Q: " + chat.getQuestion() + "\nA: " + chat.getAnswer())
            .collect(Collectors.joining("\n\n"));

        logger.info(previousMessages);

        return ragService.searchTopPayloadsByContentSeq(QdrantCollections.SCRIPT, vector, contentSeq)
            .toFuture()
            .thenCompose(contents -> {
                logger.debug("🔍 [벡터 검색 결과] top contents count={}", contents.size());
                String context = contents.stream().limit(5).collect(Collectors.joining("\n"));

                if (context.isEmpty()) {
                    logger.info("⚠️ [context 없음] 기본 응답 반환");
                    String defaultAnswer = "해당 내용은 영상에서 언급되지 않았어요. 다른 질문이 있으신가요?";
                    insertChat(contentSeq, userContent.getUserSeq(), question, defaultAnswer);
                    return CompletableFuture.completedFuture(new LearningChatResponseDTO(defaultAnswer));
                }

                ChatCompletionRequestDTO chatRequest = promptFactory.createLearningChatRequest(question, context, previousMessages);
                logger.debug("📬 [OpenAI 요청 전] 생성된 prompt context 일부=\n{}", context.substring(0, Math.min(context.length(), 300)));

                return openAiClient.getChatCompletionAsync(chatRequest)
                    .thenApply(answer -> {
                        insertChat(contentSeq, userContent.getUserSeq(), question, answer);
                        return new LearningChatResponseDTO(answer);
                    });
            })
            .exceptionally(e -> {
                logger.error("❌ learningChat 처리 중 오류", e);
                throw new BusinessException(ErrorCode.OPENAI_REQUEST_FAILED, "답변 생성 중 오류가 발생했습니다.");
            });
    }



    /**
     * 무한 스크롤 방식으로 채팅 목록을 조회합니다.
     *
     * @param uuid 사용자 UUID
     * @param userContentSeq 사용자-콘텐츠 매핑 ID
     * @param lastIndate 마지막 생성 시간 (null이면 최신순 최초 요청)
     * @param limit 가져올 개수
     * @return 채팅 목록
     */
    @Transactional
    public List<ChatHistoryResponseDTO> getChatsByContentAndUser(String uuid, Long userContentSeq, String lastIndate, int limit) {
        // 1. UUID로 사용자 번호 조회
        Long actualUserSeq = userMapper.selectUserSeqByUuid(uuid);
        if (actualUserSeq == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "해당 UUID로 사용자를 찾을 수 없습니다.");
        }

        // 2. 콘텐츠 매핑 정보 조회
        UserContentDTO userContent = userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);
        if (userContent == null) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "해당 콘텐츠 정보를 찾을 수 없습니다.");
        }

        Long userSeq = userContent.getUserSeq();
        Long contentSeq = userContent.getContentSeq();

        // 3. 권한 체크
        if (!actualUserSeq.equals(userSeq)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS, "해당 콘텐츠에 대한 접근 권한이 없습니다.");
        }

        // 4. 채팅 목록 조회
        List<ChatDTO> chatList = chatMapper.selectChatsByCursor(
            contentSeq,
            userSeq,
            lastIndate,
            limit
        );

        // 5. ChatDTO → ChatHistoryResponseDTO 변환 (질문/답변 분리)
        return chatList.stream()
            .flatMap(chat -> {
                List<ChatHistoryResponseDTO> items = new ArrayList<>();
                if (chat.getQuestion() != null && !chat.getQuestion().isBlank()) {
                    items.add(new ChatHistoryResponseDTO(chat.getChatSeq(), chat.getQuestion(), chat.getIndate(), 0));
                }
                if (chat.getAnswer() != null && !chat.getAnswer().isBlank()) {
                    items.add(new ChatHistoryResponseDTO(chat.getChatSeq(), chat.getAnswer(), chat.getIndate(), 1));
                }
                return items.stream();
            })
            .collect(Collectors.toList());
    }

    
    /**
     * 다음 시험 문제 받아오기 
     *
     * @param contentSeq 콘텐츠 번호
     * @param uuid 사용자 번호
     * @return 질문 가져오기 
     */
    @Async
    @Transactional
    public CompletableFuture<NextExamResponseDTO> getNextExam(String uuid, Long userContentSeq) {
        // 1. UUID로 사용자 번호 조회
        Long actualUserSeq = userMapper.selectUserSeqByUuid(uuid);
        if (actualUserSeq == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        // 2. 콘텐츠 매핑 정보 조회
        UserContentDTO userContent = userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);
        if (userContent == null) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "해당 콘텐츠를 찾을 수 없습니다.");
        }

        Long userSeq = userContent.getUserSeq();
        Long contentSeq = userContent.getContentSeq();

        // 3. 권한 체크
        if (!actualUserSeq.equals(userSeq)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS, "해당 콘텐츠에 대한 접근 권한이 없습니다.");
        }

        // 5. 사용자 답변 수
        int answered = examsMapper.selectAnsweredExamCount(contentSeq, userSeq);

        // 6. 다음 문제 정보
        ExamsDTO nextQuestion = examsMapper.selectNextExamQuestion(contentSeq, answered + 1);
        if (nextQuestion == null) {
            // ❗ 모든 문제를 푼 상태 → 메시지만 반환 (예외 대신 정상 응답 처리 유지)
            return CompletableFuture.completedFuture(
                NextExamResponseDTO.builder()
                    .question("모든 문제를 다 푸셨습니다.")
                    .build()
            );
        }

        // 7. 반환 DTO 조립
        NextExamResponseDTO response = NextExamResponseDTO.builder()
            .question(nextQuestion.getQuestionContent())
            .build();

        return CompletableFuture.completedFuture(response);
    }

    /**
     * 시험 문제 제출 및 응답 
     *
     * @param uuid 사용자 번호 
     * @param request 질문 내용
     * @return 질문 응답
     */
    @Async
    @Transactional
    public CompletableFuture<ExamAnswerResponseDTO> submitExamAnswer(String uuid, Long userContentSeq, ExamAnswerRequestDTO request) {
        // 1. UUID로 사용자 번호 조회
        Long actualUserSeq = userMapper.selectUserSeqByUuid(uuid);
        if (actualUserSeq == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        // 2. 콘텐츠 매핑 정보 조회
        UserContentDTO userContent = userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);
        if (userContent == null) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "해당 콘텐츠 정보를 찾을 수 없습니다.");
        }

        Long userSeq = userContent.getUserSeq();
        Long contentSeq = userContent.getContentSeq();

        // 3. 권한 체크
        if (!actualUserSeq.equals(userSeq)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS, "해당 콘텐츠에 대한 접근 권한이 없습니다.");
        }

        // 4. 답변 수 기준 다음 문제 index
        int answered = examsMapper.selectAnsweredExamCount(contentSeq, userSeq);
        int nextIndex = answered + 1;

        // 5. 해당 시험 문제 조회
        ExamsDTO currentExam = examsMapper.selectNextExamQuestion(contentSeq, nextIndex);
        if (currentExam == null) {
            throw new BusinessException(ErrorCode.ANSWER_ALREADY_COMPLETED, "모든 문제를 이미 푸셨습니다. 고생하셨습니다!");
        }

        // 6. 사용자 답변
        String userAnswer = request.getAnswer();
        if (userAnswer == null || userAnswer.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "답변이 비어 있습니다.");
        }

        // 7. 프롬프트 구성
        ChatCompletionRequestDTO prompt = promptFactory.createExamAnswerRequest(
            userAnswer,
            currentExam.getModelAnswer(),
            currentExam.getReferenceText()
        );

        // 8. 해설 생성 → 저장 → 응답 DTO 생성
        return openAiClient.getChatCompletionAsync(prompt)
            .thenApply(aiExplanation -> {
                String indate = TimeUtil.getCurrentTimestamp14();

                // 9. DB 저장
                ExamAnswerDTO answerDTO = ExamAnswerDTO.builder()
                        .examSeq(currentExam.getExamSeq())
                        .userSeq(userSeq)
                        .answerContent(userAnswer)
                        .explanationContent(aiExplanation)
                        .indate(indate)
                        .build();
                examsMapper.insertExamAnswer(answerDTO);

                // 10. 응답 DTO 구성 및 반환
                return new ExamAnswerResponseDTO(aiExplanation);
            })
            .exceptionally(e -> {
                logger.error("❌ 시험 응답 처리 중 오류 발생", e);
                throw new BusinessException(ErrorCode.OPENAI_REQUEST_FAILED, "해설 생성 중 오류가 발생했습니다.");
            });
    }

    
    // Service 내부
    @Async
    public CompletableFuture<List<ExamHistoryResponseDTO>> getExamHistory(
            String uuid, Long userContentSeq, String lastIndate, int limit) {

        // 1. UUID로 사용자 번호 조회
        Long actualUserSeq = userMapper.selectUserSeqByUuid(uuid);
        if (actualUserSeq == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        // 2. 콘텐츠 매핑 정보 조회
        UserContentDTO userContent = userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);
        if (userContent == null) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠 정보를 찾을 수 없습니다.");
        }

        Long userSeq = userContent.getUserSeq();
        Long contentSeq = userContent.getContentSeq();

        // 3. 권한 체크
        if (!actualUserSeq.equals(userSeq)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS, "해당 콘텐츠에 대한 접근 권한이 없습니다.");
        }

        // 4. 답변 기록 원본 조회
        List<Map<String, Object>> rawList = examsMapper.selectRawAnsweredExamList(contentSeq, userSeq, lastIndate, limit);

        // 5. 질문 → 답변 → 해설 순서로 정렬하여 구성
        List<ExamHistoryResponseDTO> result = new ArrayList<>();
        for (Map<String, Object> row : rawList) {
            Object indate = row.get("indate");

            ExamHistoryResponseDTO explanationDto = toDto(row.get("explanation"), indate, 1); // 해설
            ExamHistoryResponseDTO answerDto = toDto(row.get("answer"), indate, 0);           // 답변
            ExamHistoryResponseDTO questionDto = toDto(row.get("question"), indate, 1);       // 질문

            if (explanationDto != null) result.add(explanationDto);
            if (answerDto != null) result.add(answerDto);
            if (questionDto != null) result.add(questionDto);
        }

        return CompletableFuture.completedFuture(result);
    }

    /**
     * question/answer/explanation 항목을 하나의 ExamHistoryResponseDTO로 변환
     * type: 1=질문, 0=답변, 1=해설
     */
    private ExamHistoryResponseDTO toDto(Object content, Object indate, int type) {
        if (content == null || content.toString().isBlank()) return null;
        return ExamHistoryResponseDTO.builder()
                .content(content.toString())
                .indate(indate.toString())
                .type(type)
                .build();
    }

    private void insertChat(Long contentSeq, Long userSeq, String question, String answer) {
        ChatDTO chat = new ChatDTO();
        chat.setContentSeq(contentSeq);
        chat.setUserSeq(userSeq);
        chat.setQuestion(question);
        chat.setAnswer(answer);
        chat.setIndate(TimeUtil.getCurrentTimestamp14());
        chatMapper.insertChat(chat);
    }

}
