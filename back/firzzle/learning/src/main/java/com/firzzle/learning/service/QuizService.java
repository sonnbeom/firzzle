package com.firzzle.learning.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.MyBatisTransactionManager;
import com.firzzle.common.library.RequestBox;
import com.firzzle.learning.dao.QuizDAO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Class Name : QuizService.java
 * @Description : 퀴즈 관련 서비스
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Service
@RequiredArgsConstructor
public class QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);

    private final QuizDAO quizDAO;
    private final MyBatisSupport myBatisSupport;

    /**
     * 퀴즈 조회
     * 콘텐츠에 해당하는 퀴즈 문제를 조회합니다.
     *
     * @param box 요청 정보
     * @return DataBox 퀴즈 정보
     */
    public DataBox selectQuiz(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("퀴즈 조회 요청 - 사용자 콘텐츠 일련번호: {}, UUID: {}",
                box.getLong("userContentSeq"), uuid);

        try {
            // 사용자 콘텐츠 정보를 통해 콘텐츠 정보 조회
            DataBox contentInfo = quizDAO.selectContentByUserContentSeq(box);

            if (contentInfo == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "요청한 콘텐츠를 찾을 수 없습니다.");
            }

            Long contentSeq = contentInfo.getLong2("d_content_seq");
            Long userContentSeq = contentInfo.getLong2("d_user_content_seq");
            box.put("contentSeq", contentSeq); // 실제 contentSeq를 RequestBox에 추가

            logger.debug("콘텐츠 정보 조회 완료 - 콘텐츠 일련번호: {}, UUID: {}", contentSeq, uuid);

            // 퀴즈 문제 조회
            List<DataBox> questionsList = quizDAO.selectQuestionListDataBox(box);
            if (questionsList == null || questionsList.isEmpty()) {
                throw new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "등록된 퀴즈가 없습니다.");
            }

            // 퀴즈 문제 보기 조회
            for (DataBox question : questionsList) {
                RequestBox optionBox = new RequestBox("optionBox");
                optionBox.put("questionSeq", question.getLong2("d_question_seq"));
                optionBox.put("uuid", uuid); // UUID 설정
                List<DataBox> optionsList = quizDAO.selectQuestionOptionsDataBox(optionBox);
                question.put("d_options", optionsList);
            }

            // 사용자 최근 제출 정보 조회
            RequestBox submissionBox = new RequestBox("submissionBox");
            submissionBox.put("contentSeq", contentSeq);
            submissionBox.put("uuid", uuid);
            DataBox lastSubmission = quizDAO.selectLastSubmissionDataBox(submissionBox);

            // 사용자가 제출한 답변 정보 조회
            List<DataBox> userAnswers = new ArrayList<>();
            if (lastSubmission != null) {
                // 최신 제출 번호로 모든 답변 조회
                RequestBox answerBox = new RequestBox("answerBox");
                answerBox.put("submissionSeq", lastSubmission.getLong2("d_submission_seq"));
                answerBox.put("uuid", uuid); // UUID 설정
                userAnswers = quizDAO.selectAnswersBySubmissionSeq(answerBox);

                logger.debug("사용자 답변 조회 결과 - 제출번호: {}, 답변 수: {}, UUID: {}",
                        lastSubmission.getLong2("d_submission_seq"),
                        userAnswers != null ? userAnswers.size() : 0,
                        uuid);

                // 데이터 확인을 위한 로그
                if (userAnswers != null && !userAnswers.isEmpty()) {
                    for (DataBox answer : userAnswers) {
                        logger.debug("조회된 사용자 답변 - 문제번호: {}, 선택답변: {}, 정답여부: {}, UUID: {}",
                                answer.getLong2("d_question_seq"),
                                answer.getString("d_selected_answer"),
                                answer.getString("d_correct_yn"),
                                uuid);
                    }
                }
            }

            // 결과 DataBox 생성
            DataBox result = new DataBox("quizResultBox");
            result.put("d_content_seq", contentSeq);
            result.put("d_user_content_seq", userContentSeq);
            result.put("d_questions", questionsList);
            result.put("d_last_submission", lastSubmission);
            result.put("d_user_answers", userAnswers);

            logger.debug("퀴즈 조회 완료 - ContentSeq: {}, 문제 수: {}, UUID: {}",
                    contentSeq, questionsList.size(), uuid);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("퀴즈 조회 중 오류 발생: {}, UUID: {}", e.getMessage(), uuid, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 퀴즈 답변 제출
     * 사용자가 제출한 퀴즈 답변을 처리하고 결과를 반환합니다.
     *
     * @param box 답변 제출 정보
     * @return DataBox 퀴즈 결과 정보
     */
    public DataBox submitQuizAnswers(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("퀴즈 답변 제출 요청 - 사용자 콘텐츠 일련번호: {}, UUID: {}",
                box.getLong("userContentSeq"), uuid);

        MyBatisTransactionManager transaction = myBatisSupport.getTransactionManager();
        DataBox result = null;

        try {
            // 트랜잭션 시작
            transaction.start();

            // 사용자 콘텐츠 정보를 통해 콘텐츠 정보 조회
            DataBox contentInfo = quizDAO.selectContentByUserContentSeq(box);

            if (contentInfo == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "요청한 콘텐츠를 찾을 수 없습니다.");
            }

            Long contentSeq = contentInfo.getLong2("d_content_seq");
            box.put("contentSeq", contentSeq); // 실제 contentSeq를 RequestBox에 추가

            logger.debug("콘텐츠 정보 조회 완료 - 콘텐츠 일련번호: {}, UUID: {}", contentSeq, uuid);

            // 해당 콘텐츠의 전체 문제 수 조회
            List<DataBox> questionsList = quizDAO.selectQuestionListDataBox(box);
            if (questionsList == null || questionsList.isEmpty()) {
                throw new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "등록된 퀴즈가 없습니다.");
            }

            int totalQuestions = questionsList.size();
            logger.debug("콘텐츠 퀴즈 문제 수: {}, UUID: {}", totalQuestions, uuid);

            // 답변 개수 확인
            int answerCount = box.getInt("answerCount");
            if (answerCount <= 0) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "제출된 답변이 없습니다.");
            }

            // 문제 수와 답변 수 비교
            if (totalQuestions != answerCount) {
                logger.error("퀴즈 답변 제출 오류 - 문제 수({})와 답변 수({})가 일치하지 않습니다. UUID: {}",
                        totalQuestions, answerCount, uuid);
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        String.format("모든 문제에 대한 답변을 제출해야 합니다. (문제: %d개, 답변: %d개)", totalQuestions, answerCount));
            }

            // 퀴즈 제출 정보 등록
            RequestBox submissionBox = new RequestBox("submissionBox");
            submissionBox.put("contentSeq", contentSeq);
            submissionBox.put("uuid", uuid);
            submissionBox.put("total", answerCount);
            submissionBox.put("correct", 0); // 초기값, 채점 후 업데이트
            submissionBox.put("percentage", 0.0f); // 초기값, 채점 후 업데이트
            submissionBox.put("indate", FormatDate.getDate("yyyyMMddHHmmss"));

            int submissionResult = quizDAO.insertQuizSubmission(submissionBox);
            if (submissionResult == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 제출 정보 등록에 실패했습니다.");
            }

            // 답변 채점 및 등록
            int correctCount = 0;
            List<DataBox> questionResults = new ArrayList<>();

            for (int i = 0; i < answerCount; i++) {
                Long questionSeq = box.getLong("questionSeq_" + i);
                String selectedAnswer = box.getString("selectedAnswer_" + i);

                // 문제 정보 조회
                RequestBox questionBox = new RequestBox("questionBox");
                questionBox.put("questionSeq", questionSeq);
                questionBox.put("uuid", uuid); // UUID 설정
                DataBox question = quizDAO.selectQuestionDataBox(questionBox);

                if (question == null) {
                    throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND,
                            "문제를 찾을 수 없습니다. (questionSeq: " + questionSeq + ")");
                }

                // 정답 확인
                String correctAnswer = question.getString("d_correct_answer");
                boolean isCorrect = selectedAnswer.equals(correctAnswer);
                if (isCorrect) {
                    correctCount++;
                }

                // 답변 정보 등록
                RequestBox answerBox = new RequestBox("answerBox");
                answerBox.put("submissionSeq", submissionBox.getLong("submissionSeq"));
                answerBox.put("questionSeq", questionSeq);
                answerBox.put("selectedAnswer", selectedAnswer);
                answerBox.put("correctYn", isCorrect ? "Y" : "N");
                answerBox.put("uuid", uuid); // UUID 설정

                int answerResult = quizDAO.insertQuestionAnswer(answerBox);
                if (answerResult == 0) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "답변 정보 등록에 실패했습니다.");
                }

                // 문제 결과 정보 생성
                DataBox questionResult = new DataBox("questionResultBox");
                questionResult.put("d_question_seq", questionSeq);
                questionResult.put("d_question", question.getString("d_question"));
                questionResult.put("d_selected_answer", selectedAnswer);
                questionResult.put("d_correct_answer", correctAnswer);
                questionResult.put("d_is_correct", isCorrect);
                questionResult.put("d_explanation", question.getString("d_explanation"));
                questionResults.add(questionResult);
            }

            // 제출 정보 업데이트 (정답 수, 정답률)
            float percentage = (float) correctCount / answerCount * 100;
            submissionBox.put("correct", correctCount);
            submissionBox.put("percentage", percentage);

            int updateResult = quizDAO.updateQuizSubmission(submissionBox);
            if (updateResult == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 제출 정보 업데이트에 실패했습니다.");
            }

            // 결과 DataBox 생성
            result = new DataBox("submissionResultBox");
            result.put("d_submission_seq", submissionBox.getLong("submissionSeq"));
            result.put("d_content_seq", contentSeq);
            result.put("d_correct", correctCount);
            result.put("d_total", answerCount);
            result.put("d_percentage", percentage);
            result.put("d_indate", submissionBox.getString("indate"));
            result.put("d_question_results", questionResults);

            // 성공 시 커밋
            transaction.commit();
            logger.debug("퀴즈 답변 제출 완료 - ContentSeq: {}, 정답률: {}%, UUID: {}",
                    contentSeq, percentage, uuid);
            return result;

        } catch (BusinessException e) {
            transaction.rollback();
            throw e;
        } catch (Exception e) {
            transaction.rollback();
            logger.error("퀴즈 답변 제출 중 오류 발생: {}, UUID: {}", e.getMessage(), uuid, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "퀴즈 답변 제출 중 오류가 발생했습니다.");
        } finally {
            // 트랜잭션 종료
            transaction.end();
        }
    }
}