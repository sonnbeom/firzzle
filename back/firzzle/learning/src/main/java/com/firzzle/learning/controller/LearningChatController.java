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
     */
    private LearningChatResponseDTO convertToLearningChatResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            logger.error("변환할 DataBox가 null입니다");
            return new LearningChatResponseDTO();
        }

        try {
            Long chatSeq = dataBox.getLong2("d_chat_seq");
            String answer = dataBox.getString("d_answer");

            // 값 검증 및 로깅
            if (chatSeq == null || chatSeq == 0) {
                logger.warn("chatSeq가 null 또는 0입니다: {}", chatSeq);
            }

            if (StringUtils.isBlank(answer)) {
                logger.warn("answer가 비어있습니다");
                answer = "죄송합니다. 답변을 생성하는 중 오류가 발생했습니다.";
            }

            LearningChatResponseDTO responseDTO = LearningChatResponseDTO.builder()
                    .chatSeq(chatSeq)
                    .answer(answer)
                    .build();

            logger.debug("LearningChatResponseDTO 변환 완료: chatSeq={}, 응답길이={}",
                    responseDTO.getChatSeq(), responseDTO.getAnswer().length());

            return responseDTO;
        } catch (Exception e) {
            logger.error("LearningChatResponseDTO 변환 중 오류 발생: {}", e.getMessage(), e);
            return LearningChatResponseDTO.builder()
                    .chatSeq(0L)
                    .answer("응답 데이터 처리 중 오류가 발생했습니다.")
                    .build();
        }
    }

    /**
     * DataBox를 ExamQuestionResponseDTO로 변환
     */
    private ExamQuestionResponseDTO convertToExamQuestionResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            logger.error("변환할 DataBox가 null입니다");
            return new ExamQuestionResponseDTO();
        }

        try {
            String status = dataBox.getString("d_status");
            Boolean isLastQuestion = dataBox.getBoolean("d_is_last_question");

            if (isLastQuestion == null) {
                logger.error("DataBox에 d_is_last_question 값이 없습니다. 기본값 false 설정");
                isLastQuestion = false;
            }

            logger.debug("DataBox status: {}", status);

            ExamQuestionResponseDTO responseDTO = ExamQuestionResponseDTO.builder()
                    .status(status)
                    .isLastQuestion(isLastQuestion)
                    .build();

            if ("success".equals(status)) {
                responseDTO.setQuestionNumber(dataBox.getInt2("d_question_number"));
                responseDTO.setTotalQuestions(dataBox.getInt2("d_total_questions"));
                responseDTO.setQuestion(dataBox.getString("d_question"));
                logger.debug("success 응답 생성: 문제번호={}, 총문제수={}, 문제={}",
                        responseDTO.getQuestionNumber(), responseDTO.getTotalQuestions(), responseDTO.getQuestion());
            } else if ("pending".equals(status)) {
                responseDTO.setQuestionNumber(dataBox.getInt2("d_question_number"));
                responseDTO.setTotalQuestions(dataBox.getInt2("d_total_questions"));
                responseDTO.setQuestion(dataBox.getString("d_question"));
                logger.debug("pending 응답 생성: 문제번호={}, 총문제수={}, 문제={}",
                        responseDTO.getQuestionNumber(), responseDTO.getTotalQuestions(), responseDTO.getQuestion());
            }
            // completed 상태는 여기서는 빈 DTO만 반환 (메시지는 Response에 담김)

            return responseDTO;
        } catch (Exception e) {
            logger.error("ExamQuestionResponseDTO 변환 중 오류 발생: {}", e.getMessage(), e);
            return new ExamQuestionResponseDTO();
        }
    }

    /**
     * DataBox를 ExamEvaluationResponseDTO로 변환
     */
    private ExamEvaluationResponseDTO convertToExamEvaluationResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            logger.error("변환할 DataBox가 null입니다");
            return new ExamEvaluationResponseDTO();
        }

        try {
            String result = dataBox.getString("d_result");
            String feedback = dataBox.getString("d_feedback");
            Boolean isLastQuestion = dataBox.getBoolean("d_is_last_question");

            // 값 검증 및 기본값 설정
            if (StringUtils.isBlank(result)) {
                logger.error("DataBox에 d_result 값이 없습니다. 기본값 'incorrect' 설정");
                result = "incorrect";
            }

            if (StringUtils.isBlank(feedback)) {
                logger.error("DataBox에 d_feedback 값이 없습니다. 기본값 설정");
                feedback = "incorrect".equals(result) ? "조금 아쉬워요. 다시 생각해보세요!" :
                        "irrelevant".equals(result) ? "질문에 벗어난 내용이에요. 다시 입력하세요." :
                                "정답입니다! 잘 이해하고 있어요.";
            }

            if (isLastQuestion == null) {
                logger.error("DataBox에 d_is_last_question 값이 없습니다. 기본값 false 설정");
                isLastQuestion = false;
            }

            ExamEvaluationResponseDTO responseDTO = ExamEvaluationResponseDTO.builder()
                    .result(result)
                    .feedback(feedback)
                    .isLastQuestion(isLastQuestion)
                    .build();

            if (!"correct".equals(result) && dataBox.getString("d_model_answer") != null) {
                responseDTO.setModelAnswer(dataBox.getString("d_model_answer"));
            }

            logger.debug("ExamEvaluationResponseDTO 변환 완료: result={}, feedback={}, isLastQuestion={}, modelAnswer={}",
                    responseDTO.getResult(), responseDTO.getFeedback(), responseDTO.getIsLastQuestion(),
                    responseDTO.getModelAnswer());

            return responseDTO;
        } catch (Exception e) {
            logger.error("ExamEvaluationResponseDTO 변환 중 오류 발생: {}", e.getMessage(), e);
            return ExamEvaluationResponseDTO.builder()
                    .result("incorrect")
                    .feedback("평가 결과 변환 중 오류가 발생했습니다.")
                    .isLastQuestion(false)
                    .build();
        }
    }
}