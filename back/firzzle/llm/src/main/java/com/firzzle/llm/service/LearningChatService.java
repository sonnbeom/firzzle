package com.firzzle.llm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firzzle.llm.client.OpenAiClient;
import com.firzzle.llm.dto.ChatCompletionRequestDTO;
import com.firzzle.llm.dto.ChatDTO;
import com.firzzle.llm.dto.ChatHistoryResponseDTO;
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
    public CompletableFuture<LearningChatResponseDTO> learningChat(Long userContentSeq, LearningChatRequestDTO request) {
        String question = request.getQuestion();
        logger.info("📥 [learningChat 시작] userContentSeq={}, userId={}, question={}", userContentSeq, question);

        UserContentDTO userContent = userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);

        Long contentSeq = userContent.getContentSeq();
        List<Float> vector = embeddingService.embed(question);

        // ✅ 이전 응답 2개 불러오기
        List<ChatDTO> previousChats = chatMapper.selectChatsByCursor(
            contentSeq,
            userContent.getUserSeq(),
            null, // 최신순으로부터
            2
        );

        String previousMessages = previousChats.stream()
            .sorted((a, b) -> a.getIndate().compareTo(b.getIndate())) // 오래된 순 정렬
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
                        String defaultAnswer = "해당 내용은 영상에서 언급되지 않았어요. 다른 질문이 있으신가요? 궁금한 점을 말씀해 주시면 최대한 도와드릴게요!";
                        insertChat(contentSeq, userContent.getUserSeq(), question, defaultAnswer);
                        return CompletableFuture.completedFuture(new LearningChatResponseDTO(defaultAnswer));
                    }

                    // ✅ previousMessages 추가하여 prompt 구성
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
                    return new LearningChatResponseDTO("답변 생성 중 오류가 발생했습니다.");
                });
    }



    /**
     * 무한 스크롤 방식으로 채팅 목록을 조회합니다.
     *
     * @param contentSeq 콘텐츠 번호
     * @param userSeq 사용자 번호
     * @param lastIndate 마지막 생성 시간 (null이면 최신순 최초 요청)
     * @param limit 가져올 개수
     * @return 채팅 목록
     */
    @Transactional
    public List<ChatHistoryResponseDTO> getChatsByContentAndUser( Long userContentSeq, String lastIndate, int limit) {
       
        // userContentSeq로 contentSeq와 userSeq 가져옴
        UserContentDTO userContent = userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);

        // 채팅 목록 조회
        List<ChatDTO> chatList = chatMapper.selectChatsByCursor(
                userContent.getContentSeq(),
                userContent.getUserSeq(),
                lastIndate,
                limit
        );

        // ChatDTO를 ChatHistoryResponseDTO로 분리 (question, answer 각각 하나의 응답)
        return chatList.stream()
                .flatMap(chat -> {
                    List<ChatHistoryResponseDTO> items = new ArrayList<>();
                    if (chat.getQuestion() != null) {
                        items.add(new ChatHistoryResponseDTO(chat.getChatSeq(), chat.getQuestion(), chat.getIndate(), 0));
                    }
                    if (chat.getAnswer() != null) {
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
        // 1. user_seq, content_seq 조회
        UserContentDTO userContent = userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);
        Long userSeq = userContent.getUserSeq();
        Long contentSeq = userContent.getContentSeq();

        // 2. 전체 문제 수
        int total = examsMapper.selectTotalExamCount(contentSeq);

        // 3. 사용자 답변 수
        int answered = examsMapper.selectAnsweredExamCount(contentSeq, userSeq);

        // 4. 다음 문제 정보
        ExamsDTO nextQuestion = examsMapper.selectNextExamQuestion(contentSeq, answered+1);

        // 5. 반환 DTO 조립
        NextExamResponseDTO response = NextExamResponseDTO.builder()
                .question(nextQuestion != null ? nextQuestion.getQuestionContent() : "모든 문제를 다 푸셨습니다.")
                .totalCount(total)
                .currentIndex(answered + 1) // 1부터 시작
                .build();

        return CompletableFuture.completedFuture(response);
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
