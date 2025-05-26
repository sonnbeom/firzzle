package com.firzzle.learning.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.learning.ai.dao.ChatDAO;
import com.firzzle.learning.ai.dao.ExamDAO;
import com.firzzle.learning.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Class Name : LearningChatService.java
 * @Description : 러닝챗 서비스
 * @author Firzzle
 * @since 2025. 5. 9.
 */
@Service
@RequiredArgsConstructor
public class LearningChatService {

    private static final Logger logger = LoggerFactory.getLogger(LearningChatService.class);

    private final ContentService contentService;
    private final AiService aiService;
    private final ChatDAO chatDAO;
    private final ExamDAO examDAO;

    // 설정 값은 상수로 정의
    private static final int MAX_QUESTION_COUNT = 3;
    private static final int MAX_QUESTION_LENGTH = 200;
    private static final String PROMPT_LEARNING_MODE = "AIQ001";
    private static final String PROMPT_EXAM_QUESTION = "AIQ002";
    private static final String PROMPT_EXAM_EVALUATION = "AIQ003";
    private static final int MAX_CHAT_HISTORY = 3;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    /**
     * 학습모드 채팅 내역 조회
     * @param box 요청 파라미터가 담긴 RequestBox
     * @return 처리 결과가 담긴 DataBox
     * @throws BusinessException 비즈니스 예외 발생 시
     */
    public DataBox getLearningModeChats(RequestBox box) {
        Long userContentSeq = box.getLong("userContentSeq");
        Long cursor = box.getLong("cursor");
        int size = box.getInt("size");
        String orderBy = box.getString("orderBy");
        String direction = box.getString("direction");
        String uuid = box.getString("uuid");

        // 테이블 별칭 설정 (없을 경우를 대비)
        if (box.get("cursorTable") == null) {
            box.put("cursorTable", "C");
        }

        logger.debug("학습모드 채팅 내역 조회 시작 - 사용자 콘텐츠 일련번호: {}, 커서: {}, 페이지 크기: {}, 정렬: {} {}, UUID: {}",
                userContentSeq, cursor, size, orderBy, direction, uuid);

        // 1. 사용자-콘텐츠 정보 조회
        DataBox userContentInfo = getUserContentInfo(userContentSeq, uuid);
        Long contentSeq = userContentInfo.getLong2("d_content_seq");

        // 2. 채팅 내역 조회
        RequestBox chatHistoryBox = new RequestBox("chatHistoryBox");
        chatHistoryBox.put("uuid", uuid);
        chatHistoryBox.put("contentSeq", contentSeq);
        chatHistoryBox.put("cursor", cursor);
        chatHistoryBox.put("size", size + 1); // 추가 데이터가 있는지 확인을 위해 요청 크기 + 1
        chatHistoryBox.put("orderBy", orderBy);
        chatHistoryBox.put("direction", direction);

        List<DataBox> chatHistory = chatDAO.selectChatHistoryWithCursor(chatHistoryBox);

        boolean hasNextPage = chatHistory.size() > size;
        if (hasNextPage) {
            chatHistory.remove(chatHistory.size() - 1); // 마지막 항목 제거
        }

        Long nextCursor = null;
        if (hasNextPage && !chatHistory.isEmpty()) {
            nextCursor = chatHistory.get(chatHistory.size() - 1).getLong2("d_chat_seq");
        }

        // 3. 결과 DataBox 구성
        DataBox resultDataBox = new DataBox();
        resultDataBox.put("d_chat_history", chatHistory);
        resultDataBox.put("d_has_more", hasNextPage);
        resultDataBox.put("d_next_cursor", nextCursor);

        logger.debug("학습모드 채팅 내역 조회 완료 - 채팅 수: {}, 다음 항목 존재: {}, 다음 커서: {}",
                chatHistory.size(), hasNextPage, nextCursor);

        return resultDataBox;
    }

    /**
     * 시험모드 문제/답변 내역 조회
     * @param box 요청 파라미터가 담긴 RequestBox
     * @return 처리 결과가 담긴 DataBox
     * @throws BusinessException 비즈니스 예외 발생 시
     */
    public DataBox getExamModeHistory(RequestBox box) {
        Long userContentSeq = box.getLong("userContentSeq");
        Long cursor = box.getLong("cursor");
        int size = box.getInt("size");
        String orderBy = box.getString("orderBy");
        String direction = box.getString("direction");
        String uuid = box.getString("uuid");

        // 테이블 별칭 설정 (없을 경우를 대비)
        if (box.get("cursorTable") == null) {
            box.put("cursorTable", "E");
        }

        logger.debug("시험모드 문제/답변 내역 조회 시작 - 사용자 콘텐츠 일련번호: {}, 커서: {}, 페이지 크기: {}, 정렬: {} {}, UUID: {}",
                userContentSeq, cursor, size, orderBy, direction, uuid);

        // 1. 사용자-콘텐츠 정보 조회
        DataBox userContentInfo = getUserContentInfo(userContentSeq, uuid);
        Long contentSeq = userContentInfo.getLong2("d_content_seq");

        // 2. 시험 내역 조회
        RequestBox examHistoryBox = new RequestBox("examHistoryBox");
        examHistoryBox.put("uuid", uuid);
        examHistoryBox.put("contentSeq", contentSeq);
        examHistoryBox.put("cursor", cursor);
        examHistoryBox.put("size", size + 1); // 추가 데이터가 있는지 확인을 위해 요청 크기 + 1
        examHistoryBox.put("orderBy", orderBy);
        examHistoryBox.put("direction", direction);

        List<DataBox> examHistory = examDAO.selectExamHistoryWithCursor(examHistoryBox);

        boolean hasNextPage = examHistory.size() > size;
        if (hasNextPage) {
            examHistory.remove(examHistory.size() - 1); // 마지막 항목 제거
        }

        Long nextCursor = null;
        if (hasNextPage && !examHistory.isEmpty()) {
            nextCursor = examHistory.get(examHistory.size() - 1).getLong2("d_exam_seq");
        }

        // 3. 결과 DataBox 구성
        DataBox resultDataBox = new DataBox();
        resultDataBox.put("d_exam_history", examHistory);
        resultDataBox.put("d_has_more", hasNextPage);
        resultDataBox.put("d_next_cursor", nextCursor);

        logger.debug("시험모드 문제/답변 내역 조회 완료 - 시험 수: {}, 다음 항목 존재: {}, 다음 커서: {}",
                examHistory.size(), hasNextPage, nextCursor);

        return resultDataBox;
    }

    /**
     * 학습모드 질문 처리
     * @param box 요청 파라미터가 담긴 RequestBox
     * @return 처리 결과가 담긴 DataBox
     * @throws BusinessException 비즈니스 예외 발생 시
     */
    public DataBox processLearningModeQuestion(RequestBox box) {
        Long userContentSeq = box.getLong("userContentSeq");
        String question = box.getString("question");
        String uuid = box.getString("uuid");

        logger.debug("학습모드 질문 처리 시작 - 사용자 콘텐츠 일련번호: {}, 질문: {}, UUID: {}",
                userContentSeq, question, uuid);

        // 1. 입력값 검증
        validateQuestionInput(question);

        // 2. 사용자-콘텐츠 정보 조회
        DataBox userContentInfo = getUserContentInfo(userContentSeq, uuid);
        Long contentSeq = userContentInfo.getLong2("d_content_seq");

        // 3. 벡터 DB 검색 호출 (RAG)
        String retrievedDocuments = retrieveDocumentsFromVectorDB(userContentSeq, question);

        // 4. 프롬프트 조회 및 변수 설정
        DataBox promptInfo = getPromptInfo(PROMPT_LEARNING_MODE);
        DataBox processedPrompt = preparePromptForLearningMode(promptInfo, userContentInfo, question, retrievedDocuments);

        // 5. GPT API 호출 (대화 이력 활용)
        String gptResponse = callGptApi(processedPrompt, uuid, contentSeq, "learning");

        // 6. 채팅 저장
        Long chatSeq = saveChat(contentSeq, uuid, question, gptResponse);

        // 7. 결과 DataBox 구성
        DataBox resultDataBox = new DataBox();
        resultDataBox.put("d_chat_seq", chatSeq != null ? chatSeq : 0L);
        resultDataBox.put("d_answer", gptResponse);

        logger.debug("학습모드 질문 처리 완료 - chatSeq: {}, 응답길이: {}",
                chatSeq, gptResponse != null ? gptResponse.length() : 0);

        return resultDataBox;
    }

    /**
     * 시험모드 문제 생성
     * @param box 요청 파라미터가 담긴 RequestBox
     * @return 생성된 문제 정보가 담긴 DataBox
     * @throws BusinessException 비즈니스 예외 발생 시
     */
    public DataBox generateExamQuestion(RequestBox box) {
        Long userContentSeq = box.getLong("userContentSeq");
        String uuid = box.getString("uuid");

        logger.debug("시험모드 문제 생성 시작 - 사용자 콘텐츠 일련번호: {}, UUID: {}", userContentSeq, uuid);

        // 1. 사용자-콘텐츠 정보 조회
        DataBox userContentInfo = getUserContentInfo(userContentSeq, uuid);
        Long contentSeq = userContentInfo.getLong2("d_content_seq");

        // 2. 미답변 문제 확인
        DataBox pendingQuestion = checkPendingQuestion(uuid, contentSeq);
        if (pendingQuestion != null) {
            return pendingQuestion;
        }

        // 3. 문제 개수 확인
        int questionCount = examDAO.selectExamCount(uuid, contentSeq);
        if (questionCount >= MAX_QUESTION_COUNT) {
            logger.debug("최대 문제 수 도달 - 현재 문제 수: {}, 최대 문제 수: {}", questionCount, MAX_QUESTION_COUNT);

            DataBox resultDataBox = new DataBox();
            resultDataBox.put("d_status", "completed");
            resultDataBox.put("d_message", "문제가 모두 생성되었습니다! 다음 탭으로 이동해 학습을 이어서 진행하세요!");
            return resultDataBox;
        }

        // 4. 문제 생성 로직 준비
        int currentQuestionNumber = questionCount + 1;
        boolean isLastQuestion = (questionCount >= MAX_QUESTION_COUNT - 1);
        String question = generateQuestion(userContentInfo, currentQuestionNumber, uuid, contentSeq);

        // 5. DB에 문제 저장
        Long examSeq = saveExam(contentSeq, uuid, question);

        // 6. 응답 구성
        DataBox resultDataBox = new DataBox();
        resultDataBox.put("d_status", "success");
        resultDataBox.put("d_question_number", currentQuestionNumber);
        resultDataBox.put("d_total_questions", MAX_QUESTION_COUNT);
        resultDataBox.put("d_question", question);
        resultDataBox.put("d_is_last_question", isLastQuestion);

        logger.debug("시험모드 문제 생성 완료 - 문제번호: {}/{}, 마지막문제: {}",
                currentQuestionNumber, MAX_QUESTION_COUNT, isLastQuestion);

        return resultDataBox;
    }

    /**
     * 시험모드에서 사용자 답변을 평가하는 메서드
     * @param box 요청 파라미터가 담긴 RequestBox
     * @return 평가 결과가 담긴 DataBox
     * @throws BusinessException 비즈니스 예외 발생 시
     */
    public DataBox evaluateAnswer(RequestBox box) {
        Long userContentSeq = box.getLong("userContentSeq");
        int questionNumber = box.getInt("questionNumber");
        String userAnswer = box.getString("answer");
        String uuid = box.getString("uuid");
        boolean isDontKnow = box.getBoolean("isDontKnow");

        logger.debug("답변 평가 시작 - 사용자 콘텐츠 일련번호: {}, 문제 번호: {}, 모르겠어요: {}",
                userContentSeq, questionNumber, isDontKnow);

        // 1. 사용자-콘텐츠 정보 조회
        DataBox userContentInfo = getUserContentInfo(userContentSeq, uuid);
        Long contentSeq = userContentInfo.getLong2("d_content_seq");

        // 2. 문제 정보 조회
        DataBox examInfo = getExamInfo(uuid, contentSeq, questionNumber);

        // 3. 이미 답변한 문제인지 확인
        if (!StringUtils.isBlank(examInfo.getString("d_answer_content"))) {
            logger.debug("이미 답변한 문제 - 문제번호: {}", questionNumber);

            DataBox resultDataBox = new DataBox();
            resultDataBox.put("d_status", "already_answered");
            resultDataBox.put("d_message", "이미 답변한 문제입니다. 다음 문제로 진행해주세요.");
            return resultDataBox;
        }

        // 4. 답변 평가
        Map<String, String> evaluationResult = evaluateUserAnswer(examInfo, userAnswer, isDontKnow, uuid, contentSeq);

        // 5. 답변 및 평가 결과 저장
        updateExamWithEvaluation(examInfo.getLong2("d_exam_seq"), userAnswer,
                evaluationResult.get("modelAnswer"), evaluationResult.get("evaluation"));

        // 6. 응답 구성
        DataBox resultDataBox = new DataBox();
        resultDataBox.put("d_result", evaluationResult.get("result"));
        resultDataBox.put("d_feedback", evaluationResult.get("feedback"));
        resultDataBox.put("d_is_last_question", Boolean.parseBoolean(evaluationResult.get("isLastQuestion")));
        resultDataBox.put("d_model_answer", evaluationResult.get("modelAnswer"));

        logger.debug("답변 평가 완료 - 문제번호: {}, 결과: {}", questionNumber, evaluationResult.get("result"));

        return resultDataBox;
    }

    /**
     * 질문 입력값 검증
     * @param question 사용자 질문
     * @throws BusinessException 입력값이 유효하지 않을 경우
     */
    private void validateQuestionInput(String question) {
        if (StringUtils.isBlank(question)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "질문을 입력해주세요.");
        }

        if (question.length() > MAX_QUESTION_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "질문은 " + MAX_QUESTION_LENGTH + "자 이내로 입력해주세요.");
        }
    }

    /**
     * 사용자-콘텐츠 정보 조회
     * @param userContentSeq 사용자 콘텐츠 일련번호
     * @param uuid 사용자 UUID
     * @return 사용자-콘텐츠 정보
     * @throws BusinessException 콘텐츠를 찾을 수 없는 경우
     */
    private DataBox getUserContentInfo(Long userContentSeq, String uuid) {
        RequestBox contentBox = new RequestBox("contentBox");
        contentBox.put("userContentSeq", userContentSeq);
        contentBox.put("uuid", uuid);

        DataBox userContentInfo = contentService.selectContentByUserContentSeq(contentBox);

        if (userContentInfo == null) {
            logger.error("콘텐츠 정보를 찾을 수 없음 - 사용자 콘텐츠 일련번호: {}, UUID: {}", userContentSeq, uuid);
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠 정보를 찾을 수 없습니다.");
        }

        return userContentInfo;
    }

    /**
     * 프롬프트 정보 조회
     * @param promptSeq 프롬프트 시퀀스
     * @return 프롬프트 정보
     * @throws BusinessException 프롬프트를 찾을 수 없는 경우
     */
    private DataBox getPromptInfo(String promptSeq) {
        RequestBox promptBox = new RequestBox("promptBox");
        promptBox.put("p_seq", promptSeq);
        DataBox promptInfo = aiService.selectAiPrompt(promptBox);

        if (promptInfo == null) {
            logger.error("프롬프트 정보를 찾을 수 없음 - 프롬프트 시퀀스: {}", promptSeq);
            throw new BusinessException(ErrorCode.AI_PROMPT_NOT_FOUND, "요청한 프롬프트를 찾을 수 없습니다.");
        }

        return promptInfo;
    }

    /**
     * 학습모드를 위한 프롬프트 준비
     */
    private DataBox preparePromptForLearningMode(DataBox promptInfo, DataBox userContentInfo,
                                                 String question, String retrievedDocuments) {
        DataBox processedPrompt = new DataBox();
        processedPrompt.put("d_system", promptInfo.getString("d_system"));
        processedPrompt.put("d_user", promptInfo.getString("d_user"));
        processedPrompt.put("d_assistant", promptInfo.getString("d_assistant"));

        Map<String, Object> placeholders = new HashMap<>();
        Map<String, Object> userPlaceholders = new HashMap<>();
        userPlaceholders.put("learningContent", userContentInfo.getString("d_title") + "\n" + userContentInfo.getString("d_description"));
        userPlaceholders.put("userQuestion", question);
        userPlaceholders.put("retrievedDocuments", retrievedDocuments);
        placeholders.put("user", userPlaceholders);

        processTemplate(processedPrompt, placeholders);
        return processedPrompt;
    }

    /**
     * 미답변 문제 확인
     * @param uuid 사용자 UUID
     * @param contentSeq 콘텐츠 일련번호
     * @return 미답변 문제 정보 또는 null
     */
    private DataBox checkPendingQuestion(String uuid, Long contentSeq) {
        boolean hasUnansweredQuestions = examDAO.hasUnansweredQuestions(uuid, contentSeq);

        if (hasUnansweredQuestions) {
            int latestQuestion = examDAO.getLatestQuestionNumber(uuid, contentSeq);
            DataBox questionStatus = examDAO.checkQuestionStatus(uuid, contentSeq, latestQuestion);

            if (questionStatus != null &&
                    StringUtils.isBlank(questionStatus.getString("d_answer_content")) &&
                    StringUtils.isBlank(questionStatus.getString("d_model_answer"))) {

                logger.debug("미답변 문제 발견 - 문제 번호: {}", latestQuestion);

                DataBox resultDataBox = new DataBox();
                resultDataBox.put("d_status", "pending");
                resultDataBox.put("d_message", "이전 문제에 먼저 답변해주세요!");
                resultDataBox.put("d_question_number", latestQuestion);
                resultDataBox.put("d_total_questions", MAX_QUESTION_COUNT);
                resultDataBox.put("d_question", questionStatus.getString("d_question_content"));
                return resultDataBox;
            }
        }

        return null;
    }

    /**
     * 시험 문제 생성
     * @param userContentInfo 사용자-콘텐츠 정보
     * @param questionNumber 문제 번호
     * @param uuid 사용자 UUID
     * @param contentSeq 콘텐츠 일련번호
     * @return 생성된 문제
     * @throws BusinessException 문제 생성 중 오류 발생 시
     */
    private String generateQuestion(DataBox userContentInfo, int questionNumber, String uuid, Long contentSeq) {
        try {
            // 1. 프롬프트 조회 및 준비
            DataBox promptInfo = getPromptInfo(PROMPT_EXAM_QUESTION);

            DataBox processedPrompt = new DataBox();
            processedPrompt.put("d_system", promptInfo.getString("d_system"));
            processedPrompt.put("d_user", promptInfo.getString("d_user"));
            processedPrompt.put("d_assistant", promptInfo.getString("d_assistant"));

            Map<String, Object> placeholders = new HashMap<>();
            Map<String, Object> userPlaceholders = new HashMap<>();
            userPlaceholders.put("learningContent", userContentInfo.getString("d_title") + "\n" + userContentInfo.getString("d_description"));
            userPlaceholders.put("currentQuestionNumber", String.valueOf(questionNumber));
            placeholders.put("user", userPlaceholders);

            processTemplate(processedPrompt, placeholders);

            // 2. GPT API 호출 (대화 이력 활용)
            String gptResponse = callGptApi(processedPrompt, uuid, contentSeq, "exam");

            // 3. JSON 응답 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode;
            try {
                rootNode = mapper.readTree(gptResponse);
            } catch (Exception e) {
                logger.error("GPT 응답 JSON 파싱 오류: {}", e.getMessage(), e);
                throw new BusinessException(ErrorCode.AI_RESPONSE_PARSING_FAILED, "AI 응답 형식이 유효하지 않습니다.");
            }

            String question = rootNode.path("question").asText();
            if (StringUtils.isBlank(question)) {
                logger.error("GPT 응답에서 문제를 찾을 수 없음: {}", gptResponse);
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "AI가 문제를 생성하지 못했습니다. 다시 시도해주세요.");
            }

            return question;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("문제 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "문제 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 시험 문제 정보 조회
     * @param uuid 사용자 UUID
     * @param contentSeq 콘텐츠 일련번호
     * @param questionNumber 문제 번호
     * @return 문제 정보
     * @throws BusinessException 문제를 찾을 수 없는 경우
     */
    private DataBox getExamInfo(String uuid, Long contentSeq, int questionNumber) {
        DataBox examInfo = examDAO.selectExam(uuid, contentSeq, questionNumber);

        if (examInfo == null) {
            logger.error("문제 정보를 찾을 수 없음 - 사용자: {}, 콘텐츠: {}, 문제번호: {}",
                    uuid, contentSeq, questionNumber);
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND, "문제 정보를 찾을 수 없습니다.");
        }

        return examInfo;
    }

    /**
     * 사용자 답변 평가
     * @param examInfo 문제 정보
     * @param userAnswer 사용자 답변
     * @param isDontKnow "모르겠어요" 여부
     * @param uuid 사용자 UUID
     * @param contentSeq 콘텐츠 일련번호
     * @return 평가 결과
     * @throws BusinessException 평가 중 오류 발생 시
     */
    private Map<String, String> evaluateUserAnswer(DataBox examInfo, String userAnswer,
                                                   boolean isDontKnow, String uuid, Long contentSeq) {
        try {
            // 1. 프롬프트 조회 및 준비
            DataBox promptInfo = getPromptInfo(PROMPT_EXAM_EVALUATION);

            DataBox processedPrompt = new DataBox();
            processedPrompt.put("d_system", promptInfo.getString("d_system"));
            processedPrompt.put("d_user", promptInfo.getString("d_user"));
            processedPrompt.put("d_assistant", promptInfo.getString("d_assistant"));

            Map<String, Object> placeholders = new HashMap<>();
            Map<String, Object> userPlaceholders = new HashMap<>();

            userPlaceholders.put("question", examInfo.getString("d_question_content"));
            userPlaceholders.put("userAnswer", userAnswer);
            userPlaceholders.put("isDontKnow", String.valueOf(isDontKnow));

            // 마지막 문제인지 확인
            int questionCount = examDAO.selectExamCount(uuid, contentSeq);
            boolean isLastQuestion = questionCount >= MAX_QUESTION_COUNT;
            userPlaceholders.put("isLastQuestion", String.valueOf(isLastQuestion));
            userPlaceholders.put("questionNumber", String.valueOf(questionCount+1));

            placeholders.put("user", userPlaceholders);
            processTemplate(processedPrompt, placeholders);

            // 2. GPT API 호출 (대화 이력 활용)
            String gptResponse = callGptApi(processedPrompt, uuid, contentSeq, "exam");

            // 3. JSON 응답 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode;
            try {
                rootNode = mapper.readTree(gptResponse);
            } catch (Exception e) {
                logger.error("GPT 응답 JSON 파싱 오류: {}", e.getMessage(), e);
                throw new BusinessException(ErrorCode.AI_RESPONSE_PARSING_FAILED, "AI 응답을 처리할 수 없습니다.");
            }

            Map<String, String> result = new HashMap<>();
            result.put("result", rootNode.path("result").asText());
            result.put("feedback", rootNode.path("feedback").asText());
            result.put("isLastQuestion", String.valueOf(rootNode.path("is_last_question").asBoolean()));
            result.put("modelAnswer", rootNode.path("model_answer").asText());

            // 평가 결과 코드 설정
            if (isDontKnow) {
                result.put("evaluation", "P"); // Poor - 모르겠어요
            } else if ("correct".equals(result.get("result"))) {
                result.put("evaluation", "E"); // Excellent
            } else if ("incorrect".equals(result.get("result"))) {
                result.put("evaluation", "F"); // Fair
            } else {
                result.put("evaluation", "P"); // Poor - irrelevant
            }

            validateEvaluationResult(result);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("답변 평가 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "답변 평가 중 오류가 발생했습니다.");
        }
    }

    /**
     * 평가 결과 유효성 검증
     * @param result 평가 결과
     * @throws BusinessException 검증 실패 시
     */
    private void validateEvaluationResult(Map<String, String> result) {
        if (StringUtils.isBlank(result.get("result")) ||
                StringUtils.isBlank(result.get("feedback")) ||
                StringUtils.isBlank(result.get("modelAnswer"))) {

            logger.error("답변 평가 결과 검증 실패 - 필수 필드 누락: {}", result);
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "답변 평가 결과가 유효하지 않습니다.");
        }

        // 평가 결과 값 검증
        String evaluationResult = result.get("result");
        if (!("correct".equals(evaluationResult) ||
                "incorrect".equals(evaluationResult) ||
                "irrelevant".equals(evaluationResult))) {

            logger.error("답변 평가 결과 검증 실패 - 유효하지 않은 평가 결과: {}", evaluationResult);
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "유효하지 않은 평가 결과입니다.");
        }
    }

    /**
     * 프롬프트 템플릿 처리
     * 템플릿에서 ${key} 형태의 변수를 찾아 placeholders의 값으로 대체합니다.
     */
    private void processTemplate(DataBox dboxPrompt, Map<String, Object> placeholders) {
        String[] roleArr = {"system", "user", "assistant"};

        for (String role : roleArr) {
            if (placeholders.containsKey(role)) {
                Map<String, Object> getPlaceholders = (Map<String, Object>) placeholders.get(role);
                String template = dboxPrompt.getString("d_" + role);

                if (template != null && !StringUtils.isBlank(template)) {
                    // 템플릿에서 `${key}` 형식으로 변수를 찾기 위한 정규식
                    Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
                    Matcher matcher = pattern.matcher(template);
                    StringBuffer result = new StringBuffer();

                    while (matcher.find()) {
                        String placeholder = matcher.group(1);
                        String replacement = null;

                        // placeholders에서 key에 해당하는 값을 찾아 대체
                        if (getPlaceholders.containsKey(placeholder)) {
                            replacement = String.valueOf(getPlaceholders.get(placeholder));
                        } else {
                            // 변수가 없을 경우 원본 유지
                            replacement = matcher.group(0);
                            logger.warn("변수 {}에 해당하는 값을 찾을 수 없습니다.", placeholder);
                        }

                        // 치환할 때 replacement 문자열에 Matcher.quoteReplacement를 적용
                        matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                    }

                    matcher.appendTail(result);

                    // 치환된 템플릿을 dboxPrompt에 다시 설정
                    dboxPrompt.put("d_" + role, result.toString());
                }
            }
        }
    }

    /**
     * GPT API 호출 - 이전 대화 이력 활용
     * @param promptData 프롬프트 데이터
     * @param uuid 사용자 UUID
     * @param contentSeq 콘텐츠 일련번호
     * @param mode 대화 모드 ("learning" 또는 "exam")
     * @return API 응답
     * @throws BusinessException API 호출 중 오류 발생 시
     */
    private String callGptApi(DataBox promptData, String uuid, Long contentSeq, String mode) {
        try {
            // OpenAI API URL
            String url = openAiApiUrl + "/v1/chat/completions";

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Accept-Charset", "UTF-8");

            // 메시지 배열 구성
            List<Map<String, String>> messages = new ArrayList<>();

            // System 메시지 추가
            if (StringUtils.isNotBlank(promptData.getString("d_system"))) {
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", promptData.getString("d_system"));
                messages.add(systemMessage);
            }

            // 대화 이력 포함
            List<DataBox> chatHistory = new ArrayList<>();
            if (StringUtils.isNotBlank(mode)) {
                // 대화 이력 조회를 위한 RequestBox 구성
                RequestBox historyBox = new RequestBox("historyBox");
                historyBox.put("uuid", uuid);
                historyBox.put("contentSeq", contentSeq);
                historyBox.put("limit", MAX_CHAT_HISTORY);

                // 모드에 따라 적절한 대화 이력 조회
                if ("learning".equals(mode)) {
                    chatHistory = chatDAO.selectRecentChats(historyBox);
                } else if ("exam".equals(mode)) {
                    chatHistory = examDAO.selectRecentExams(historyBox);
                }

                // 이전 대화 내역을 메시지에 추가 (오래된 것부터)
                for (DataBox chat : chatHistory) {
                    // 사용자 질문 추가
                    if (StringUtils.isNotBlank(chat.getString("d_question"))) {
                        Map<String, String> userMessage = new HashMap<>();
                        userMessage.put("role", "user");
                        userMessage.put("content", chat.getString("d_question"));
                        messages.add(userMessage);
                    }

                    // 시스템 응답 추가
                    if (StringUtils.isNotBlank(chat.getString("d_answer"))) {
                        Map<String, String> assistantMessage = new HashMap<>();
                        assistantMessage.put("role", "assistant");
                        assistantMessage.put("content", chat.getString("d_answer"));
                        messages.add(assistantMessage);
                    }
                }

                logger.debug("대화 이력을 포함한 GPT API 호출 - 모드: {}, 이력 개수: {}", mode, chatHistory.size());
            } else {
                // 대화 이력이 없는 경우 assistant 메시지 추가 (필요시)
                if (StringUtils.isNotBlank(promptData.getString("d_assistant"))) {
                    Map<String, String> assistantMessage = new HashMap<>();
                    assistantMessage.put("role", "assistant");
                    assistantMessage.put("content", promptData.getString("d_assistant"));
                    messages.add(assistantMessage);
                }
            }

            // 현재 사용자 메시지 추가
            if (StringUtils.isNotBlank(promptData.getString("d_user"))) {
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", promptData.getString("d_user"));
                messages.add(userMessage);
            }

            // 요청 본문 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            logger.debug("GPT API 요청 메시지 수: {}", messages.size());
            logger.debug("requestBody : {}", requestBody.toString());

            // RestTemplate으로 API 호출
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            logger.debug("responseBody : {}", response.getBody().toString());

            // 응답에서 내용 추출
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                logger.error("GPT API 응답에 choices가 없음: {}", responseBody);
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "AI 응답이 유효하지 않습니다.");
            }

            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            if (message == null) {
                logger.error("GPT API 응답에 message가 없음: {}", choice);
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "AI 응답이 유효하지 않습니다.");
            }

            String content = (String) message.get("content");
            if (StringUtils.isBlank(content)) {
                logger.error("GPT API 응답 content가 비어있음: {}", message);
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "AI가 응답을 생성하지 못했습니다.");
            }

            return content;

        } catch (HttpClientErrorException e) {
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            logger.error("GPT API 클라이언트 오류: 상태 코드={}, 응답={}", statusCode, responseBody, e);

            // HTTP 상태 코드에 따른 세분화된 예외 처리
            if (statusCode == 401) {
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI 서비스 인증에 실패했습니다.");
            } else if (statusCode == 429) {
                throw new BusinessException(ErrorCode.AI_QUOTA_EXCEEDED, "AI 서비스 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
            } else if (statusCode == 400) {
                if (responseBody.contains("content_filter") || responseBody.contains("moderation")) {
                    throw new BusinessException(ErrorCode.AI_CONTENT_MODERATION_FAILED, "콘텐츠 검수에 실패했습니다.");
                } else if (responseBody.contains("context_length") || responseBody.contains("token")) {
                    throw new BusinessException(ErrorCode.AI_CONTEXT_TOO_LARGE, "입력 데이터가 너무 큽니다.");
                }
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI 요청 처리에 실패했습니다.");
            } else {
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI 서비스 호출 중 오류가 발생했습니다.");
            }
        } catch (HttpServerErrorException e) {
            logger.error("GPT API 서버 오류: 상태 코드={}, 응답={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE, "AI 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
        } catch (BusinessException e) {
            // 이미 적절한 BusinessException이므로 그대로 전파
            throw e;
        } catch (Exception e) {
            logger.error("GPT API 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE, "AI 서비스 연결에 실패했습니다.");
        }
    }

    /**
     * 학습 내용을 벡터DB에서 검색하는 메서드
     * @param userContentSeq 사용자-콘텐츠 연결 일련번호
     * @param question 사용자 질문
     * @return 검색된 문서 내용
     */
    private String retrieveDocumentsFromVectorDB(Long userContentSeq, String question) {
        // TODO: 벡터 DB 검색 기능 구현
        logger.debug("벡터 DB 검색 요청 - userContentSeq: {}, question: {}", userContentSeq, question);
        return "검색된 관련 문서 내용입니다. 실제 구현 시 벡터 DB에서 검색된 결과가 제공됩니다.";
    }

    /**
     * 채팅 내용을 저장하는 메서드
     * @param contentSeq 콘텐츠 일련번호
     * @param uuid 사용자 UUID
     * @param question 질문
     * @param answer 답변
     * @return 생성된 채팅 일련번호
     */
    private Long saveChat(Long contentSeq, String uuid, String question, String answer) {
        try {
            String currentDateTime = FormatDate.getDate("yyyyMMddHHmmss");

            RequestBox box = new RequestBox("chatBox");
            box.put("contentSeq", contentSeq);
            box.put("uuid", uuid);
            box.put("question", question);
            box.put("answer", answer);
            box.put("indate", currentDateTime);

            chatDAO.insertChat(box);
            return box.getLong("chatSeq");

        } catch (Exception e) {
            logger.error("채팅 저장 중 오류 발생: {}", e.getMessage(), e);
            // 저장 실패는 비즈니스 로직에 영향을 주지 않도록 예외를 던지지 않음
            return 0L;
        }
    }

    /**
     * 시험 문제를 저장하는 메서드
     * @param contentSeq 콘텐츠 일련번호
     * @param uuid 사용자 UUID
     * @param question 문제
     * @return 생성된 시험 일련번호
     * @throws BusinessException 저장 중 오류 발생 시
     */
    private Long saveExam(Long contentSeq, String uuid, String question) {
        try {
            String currentDateTime = FormatDate.getDate("yyyyMMddHHmmss");

            RequestBox box = new RequestBox("examBox");
            box.put("contentSeq", contentSeq);
            box.put("uuid", uuid);
            box.put("difficulty", "M");
            box.put("questionContent", question);
            box.put("indate", currentDateTime);
            box.put("ldate", currentDateTime);

            examDAO.insertExam(box);
            Long examSeq = box.getLong("examSeq");

            if (examSeq == null || examSeq == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "시험 문제 저장 결과가 유효하지 않습니다.");
            }

            return examSeq;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("시험 문제 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "시험 문제 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 시험 정보 업데이트 (사용자 답변, 평가 결과, 모범답안)
     * @param examSeq 시험 일련번호
     * @param userAnswer 사용자 답변
     * @param modelAnswer 모범 답안
     * @param evaluationResult 평가 결과
     * @throws BusinessException 업데이트 중 오류 발생 시
     */
    private void updateExamWithEvaluation(Long examSeq, String userAnswer, String modelAnswer, String evaluationResult) {
        try {
            String currentDateTime = FormatDate.getDate("yyyyMMddHHmmss");

            RequestBox box = new RequestBox("examUpdateBox");
            box.put("examSeq", examSeq);
            box.put("answerContent", userAnswer);
            box.put("modelAnswer", modelAnswer);
            box.put("evaluation", evaluationResult);
            box.put("ldate", currentDateTime);

            int updateCount = examDAO.updateExam(box);

            if (updateCount < 1) {
                throw new BusinessException(ErrorCode.UPDATE_FAILED, "시험 정보 업데이트에 실패했습니다.");
            }

            logger.debug("시험 정보 업데이트 완료 - examSeq: {}, 결과: {}", examSeq, evaluationResult);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("시험 정보 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "시험 정보 업데이트 중 오류가 발생했습니다.");
        }
    }
}