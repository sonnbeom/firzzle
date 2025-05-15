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
import com.firzzle.llm.dto.ChatCompletionRequestDTO;
import com.firzzle.llm.dto.ChatDTO;
import com.firzzle.llm.dto.ChatMessageDTO;
import com.firzzle.llm.dto.LearningChatRequestDTO;
import com.firzzle.llm.dto.LearningChatResponseDTO;
import com.firzzle.llm.dto.UserContentDTO;
import com.firzzle.llm.mapper.ChatMapper;
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
    private final UserContentMapper userContentMapper;

    private static final Logger logger = LoggerFactory.getLogger(LearningChatService.class);

    // RAG 기반 실시간 대화 응답 생성 및 DB 저장
    @Async
    @Transactional
    public CompletableFuture<LearningChatResponseDTO> learningChat(Long userContentSeq, LearningChatRequestDTO request, String userId) {
        String question = request.getQuestion();
        logger.info("📥 [learningChat 시작] userContentSeq={}, userId={}, question={}", userContentSeq, userId, question);

        Long userSeqFromUUID = userMapper.selectUserSeqByUuid(userId);
        UserContentDTO userContent = userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);

        if (!userSeqFromUUID.equals(userContent.getUserSeq())) {
            throw new IllegalArgumentException("사용자 인증 정보가 일치하지 않습니다.");
        }

        Long contentSeq = userContent.getContentSeq();

        List<Float> vector = embeddingService.embed(question);

        return ragService.searchTopPayloadsByContentSeq(QdrantCollections.SCRIPT, vector, contentSeq)
                .toFuture()
                .thenCompose(contents -> {
                    logger.debug("🔍 [벡터 검색 결과] top contents count={}", contents.size());

                    String context = contents.stream().limit(5).collect(Collectors.joining("\n"));

                    if (context.isEmpty()) {
                        logger.info("⚠️ [context 없음] 기본 응답 반환");
                        String defaultAnswer = "해당 내용은 영상에서 언급되지 않았어요. 다른 질문이 있으신가요? 궁금한 점을 말씀해 주시면 최대한 도와드릴게요!";
                        insertChat(contentSeq, userSeqFromUUID, question, defaultAnswer);
                        return CompletableFuture.completedFuture(new LearningChatResponseDTO(defaultAnswer));
                    }

                    ChatCompletionRequestDTO chatRequest = promptFactory.createLearningChatRequest(question, context);
                    logger.debug("📬 [OpenAI 요청 전] 생성된 prompt context 일부=\n{}", context.substring(0, Math.min(context.length(), 300)));

                    return openAiClient.getChatCompletionAsync(chatRequest)
                            .thenApply(answer -> {
                                insertChat(contentSeq, userSeqFromUUID, question, answer);
                                return new LearningChatResponseDTO(answer);
                            });
                })
                .exceptionally(e -> {
                    logger.error("❌ learningChat 처리 중 오류", e);
                    return new LearningChatResponseDTO("답변 생성 중 오류가 발생했습니다.");
                });
    }

    @Transactional
    public List<ChatMessageDTO> getChatMessages(Long userContentSeq, Long lastMessageId, int limit) {
        return null;
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
