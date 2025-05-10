package com.firzzle.learning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.learning.dao.ChatDAO;
import com.firzzle.learning.dao.ExamDAO;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    // 임시로 모범답안을 저장하기 위한 맵 (examSeq -> modelAnswer)
    private Map<Long, String> pendingModelAnswer = new ConcurrentHashMap<>();

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    public DataBox processLearningModeQuestion(RequestBox box) {
        try {
            Long userContentSeq = box.getLong("userContentSeq");
            String question = box.getString("question");
            String uuid = box.getString("uuid"); // HTTP 요청에서 가져온 uuid

            logger.debug("학습모드 질문 처리 - 사용자 콘텐츠 일련번호: {}, 질문: {}, UUID: {}",
                    userContentSeq, question, uuid);

            // 1. 입력값 검증
            if (question.length() > 200) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "질문은 200자 이내로 입력해주세요.");
            }

            // 2. 사용자-콘텐츠 정보 조회
            RequestBox contentBox = new RequestBox("contentBox");
            contentBox.put("userContentSeq", userContentSeq);
            contentBox.put("uuid", uuid); // uuid 설정

            DataBox userContentInfo = contentService.selectContent(contentBox);

            if (userContentInfo == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠 정보를 찾을 수 없습니다.");
            }

            // 3. 벡터 DB 검색 호출 (RAG)
            // TODO: 추후 벡터 DB 연동 구현 예정
            String retrievedDocuments = retrieveDocumentsFromVectorDB(userContentSeq, question);

            // 4. 프롬프트 조회
            RequestBox promptBox = new RequestBox("promptBox");
            promptBox.put("p_seq", "AIQ001");
            DataBox promptInfo = aiService.selectAiPrompt(promptBox);

            if (promptInfo == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "프롬프트 정보를 찾을 수 없습니다.");
            }

            // 5. 프롬프트 변수 설정 및 치환
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

            logger.debug("placeholders : {}", placeholders.toString());
            logger.debug("userPlaceholders : {}", userPlaceholders.toString());

            processTemplate(processedPrompt, placeholders);

            // 6. GPT API 호출
            String gptResponse = callGptApi(processedPrompt);
            logger.debug("GPT 응답: {}", gptResponse);

            // 7. 채팅 저장 - uuid 사용하도록 수정
            Long contentSeq = userContentInfo.getLong2("d_content_seq");

            Long chatSeq = null;
            try {
                chatSeq = saveChat(contentSeq, uuid, question, gptResponse, "AIQ001");
                logger.debug("채팅 저장 완료 - chatSeq: {}", chatSeq);
            } catch (Exception e) {
                logger.error("채팅 저장 중 오류 발생: {}", e.getMessage(), e);
                // 채팅 저장 실패는 비즈니스 로직에 영향을 주지 않도록 예외를 던지지 않음
            }

            // 8. 결과 DataBox 구성
            DataBox resultDataBox = new DataBox();
            resultDataBox.put("d_chat_seq", chatSeq != null ? chatSeq : 0L);
            resultDataBox.put("d_answer", gptResponse);

            logger.debug("학습모드 질문 처리 결과: chatSeq={}, 응답길이={}",
                    chatSeq, gptResponse != null ? gptResponse.length() : 0);

            return resultDataBox;

        } catch (BusinessException e) {
            logger.error("학습모드 처리 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("학습모드 처리 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "학습모드 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 시험모드 문제 생성
     * @param box 요청 파라미터가 담긴 RequestBox
     * @return 생성된 문제 정보가 담긴 DataBox
     */
    public DataBox generateExamQuestion(RequestBox box) {
        try {
            Long userContentSeq = box.getLong("userContentSeq");
            String uuid = box.getString("uuid");

            logger.debug("시험모드 문제 생성 - 사용자 콘텐츠 일련번호: {}, UUID: {}", userContentSeq, uuid);

            // 사용자-콘텐츠 정보 조회
            RequestBox contentBox = new RequestBox("contentBox");
            contentBox.put("userContentSeq", userContentSeq);
            contentBox.put("uuid", uuid);

            DataBox userContentInfo = contentService.selectContent(contentBox);

            if (userContentInfo == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠 정보를 찾을 수 없습니다.");
            }

            Long contentSeq = userContentInfo.getLong2("d_content_seq");

            // 1. 미답변 문제가 있는지 확인
            boolean hasUnansweredQuestions = examDAO.hasUnansweredQuestions(uuid, contentSeq);

            if (hasUnansweredQuestions) {
                // 미답변 문제가 있다면 해당 문제 정보 반환
                int latestQuestion = examDAO.getLatestQuestionNumber(uuid, contentSeq);
                DataBox questionStatus = examDAO.checkQuestionStatus(uuid, contentSeq, latestQuestion);

                if (questionStatus != null &&
                        StringUtils.isBlank(questionStatus.getString("d_answer_content")) && StringUtils.isBlank(questionStatus.getString("d_model_answer"))) {

                    logger.debug("미답변 문제 발견 - 문제 번호: {}, 내용: {}",
                            latestQuestion, questionStatus.getString("d_question_content"));

                    DataBox resultDataBox = new DataBox();
                    resultDataBox.put("d_status", "pending");
                    resultDataBox.put("d_message", "이전 문제에 먼저 답변해주세요!");
                    resultDataBox.put("d_question_number", latestQuestion);
                    resultDataBox.put("d_total_questions", 3);
                    resultDataBox.put("d_question", questionStatus.getString("d_question_content"));
                    return resultDataBox;
                }
            }

            // 2. 이미 생성된 문제 개수 확인 (한 콘텐츠당 최대 3문제)
            int questionCount = examDAO.selectExamCount(uuid, contentSeq);
            int maxQuestionCount = 3;
            boolean responseIsLastQuestion = (questionCount >= maxQuestionCount-1);

            if (questionCount >= maxQuestionCount) {
                logger.debug("최대 문제 수 도달 - 현재 문제 수: {}, 최대 문제 수: {}", questionCount, maxQuestionCount);
                DataBox resultDataBox = new DataBox();
                resultDataBox.put("d_status", "completed");
                resultDataBox.put("d_message", "문제가 모두 생성되었습니다! 다음 탭으로 이동해 학습을 이어서 진행하세요!");
                return resultDataBox;
            }

            // 현재 문제 번호 계산
            int currentQuestionNumber = questionCount + 1;

            // 3. 프롬프트 조회
            RequestBox promptBox = new RequestBox("promptBox");
            promptBox.put("p_seq", "AIQ002");
            DataBox promptInfo = aiService.selectAiPrompt(promptBox);

            if (promptInfo == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "프롬프트 정보를 찾을 수 없습니다.");
            }

            // 4. 프롬프트 변수 설정 및 치환
            DataBox processedPrompt = new DataBox();
            processedPrompt.put("d_system", promptInfo.getString("d_system"));
            processedPrompt.put("d_user", promptInfo.getString("d_user"));
            processedPrompt.put("d_assistant", promptInfo.getString("d_assistant"));

            Map<String, Object> placeholders = new HashMap<>();
            Map<String, Object> userPlaceholders = new HashMap<>();
            userPlaceholders.put("learningContent", userContentInfo.getString("d_title") + "\n" + userContentInfo.getString("d_description"));
            userPlaceholders.put("currentQuestionNumber", String.valueOf(currentQuestionNumber));
            placeholders.put("user", userPlaceholders);

            processTemplate(processedPrompt, placeholders);

            // 5. GPT API 호출
            String gptResponse = callGptApi(processedPrompt);
            logger.debug("GPT 응답: {}", gptResponse);

            // 6. JSON 응답 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode;
            try {
                rootNode = mapper.readTree(gptResponse);
            } catch (Exception e) {
                logger.error("GPT 응답 JSON 파싱 오류: {}", e.getMessage());
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 응답 처리 중 오류가 발생했습니다.");
            }

            String question = rootNode.path("question").asText();
            if (question == null || question.isEmpty()) {
                logger.error("GPT 응답에서 문제를 찾을 수 없습니다: {}", gptResponse);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 응답에서 문제를 생성할 수 없습니다.");
            }

            // 7. DB에 문제만 저장 (모범답안 제외)
            Long examSeq;
            try {
                examSeq = saveExam(contentSeq, uuid, question);
                logger.debug("시험 문제 저장 완료 - examSeq: {}", examSeq);
            } catch (Exception e) {
                logger.error("시험 문제 저장 중 오류 발생: {}", e.getMessage(), e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "시험 문제 저장 중 오류가 발생했습니다.");
            }

            // 8. 응답 구성
            DataBox resultDataBox = new DataBox();
            resultDataBox.put("d_status", "success");
            resultDataBox.put("d_question_number", currentQuestionNumber);
            resultDataBox.put("d_total_questions", maxQuestionCount);
            resultDataBox.put("d_question", question);
            resultDataBox.put("d_is_last_question", responseIsLastQuestion);

            logger.debug("결과 DataBox: {}", resultDataBox.toString());
            return resultDataBox;

        } catch (BusinessException e) {
            logger.error("시험 문제 생성 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("시험 문제 생성 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "시험 문제 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 시험모드에서 사용자 답변을 평가하는 메서드 (모르겠어요 기능 포함)
     * @param box 요청 파라미터가 담긴 RequestBox
     * @return 평가 결과가 담긴 DataBox
     */
    public DataBox evaluateAnswer(RequestBox box) {
        try {
            Long userContentSeq = box.getLong("userContentSeq");
            int questionNumber = box.getInt("questionNumber");
            String userAnswer = box.getString("answer");
            String uuid = box.getString("uuid");
            boolean isDontKnow = box.getBoolean("isDontKnow");

            logger.debug("답변 평가 - 사용자 콘텐츠 일련번호: {}, 문제 번호: {}, 답변: {}, 모르겠어요: {}, UUID: {}",
                    userContentSeq, questionNumber, userAnswer, isDontKnow, uuid);

            // 1. 사용자-콘텐츠 정보 조회
            RequestBox contentBox = new RequestBox("contentBox");
            contentBox.put("userContentSeq", userContentSeq);
            contentBox.put("uuid", uuid);

            DataBox userContentInfo = contentService.selectContent(contentBox);

            if (userContentInfo == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠 정보를 찾을 수 없습니다.");
            }

            Long contentSeq = userContentInfo.getLong2("d_content_seq");

            // 2. 해당 문제 정보 조회
            DataBox examInfo = examDAO.selectExam(uuid, contentSeq, questionNumber);

            if (examInfo == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "문제 정보를 찾을 수 없습니다.");
            }

            // 이미 답변한 문제인 경우
            if (!StringUtils.isBlank(examInfo.getString("d_answer_content"))) {
                DataBox resultDataBox = new DataBox();
                resultDataBox.put("d_status", "already_answered");
                resultDataBox.put("d_message", "이미 답변한 문제입니다. 다음 문제로 진행해주세요.");
                return resultDataBox;
            }

            // 3. 프롬프트 조회 - 통합된 프롬프트 AIQ003 사용
            RequestBox promptBox = new RequestBox("promptBox");
            promptBox.put("p_seq", "AIQ003");
            DataBox promptInfo = aiService.selectAiPrompt(promptBox);

            if (promptInfo == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "프롬프트 정보를 찾을 수 없습니다.");
            }

            // 4. 프롬프트 변수 설정 및 치환
            DataBox processedPrompt = new DataBox();
            processedPrompt.put("d_system", promptInfo.getString("d_system"));
            processedPrompt.put("d_user", promptInfo.getString("d_user"));
            processedPrompt.put("d_assistant", promptInfo.getString("d_assistant"));

            Map<String, Object> placeholders = new HashMap<>();
            Map<String, Object> userPlaceholders = new HashMap<>();

            // 모든 필요한 변수 설정 (통합 프롬프트용)
            userPlaceholders.put("questionNumber", String.valueOf(questionNumber));
            userPlaceholders.put("question", examInfo.getString("d_question_content"));
            userPlaceholders.put("userAnswer", userAnswer);
            userPlaceholders.put("isDontKnow", String.valueOf(isDontKnow));

            // 마지막 문제인지 확인
            int totalQuestions = 3;
            int questionCount = examDAO.selectExamCount(uuid, contentSeq);
            boolean isLastQuestion = questionCount >= totalQuestions;
            userPlaceholders.put("isLastQuestion", String.valueOf(isLastQuestion));

            placeholders.put("user", userPlaceholders);
            processTemplate(processedPrompt, placeholders);

            // 5. GPT API 호출
            String gptResponse = callGptApi(processedPrompt);
            logger.debug("GPT API 응답: {}", gptResponse);

            // 6. JSON 응답 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode;
            try {
                rootNode = mapper.readTree(gptResponse);
            } catch (Exception e) {
                logger.error("GPT 응답 JSON 파싱 오류: {}", e.getMessage(), e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 응답 처리 중 오류가 발생했습니다.");
            }

            // 7. 응답 처리
            String result = rootNode.path("result").asText();
            String feedback = rootNode.path("feedback").asText();
            boolean responseIsLastQuestion = rootNode.path("is_last_question").asBoolean();
            String modelAnswer = rootNode.path("model_answer").asText();

            // 8. 사용자 답변 및 평가 결과 저장
            try {
                // 평가 결과 설정
                String evaluation;
                if (isDontKnow) {
                    evaluation = "P"; // Poor - 모르겠어요
                } else if ("correct".equals(result)) {
                    evaluation = "E"; // Excellent
                } else if ("incorrect".equals(result)) {
                    evaluation = "F"; // Fair
                } else {
                    evaluation = "P"; // Poor - irrelevant
                }

                updateExam(examInfo.getLong2("d_exam_seq"), userAnswer, modelAnswer, evaluation);
                logger.debug("답변 평가 결과 DB 업데이트 완료 - examSeq: {}, result: {}, evaluation: {}",
                        examInfo.getLong2("d_exam_seq"), result, evaluation);
            } catch (Exception e) {
                logger.error("시험 응답 업데이트 중 오류 발생: {}", e.getMessage(), e);
                // 업데이트 실패는 비즈니스 로직에 영향을 주지 않도록 예외를 던지지 않음
            }

            // 9. 응답 구성
            DataBox resultDataBox = new DataBox();
            resultDataBox.put("d_result", result);
            resultDataBox.put("d_feedback", feedback);
            resultDataBox.put("d_is_last_question", responseIsLastQuestion);
            resultDataBox.put("d_model_answer", modelAnswer);

            logger.debug("답변 평가 결과: result={}, feedback={}, isLastQuestion={}, 모범답안제공={}, 추가설명={}",
                    result, feedback, responseIsLastQuestion,
                    resultDataBox.containsKey("d_model_answer"),
                    resultDataBox.containsKey("d_additional_explanation"));

            return resultDataBox;

        } catch (BusinessException e) {
            logger.error("답변 평가 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("답변 평가 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "답변 평가 중 오류가 발생했습니다.");
        }
    }

    /**
     * 프롬프트 템플릿 처리
     * 템플릿에서 ${key} 형태의 변수를 찾아 placeholders의 값으로 대체합니다.
     */
    private void processTemplate(DataBox dboxPrompt, Map<String, Object> placeholders) {
        String[] roleArr = {"system", "user", "assistant"};

        // 전체 placeholders 키 집합 로깅
        logger.debug("processTemplate 시작 - 전체 placeholders 키: {}", placeholders.keySet());

        for (String role : roleArr) {
            logger.debug("현재 처리 중인 role: {}", role);

            if (placeholders.containsKey(role)) {
                Map<String, Object> getPlaceholders = (Map<String, Object>) placeholders.get(role);
                String template = dboxPrompt.getString("d_" + role);

                // role에 대한 placeholders 키 집합 로깅
                logger.debug("{} role의 placeholders 키: {}", role, getPlaceholders.keySet());

                // 템플릿 원본 내용 로깅
                logger.debug("{} role의 처리 전 템플릿: {}", role, template);

                if (template != null && !StringUtils.isBlank(template)) {
                    // 템플릿에서 `${key}` 형식으로 변수를 찾기 위한 정규식
                    Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
                    Matcher matcher = pattern.matcher(template);
                    StringBuffer result = new StringBuffer();

                    // 발견된 모든 변수 로깅
                    List<String> foundPlaceholders = new ArrayList<>();
                    Matcher preCheckMatcher = pattern.matcher(template);
                    while (preCheckMatcher.find()) {
                        foundPlaceholders.add(preCheckMatcher.group(1));
                    }
                    logger.debug("{} role의 템플릿에서 발견된 변수: {}", role, foundPlaceholders);

                    while (matcher.find()) {
                        String placeholder = matcher.group(1);
                        String replacement = null;

                        // 현재 처리 중인 변수 로깅
                        logger.debug("현재 처리 중인 변수: {}", placeholder);

                        // placeholders에서 key에 해당하는 값을 찾아 대체
                        if (getPlaceholders.containsKey(placeholder)) {
                            replacement = String.valueOf(getPlaceholders.get(placeholder));
                            logger.debug("변수 {}의 값: {}", placeholder, replacement);
                        } else {
                            logger.warn("변수 {}에 해당하는 값을 찾을 수 없습니다!", placeholder);
                        }

                        // 치환할 값 로깅
                        String finalReplacement = replacement != null ? replacement : matcher.group(0);
                        logger.debug("변수 {}를 {}로 치환합니다", placeholder, finalReplacement);

                        // 치환할 때 replacement 문자열에 Matcher.quoteReplacement를 적용
                        matcher.appendReplacement(result, Matcher.quoteReplacement(finalReplacement));
                    }

                    matcher.appendTail(result);

                    // 치환된 결과 로깅
                    logger.debug("{} role의 처리 후 템플릿: {}", role, result.toString());

                    // 치환된 템플릿을 dboxPrompt에 다시 설정
                    dboxPrompt.put("d_" + role, result.toString());
                } else {
                    logger.warn("{} role의 템플릿이 null이거나 비어 있습니다!", role);
                }
            } else {
                logger.warn("placeholders에 {} role이 포함되어 있지 않습니다!", role);
            }
        }

        // 최종 처리 결과 요약 로깅
        logger.debug("processTemplate 완료 - 최종 처리된 템플릿:");
        for (String role : roleArr) {
            if (dboxPrompt.containsKey("d_" + role)) {
                logger.debug("{} role의 최종 템플릿: {}", role, dboxPrompt.getString("d_" + role));
            }
        }
    }

    /**
     * GPT API 호출
     *
     * @param promptData 프롬프트 데이터
     * @return API 응답
     */
    private String callGptApi(DataBox promptData) {
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

            // Assistant 메시지 추가
            if (StringUtils.isNotBlank(promptData.getString("d_assistant"))) {
                Map<String, String> assistantMessage = new HashMap<>();
                assistantMessage.put("role", "assistant");
                assistantMessage.put("content", promptData.getString("d_assistant"));
                messages.add(assistantMessage);
            }

            // User 메시지 추가
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

            logger.debug("requestBody : {}", messages.toString());

            // RestTemplate으로 API 호출
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            logger.debug("response.getBody().toString() : {}", response.getBody().toString());

            // 응답에서 내용 추출
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            String content = (String) message.get("content");

            return content;

        } catch (Exception e) {
            logger.error("GPT API 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "GPT API 호출 중 오류가 발생했습니다.");
        }
    }

    /**
     * 학습 내용을 벡터DB에서 검색하는 메서드
     * @param userContentSeq 사용자-콘텐츠 연결 일련번호
     * @param question 사용자 질문
     * @return 검색된 문서 내용
     */
    private String retrieveDocumentsFromVectorDB(Long userContentSeq, String question) {
        // TODO: 추후 벡터 DB 검색 기능 구현 예정
        // 현재는 임시 데이터 반환
        return "검색된 관련 문서 내용입니다. 실제 구현 시 벡터 DB에서 검색된 결과가 제공됩니다.";
    }

    /**
     * 채팅 내용을 저장하는 메서드
     * @param contentSeq 콘텐츠 일련번호
     * @param uuid 사용자 UUID
     * @param question 질문
     * @param answer 답변
     * @param seq 시퀀스 번호
     * @return 생성된 채팅 일련번호
     * @throws BusinessException 비즈니스 예외 발생 시
     */
    private Long saveChat(Long contentSeq, String uuid, String question, String answer, String seq) {
        try {
            String currentDateTime = FormatDate.getDate("yyyyMMddHHmmss");

            RequestBox box = new RequestBox("chatBox");
            box.put("contentSeq", contentSeq);
            box.put("uuid", uuid); // userSeq 대신 uuid 사용
            box.put("question", question);
            box.put("answer", answer);
            box.put("seq", seq);
            box.put("indate", currentDateTime);

            // fb_ai_chats 테이블에 저장
            chatDAO.insertChat(box);
            Long chatSeq = box.getLong("chatSeq");

            // TODO: 추후 사용자 질문-답변 이력 분석 기능 구현 예정

            return chatSeq;

        } catch (Exception e) {
            logger.error("채팅 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "채팅 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 시험 문제를 저장하는 메서드
     * @param contentSeq 콘텐츠 일련번호
     * @param uuid 사용자 UUID
     * @param question 문제
     * @return 생성된 시험 일련번호
     * @throws BusinessException 비즈니스 예외 발생 시
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

            return examSeq;

        } catch (Exception e) {
            logger.error("시험 문제 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "시험 문제 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 답변 및 평가 결과를 업데이트하는 메서드 (모범답안 포함)
     * @throws BusinessException 비즈니스 예외 발생 시
     */
    private void updateExam(Long examSeq, String userAnswer, String modelAnswer, String evaluationResult) {
        try {
            String currentDateTime = FormatDate.getDate("yyyyMMddHHmmss");

            RequestBox box = new RequestBox("examUpdateBox");
            box.put("examSeq", examSeq);
            box.put("answerContent", userAnswer);

            // 모범답안 설정
            if (StringUtils.isBlank(modelAnswer)) {
                // 임시 저장된 모범답안 사용
                modelAnswer = pendingModelAnswer.getOrDefault(examSeq, "");
                pendingModelAnswer.remove(examSeq); // 사용 후 제거
            }
            box.put("modelAnswer", modelAnswer);

            // 평가 결과에 따른 등급 설정
            String evaluation = "F"; // 기본값 Fair
            if ("correct".equals(evaluationResult)) {
                evaluation = "E"; // Excellent
            } else if ("incorrect".equals(evaluationResult)) {
                evaluation = "F"; // Fair
            } else if ("irrelevant".equals(evaluationResult)) {
                evaluation = "P"; // Poor
            }

            box.put("evaluation", evaluation);
            box.put("ldate", currentDateTime);

            // fb_ai_exams 테이블 업데이트
            examDAO.updateExam(box);
            logger.debug("답변 평가 - examSeq: {}, 답변 및 모범답안 저장", examSeq);

        } catch (Exception e) {
            logger.error("시험 응답 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "시험 응답 업데이트 중 오류가 발생했습니다.");
        }
    }
}