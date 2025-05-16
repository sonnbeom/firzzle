package com.firzzle.learning.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.common.logging.dto.UserActionLog;
import com.firzzle.common.logging.service.LoggingService;
import com.firzzle.common.response.Response;
import com.firzzle.common.response.Status;
import com.firzzle.learning.dto.QuizResponseDTO;
import com.firzzle.learning.dto.QuizSubmissionRequestDTO;
import com.firzzle.learning.dto.QuizSubmissionResponseDTO;
import com.firzzle.learning.dto.SnapReviewResponseDTO;
import com.firzzle.learning.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Class Name : QuizController.java
 * @Description : 퀴즈 관련 API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents")
@Tag(name = "퀴즈 API", description = "콘텐츠 학습을 위한 퀴즈 관련 API")
public class QuizController {

    private static final Logger logger = LoggerFactory.getLogger(QuizController.class);

    private final QuizService quizService;

    /**
     * 퀴즈 조회
     * 콘텐츠에 해당하는 출제된 퀴즈 문제를 조회합니다.
     *
     * @param userContentSeq 사용자 콘텐츠 일련번호
     * @param request HTTP 요청 객체
     * @return 퀴즈 문제 정보
     */
    @GetMapping(value = "/{contentSeq}/quiz", produces = "application/json;charset=UTF-8")
    @Operation(summary = "퀴즈 조회", description = "콘텐츠에 해당하는 출제된 퀴즈 문제를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 조회 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:read')")
    public ResponseEntity<Response<QuizResponseDTO>> getQuiz(
            @Parameter(description = "사용자 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            HttpServletRequest request) {

        String uuid = (String) request.getAttribute("uuid");
        logger.info("퀴즈 조회 요청 - 사용자 콘텐츠 일련번호: {}, UUID: {}", userContentSeq, uuid);

        try {
            RequestBox box = RequestManager.getBox(request);
            logger.debug("요청 처리 UUID: {}", box.getString("uuid"));
            box.put("userContentSeq", userContentSeq);

            DataBox dataBox = quizService.selectQuiz(box);
            QuizResponseDTO quizResponseDTO = convertToQuizResponseDTO(dataBox);

            Response<QuizResponseDTO> response = Response.<QuizResponseDTO>builder()
                    .status(Status.OK)
                    .data(quizResponseDTO)
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("퀴즈 조회 중 비즈니스 예외 발생: {}, UUID: {}", e.getMessage(), uuid);
            throw e;
        } catch (Exception e) {
            logger.error("퀴즈 조회 중 예외 발생: {}, UUID: {}", e.getMessage(), uuid, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 퀴즈 답변 제출
     * 퀴즈 답변을 제출하고 결과를 반환합니다.
     *
     * @param userContentSeq 사용자 콘텐츠 일련번호
     * @param quizSubmissionRequestDTO 퀴즈 답변 정보
     * @param request HTTP 요청 객체
     * @return 퀴즈 제출 결과
     */
    @PostMapping(value = "/{contentSeq}/quiz", produces = "application/json;charset=UTF-8")
    @Operation(summary = "퀴즈 답변 제출", description = "퀴즈 답변을 제출하고 결과를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "답변 제출 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('content:write')")
    public ResponseEntity<Response<QuizSubmissionResponseDTO>> submitQuizAnswers(
            @Parameter(description = "사용자 콘텐츠 일련번호", required = true) @PathVariable("contentSeq") Long userContentSeq,
            @Parameter(description = "퀴즈 답변 정보", required = true) @Valid @RequestBody QuizSubmissionRequestDTO quizSubmissionRequestDTO,
            HttpServletRequest request) {

        String uuid = (String) request.getAttribute("uuid");
        logger.info("퀴즈 답변 제출 요청 - 사용자 콘텐츠 일련번호: {}, UUID: {}", userContentSeq, uuid);

        try {
            RequestBox box = RequestManager.getBox(request);
            logger.debug("요청 처리 UUID: {}", box.getString("uuid"));
            box.put("userContentSeq", userContentSeq);

            // 개별 답변 정보 설정
            for (int i = 0; i < quizSubmissionRequestDTO.getAnswers().size(); i++) {
                QuizSubmissionRequestDTO.AnswerDTO answer = quizSubmissionRequestDTO.getAnswers().get(i);
                box.put("questionSeq_" + i, answer.getQuestionSeq());
                box.put("selectedAnswer_" + i, answer.getSelectedAnswer());
            }
            box.put("answerCount", quizSubmissionRequestDTO.getAnswers().size());

            DataBox dataBox = quizService.submitQuizAnswers(box);
            QuizSubmissionResponseDTO responseDTO = convertToQuizSubmissionResponseDTO(dataBox);

            Response<QuizSubmissionResponseDTO> response = Response.<QuizSubmissionResponseDTO>builder()
                    .status(Status.OK)
                    .message("퀴즈 답변이 성공적으로 제출되었습니다.")
                    .data(responseDTO)
                    .build();

            // 퀴즈답변 작성 로깅 => ELK
            LoggingService.log(UserActionLog.userActionLog(uuid, "QUIZ_SUBMIT"));

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            logger.error("퀴즈 답변 제출 중 비즈니스 예외 발생: {}, UUID: {}", e.getMessage(), uuid);
            throw e;
        } catch (Exception e) {
            logger.error("퀴즈 답변 제출 중 예외 발생: {}, UUID: {}", e.getMessage(), uuid, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 답변 제출 중 오류가 발생했습니다.");
        }
    }

    /**
     * DataBox를 QuizResponseDTO로 변환
     *
     * @param dataBox 퀴즈 데이터
     * @return QuizResponseDTO 변환된 DTO
     */
    private QuizResponseDTO convertToQuizResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            return null;
        }

        // 콘텐츠 정보 변환
        Long contentSeq = dataBox.getLong2("d_user_content_seq");

        // 문제 정보 변환
        List<QuizResponseDTO.QuestionDTO> questions = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<DataBox> questionDataBoxes = (List<DataBox>) dataBox.getObject("d_questions");
        if (questionDataBoxes != null) {
            for (DataBox questionDataBox : questionDataBoxes) {
                List<QuizResponseDTO.OptionDTO> options = new ArrayList<>();

                @SuppressWarnings("unchecked")
                List<DataBox> optionDataBoxes = (List<DataBox>) questionDataBox.getObject("d_options");
                if (optionDataBoxes != null) {
                    for (DataBox optionDataBox : optionDataBoxes) {
                        QuizResponseDTO.OptionDTO option = QuizResponseDTO.OptionDTO.builder()
                                .optionSeq(optionDataBox.getLong2("d_option_seq"))
                                .text(optionDataBox.getString("d_option_value"))
                                .build();
                        options.add(option);
                    }
                }

                // 문제 유형 변환
                String questionType = questionDataBox.getString("d_type");
                String mappedType;
                switch(questionType) {
                    case "M4":
                        mappedType = "M4";//multiple_choice4
                        break;
                    case "M5":
                        mappedType = "M5";//multiple_choice5
                        break;
//                case "S":
//                    mappedType = "short_answer";
//                    break;
                    case "OX":
                        mappedType = "OX";
                        break;
                    default:
                        mappedType = questionType.toLowerCase();
                }

                // 사용자 답변 정보 변환
                QuizResponseDTO.UserAnswerDTO userAnswer = null;

                // 사용자 답변 정보가 있는 경우 매핑
                @SuppressWarnings("unchecked")
                List<DataBox> userAnswerDataBoxes = (List<DataBox>) dataBox.getObject("d_user_answers");
                if (userAnswerDataBoxes != null) {
                    for (DataBox answerDataBox : userAnswerDataBoxes) {
                        if (answerDataBox.getLong2("d_question_seq") == questionDataBox.getLong2("d_question_seq")) {
                            String selectedAnswer = answerDataBox.getString("d_selected_answer");
                            boolean isCorrect = "Y".equals(answerDataBox.getString("d_correct_yn"));

                            // 선택된 옵션의 일련번호 찾기
                            Long selectedOptionSeq = null;
                            if (selectedAnswer != null && !selectedAnswer.isEmpty()) {
                                try {
                                    int optionIndex = Integer.parseInt(selectedAnswer) - 1;
                                    if (optionIndex >= 0 && optionIndex < options.size()) {
                                        selectedOptionSeq = options.get(optionIndex).getOptionSeq();
                                    }
                                } catch (NumberFormatException e) {
                                    // 숫자가 아닌 경우 처리
                                    for (QuizResponseDTO.OptionDTO option : options) {
                                        if (selectedAnswer.equals(option.getText())) {
                                            selectedOptionSeq = option.getOptionSeq();
                                            break;
                                        }
                                    }
                                }
                            }

                            // 푼 문제의 경우, 해설 정보 추가
                            String explanation = null;
                            if (isCorrect || !isCorrect) { // 정답이든 오답이든 푼 문제인 경우
                                explanation = questionDataBox.getString("d_explanation");
                            }

                            userAnswer = QuizResponseDTO.UserAnswerDTO.builder()
                                    .selectedOptionSeq(selectedOptionSeq)
                                    .isCorrect(isCorrect)
                                    .explanation(explanation)
                                    .build();
                            break;
                        }
                    }
                }

                // 타임스탬프를 HH:MM:SS 형식으로 변환
                String formattedTimestamp = FormatDate.formatHourMinSec(questionDataBox.getInt2("d_start_time"));

                QuizResponseDTO.QuestionDTO question = QuizResponseDTO.QuestionDTO.builder()
                        .questionSeq(questionDataBox.getLong2("d_question_seq"))
                        .text(questionDataBox.getString("d_question"))
                        .type(mappedType)
                        .timestamp(questionDataBox.getInt2("d_start_time"))
                        .formattedTimestamp(formattedTimestamp)
                        .options(options)
                        .userAnswer(userAnswer)
                        .build();

                questions.add(question);
            }
        }

        // 최근 제출 정보 변환
        QuizResponseDTO.SubmissionDTO submissionDTO = null;
        DataBox lastSubmissionDataBox = (DataBox) dataBox.getObject("d_last_submission");
        if (lastSubmissionDataBox != null) {
            // parseDateTime 메서드를 사용하여 날짜 변환
            LocalDateTime indate = parseDateTime(lastSubmissionDataBox.getString("d_indate"));
            String formattedIndate = indate != null ?
                    indate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";

            // 점수 퍼센트
            float percentage = lastSubmissionDataBox.getInt2("d_percentage");

            submissionDTO = QuizResponseDTO.SubmissionDTO.builder()
                    .submissionSeq(lastSubmissionDataBox.getLong2("d_submission_seq"))
                    .correctAnswers(lastSubmissionDataBox.getInt2("d_correct"))
                    .totalQuestions(lastSubmissionDataBox.getInt2("d_total"))
                    .scorePercentage(percentage)
                    .indate(formattedIndate)
                    .build();
        }

        // 콘텐츠 DTO 생성
        QuizResponseDTO.ContentDTO contentDTO = QuizResponseDTO.ContentDTO.builder()
                .contentSeq(contentSeq)
                .questions(questions)
                .build();

        // 최종 응답 DTO 생성
        return QuizResponseDTO.builder()
                .content(contentDTO)
                .submission(submissionDTO)
                .build();
    }

    /**
     * DataBox를 QuizSubmissionResponseDTO로 변환
     *
     * @param dataBox 퀴즈 제출 결과 데이터
     * @return QuizSubmissionResponseDTO 변환된 DTO
     */
    private QuizSubmissionResponseDTO convertToQuizSubmissionResponseDTO(DataBox dataBox) {
        if (dataBox == null) {
            return null;
        }

        // 문제별 결과 변환
        List<QuizSubmissionResponseDTO.QuestionResultDTO> questionResults = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<DataBox> questionResultDataBoxes = (List<DataBox>) dataBox.getObject("d_question_results");
        if (questionResultDataBoxes != null) {
            for (DataBox questionResultDataBox : questionResultDataBoxes) {
                QuizSubmissionResponseDTO.QuestionResultDTO questionResult =
                        QuizSubmissionResponseDTO.QuestionResultDTO.builder()
                                .questionSeq(questionResultDataBox.getLong2("d_question_seq"))
                                .question(questionResultDataBox.getString("d_question"))
                                .selectedAnswer(questionResultDataBox.getString("d_selected_answer"))
                                .correctAnswer(questionResultDataBox.getString("d_correct_answer"))
                                .isCorrect(questionResultDataBox.getBoolean("d_is_correct"))
                                .explanation(questionResultDataBox.getString("d_explanation"))
                                .build();

                questionResults.add(questionResult);
            }
        }

        // parseDateTime 메서드를 사용하여 날짜 변환
        LocalDateTime indate = parseDateTime(dataBox.getString("d_indate"));
        String formattedIndate = indate != null ?
                indate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";

        // 퍼센티지 값 추출
        float percentage = 0f;
        if (dataBox.getObject("d_percentage") != null) {
            try {
                percentage = Float.parseFloat(dataBox.getString("d_percentage"));
            } catch (NumberFormatException e) {
                percentage = dataBox.getInt2("d_percentage");
            }
        }

        // 제출 정보 DTO 생성
        QuizSubmissionResponseDTO.SubmissionDTO submissionDTO = QuizSubmissionResponseDTO.SubmissionDTO.builder()
                .seq(dataBox.getLong2("d_submission_seq"))
                .contentSeq(dataBox.getLong2("d_user_content_seq"))
                .correctAnswers(dataBox.getInt2("d_correct"))
                .totalQuestions(dataBox.getInt2("d_total"))
                .scorePercentage(percentage)
                .indate(formattedIndate)
                .build();

        // 최종 응답 DTO 생성
        return QuizSubmissionResponseDTO.builder()
                .submission(submissionDTO)
                .questionResults(questionResults)
                .build();
    }

    /**
     * FormatDate를 사용하여 YYYYMMDDHHMMSS -> LocalDateTime 변환
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        try {
            // YYYYMMDDHHMMSS -> "YYYY-MM-DD HH:MM:SS" 형식으로 변환
            String formattedDateTime = FormatDate.getFormatDate(dateTimeStr, "yyyy-MM-dd HH:mm:ss");

            if (formattedDateTime == null || formattedDateTime.isEmpty()) {
                return null;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(formattedDateTime, formatter);
        } catch (Exception e) {
            logger.error("날짜 변환 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
}