package com.firzzle.learning.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.dto.*;
import com.firzzle.learning.service.LearningChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @Class Name : LearningChatController.java
 * @Description : 러닝챗 API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 9.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
@Tag(name = "러닝챗 API", description = "학습모드 및 시험모드 관련 API")
public class LearningChatController {

    private static final Logger logger = LoggerFactory.getLogger(LearningChatController.class);

    private final LearningChatService learningChatService;

    /**
     * 학습모드 질문
     */
    @PostMapping("/{contentSeq}/chat")
    @Operation(summary = "학습모드 질문", description = "학습모드에서 사용자 질문에 답변합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<LearningChatResponseDTO>> learningModeQuestion(
            @Parameter(description = "사용자 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            @Parameter(description = "질문 정보", required = true) @Valid @RequestBody LearningChatRequestDTO requestDTO,
            HttpServletRequest request) {

        logger.info("학습모드 질문 요청 - 사용자 콘텐츠 일련번호: {}, 질문: {}",
                userContentSeq, requestDTO.getQuestion());

        try {
            // RequestBox 생성 및 파라미터 설정
            RequestBox box = RequestManager.getBox(request);
            box.put("userContentSeq", userContentSeq);
            box.put("question", requestDTO.getQuestion());

            // 서비스 호출
            DataBox resultDataBox = learningChatService.processLearningModeQuestion(box);

            // DataBox를 ResponseDTO로 변환
            LearningChatResponseDTO responseDTO = convertToLearningChatResponseDTO(resultDataBox);

            // 응답 구성
            Response<LearningChatResponseDTO> response = Response.<LearningChatResponseDTO>builder()
                    .status(Status.OK)
                    .data(responseDTO)
                    .build();

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            logger.error("학습모드 질문 처리 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("학습모드 질문 처리 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "학습모드 질문 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 시험모드 문제 생성
     */
    @PostMapping("/{contentSeq}/exams/questions")
    @Operation(summary = "시험모드 문제 생성", description = "시험모드에서 다음 문제를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문제 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 문제 또는 완료된 상태"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<ExamQuestionResponseDTO>> generateExamQuestion(
            @Parameter(description = "사용자 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            HttpServletRequest request) {

        logger.info("시험모드 문제 생성 요청 - 사용자 콘텐츠 일련번호: {}", userContentSeq);

        try {
            // RequestBox 생성 및 파라미터 설정
            RequestBox box = RequestManager.getBox(request);
            box.put("userContentSeq", userContentSeq);

            // 서비스 호출
            DataBox resultDataBox = learningChatService.generateExamQuestion(box);

            // 응답 구성
            if ("pending".equals(resultDataBox.getString("d_status"))) {
                // 미답변 문제가 있는 경우 - 409 Conflict 상태 코드 사용
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Response.<ExamQuestionResponseDTO>builder()
                                .status(Status.FAIL)
                                .message(resultDataBox.getString("d_message"))
                                .build()
                        );
            } else if ("completed".equals(resultDataBox.getString("d_status"))) {
                // 모든 문제를 다 푼 경우 - 409 Conflict 상태 코드 사용
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Response.<ExamQuestionResponseDTO>builder()
                                .status(Status.FAIL)
                                .message(resultDataBox.getString("d_message"))
                                .build()
                        );
            } else {
                // 정상적인 문제 생성 케이스
                ExamQuestionResponseDTO responseDTO = convertToExamQuestionResponseDTO(resultDataBox);
                return ResponseEntity.ok(
                        Response.<ExamQuestionResponseDTO>builder()
                                .status(Status.OK)
                                .data(responseDTO)
                                .build()
                );
            }

        } catch (BusinessException e) {
            logger.error("시험모드 문제 생성 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("시험모드 문제 생성 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "시험모드 문제 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 시험모드 답변 평가
     */
    @PostMapping("/{contentSeq}/exams/questions/{questionNumber}/answers")
    @Operation(summary = "시험모드 답변 평가", description = "시험모드에서 사용자 답변을 평가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "답변 평가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 답변한 문제"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Response<ExamEvaluationResponseDTO>> evaluateAnswer(
            @Parameter(description = "사용자 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            @Parameter(description = "문제 번호", required = true) @PathVariable int questionNumber,
            @Parameter(description = "답변 정보", required = true) @Valid @RequestBody ExamAnswerRequestDTO requestDTO,
            HttpServletRequest request) {

        logger.info("시험모드 답변 평가 요청 - 사용자 콘텐츠 일련번호: {}, 문제 번호: {}, 답변: {}",
                userContentSeq, questionNumber, requestDTO.getAnswer());

        try {
            // RequestBox 생성 및 파라미터 설정
            RequestBox box = RequestManager.getBox(request);
            box.put("userContentSeq", userContentSeq);
            box.put("questionNumber", questionNumber);
            box.put("answer", requestDTO.getAnswer());

            // "모르겠어요" 플래그 추가
            boolean isDontKnow = "모르겠어요".equals(requestDTO.getAnswer());
            box.put("isDontKnow", isDontKnow);

            // 서비스 호출 (통합된 메서드)
            DataBox resultDataBox = learningChatService.evaluateAnswer(box);

            // 이미 답변한 문제인 경우
            if (resultDataBox.containsKey("d_status") && "already_answered".equals(resultDataBox.getString("d_status"))) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Response.<ExamEvaluationResponseDTO>builder()
                                .status(Status.FAIL)
                                .message(resultDataBox.getString("d_message"))
                                .build());
            }

            // DataBox를 ResponseDTO로 변환
            ExamEvaluationResponseDTO responseDTO = convertToExamEvaluationResponseDTO(resultDataBox);

            // 응답 구성
            return ResponseEntity.ok(
                    Response.<ExamEvaluationResponseDTO>builder()
                            .status(Status.OK)
                            .data(responseDTO)
                            .build());

        } catch (BusinessException e) {
            logger.error("시험모드 답변 평가 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("시험모드 답변 평가 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "시험모드 답변 평가 중 오류가 발생했습니다.");
        }
    }

    /**
     * DataBox를 LearningChatResponseDTO로 변환
     * @param dataBox 변환할 DataBox
     * @return 변환된 LearningChatResponseDTO
     * @throws BusinessException 변환 중 오류 발생 시
     */
    private LearningChatResponseDTO convertToLearningChatResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            logger.error("변환할 DataBox가 null입니다");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "응답 데이터가 존재하지 않습니다.");
        }

        Long chatSeq = dataBox.getLong2("d_chat_seq");
        String answer = dataBox.getString("d_answer");

        // 값 검증
        if (chatSeq == null || chatSeq == 0) {
            logger.warn("chatSeq 값이 유효하지 않습니다: {}", chatSeq);
            // 경고만 하고 진행 (chatSeq가 0이어도 응답은 가능)
        }

        if (StringUtils.isBlank(answer)) {
            logger.error("answer 값이 비어있습니다");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "생성된 답변이 없습니다.");
        }

        LearningChatResponseDTO responseDTO = LearningChatResponseDTO.builder()
                .chatSeq(chatSeq != null ? chatSeq : 0L)
                .answer(answer)
                .build();

        logger.debug("LearningChatResponseDTO 변환 완료: chatSeq={}, 응답길이={}",
                responseDTO.getChatSeq(), responseDTO.getAnswer().length());

        return responseDTO;
    }

    /**
     * DataBox를 ExamQuestionResponseDTO로 변환
     * @param dataBox 변환할 DataBox
     * @return 변환된 ExamQuestionResponseDTO
     * @throws BusinessException 변환 중 오류 발생 시
     */
    private ExamQuestionResponseDTO convertToExamQuestionResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            logger.error("변환할 DataBox가 null입니다");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "응답 데이터가 존재하지 않습니다.");
        }

        String status = dataBox.getString("d_status");
        if (StringUtils.isBlank(status)) {
            logger.error("status 값이 비어있습니다");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "응답 상태 정보가 없습니다.");
        }

        Boolean isLastQuestion = dataBox.getBoolean("d_is_last_question");
        if (isLastQuestion == null) {
            logger.warn("isLastQuestion 값이 null입니다. 기본값 false를 사용합니다.");
            isLastQuestion = false;
        }

        ExamQuestionResponseDTO responseDTO = ExamQuestionResponseDTO.builder()
                .status(status)
                .isLastQuestion(isLastQuestion)
                .build();

        // success와 pending 상태일 때만 추가 데이터 설정
        if ("success".equals(status) || "pending".equals(status)) {
            Integer questionNumber = dataBox.getInt2("d_question_number");
            Integer totalQuestions = dataBox.getInt2("d_total_questions");
            String question = dataBox.getString("d_question");

            if (questionNumber == null || questionNumber <= 0) {
                logger.error("questionNumber 값이 유효하지 않습니다: {}", questionNumber);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "문제 번호 정보가 유효하지 않습니다.");
            }

            if (totalQuestions == null || totalQuestions <= 0) {
                logger.error("totalQuestions 값이 유효하지 않습니다: {}", totalQuestions);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "총 문제수 정보가 유효하지 않습니다.");
            }

            if (StringUtils.isBlank(question)) {
                logger.error("question 값이 비어있습니다");
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "문제 내용이 없습니다.");
            }

            responseDTO.setQuestionNumber(questionNumber);
            responseDTO.setTotalQuestions(totalQuestions);
            responseDTO.setQuestion(question);

            logger.debug("{} 응답 생성: 문제번호={}, 총문제수={}",
                    status, responseDTO.getQuestionNumber(), responseDTO.getTotalQuestions());
        }

        return responseDTO;
    }

    /**
     * DataBox를 ExamEvaluationResponseDTO로 변환
     * @param dataBox 변환할 DataBox
     * @return 변환된 ExamEvaluationResponseDTO
     * @throws BusinessException 변환 중 오류 발생 시
     */
    private ExamEvaluationResponseDTO convertToExamEvaluationResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            logger.error("변환할 DataBox가 null입니다");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "응답 데이터가 존재하지 않습니다.");
        }

        String result = dataBox.getString("d_result");
        String feedback = dataBox.getString("d_feedback");
        Boolean isLastQuestion = dataBox.getBoolean("d_is_last_question");

        // 필수 값 검증
        if (StringUtils.isBlank(result)) {
            logger.error("result 값이 비어있습니다");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "평가 결과가 없습니다.");
        }

        if (StringUtils.isBlank(feedback)) {
            logger.error("feedback 값이 비어있습니다");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "피드백 내용이 없습니다.");
        }

        if (isLastQuestion == null) {
            logger.warn("isLastQuestion 값이 null입니다. 기본값 false를 사용합니다.");
            isLastQuestion = false;
        }

        // 평가 결과 값 검증
        if (!("correct".equals(result) || "incorrect".equals(result) || "irrelevant".equals(result))) {
            logger.error("result 값이 유효하지 않습니다: {}", result);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "유효하지 않은 평가 결과입니다.");
        }

        ExamEvaluationResponseDTO responseDTO = ExamEvaluationResponseDTO.builder()
                .result(result)
                .feedback(feedback)
                .isLastQuestion(isLastQuestion)
                .build();

        // incorrect 또는 irrelevant인 경우에만 모범답안 추가
        if (!"correct".equals(result)) {
            String modelAnswer = dataBox.getString("d_model_answer");
            if (StringUtils.isBlank(modelAnswer)) {
                logger.error("modelAnswer 값이 비어있습니다");
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "모범답안이 없습니다.");
            }
            responseDTO.setModelAnswer(modelAnswer);
        }

        logger.debug("ExamEvaluationResponseDTO 변환 완료: result={}, isLastQuestion={}, 모범답안제공={}",
                responseDTO.getResult(), responseDTO.getIsLastQuestion(), responseDTO.getModelAnswer() != null);

        return responseDTO;
    }
}