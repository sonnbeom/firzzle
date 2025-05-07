package com.firzzle.learning.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Class Name : QuizDAO.java
 * @Description : 퀴즈 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Repository
public class QuizDAO extends MyBatisSupport {

    private static final Logger logger = LoggerFactory.getLogger(QuizDAO.class);

    private static final String NAMESPACE = "QuizMapper";

    /**
     * 사용자 콘텐츠 정보를 통해 콘텐츠 정보 조회
     *
     * @param box - 요청 정보
     * @return DataBox - 조회된 콘텐츠 정보
     */
    public DataBox selectContentByUserContentSeq(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("사용자 콘텐츠 정보로 콘텐츠 정보 조회 DAO - 사용자콘텐츠일련번호: {}, UUID: {}",
                box.getLong("userContentSeq"), uuid);
        return sqlSession.selectDataBox(NAMESPACE + ".selectContentByUserContentSeq", box);
    }

    /**
     * 콘텐츠 존재 여부 확인
     *
     * @param box - 확인할 콘텐츠 정보
     * @return int - 콘텐츠 개수
     */
    public int selectContentCount(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("콘텐츠 존재 여부 확인 DAO - 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("contentSeq"), uuid);
        return (int) sqlSession.selectOne(NAMESPACE + ".selectContentCount", box);
    }

    /**
     * 퀴즈 문제 목록 조회
     *
     * @param box - 요청 정보
     * @return List<DataBox> - 조회된 문제 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectQuestionListDataBox(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("퀴즈 문제 목록 조회 DAO - 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("contentSeq"), uuid);
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectQuestionListDataBox", box);
    }

    /**
     * 퀴즈 문제 보기 목록 조회
     *
     * @param box - 요청 정보
     * @return List<DataBox> - 조회된 보기 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectQuestionOptionsDataBox(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("퀴즈 문제 보기 목록 조회 DAO - 문제일련번호: {}, UUID: {}",
                box.getLong("questionSeq"), uuid);
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectQuestionOptionsDataBox", box);
    }

    /**
     * 퀴즈 문제 정보 조회
     *
     * @param box - 요청 정보
     * @return DataBox - 조회된 문제 정보
     */
    public DataBox selectQuestionDataBox(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("퀴즈 문제 정보 조회 DAO - 문제일련번호: {}, UUID: {}",
                box.getLong("questionSeq"), uuid);
        return sqlSession.selectDataBox(NAMESPACE + ".selectQuestionDataBox", box);
    }

    /**
     * 사용자 최근 제출 정보 조회
     *
     * @param box - 요청 정보
     * @return DataBox - 조회된 제출 정보
     */
    public DataBox selectLastSubmissionDataBox(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("사용자 최근 제출 정보 조회 DAO - 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("contentSeq"), uuid);
        return sqlSession.selectDataBox(NAMESPACE + ".selectLastSubmissionDataBox", box);
    }

    /**
     * 특정 제출의 답변 정보 조회
     *
     * @param box - 요청 정보
     * @return List<DataBox> - 조회된 답변 정보 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectAnswersBySubmissionSeq(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("특정 제출의 답변 정보 조회 DAO - 제출번호: {}, UUID: {}",
                box.getLong("submissionSeq"), uuid);
        List<DataBox> answers = sqlSession.selectDataBoxList(NAMESPACE + ".selectAnswersBySubmissionSeq", box);

        // 응답 데이터 로깅
        if (answers != null && !answers.isEmpty()) {
            logger.debug("조회된 답변 수: {}, UUID: {}", answers.size(), uuid);
            for (DataBox answer : answers) {
                logger.debug("조회된 사용자 답변 - 문제번호: {}, 선택답변: {}, 정답여부: {}, UUID: {}",
                        answer.getLong2("d_question_seq"),
                        answer.getString("d_selected_answer"),
                        answer.getString("d_correct_yn"),
                        uuid);
            }
        }

        return answers;
    }

    /**
     * 퀴즈 제출 정보 등록
     *
     * @param box - 등록할 제출 정보
     * @return int - 영향받은 행 수
     */
    public int insertQuizSubmission(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("퀴즈 제출 정보 등록 DAO - 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("contentSeq"), uuid);
        return sqlSession.insert(NAMESPACE + ".insertQuizSubmission", box);
    }

    /**
     * 퀴즈 제출 정보 업데이트
     *
     * @param box - 업데이트할 제출 정보
     * @return int - 영향받은 행 수
     */
    public int updateQuizSubmission(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("퀴즈 제출 정보 업데이트 DAO - 제출일련번호: {}, UUID: {}",
                box.getLong("submissionSeq"), uuid);
        return sqlSession.update(NAMESPACE + ".updateQuizSubmission", box);
    }

    /**
     * 문제 답변 정보 등록
     *
     * @param box - 등록할 답변 정보
     * @return int - 영향받은 행 수
     */
    public int insertQuestionAnswer(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("문제 답변 정보 등록 DAO - 제출일련번호: {}, 문제일련번호: {}, UUID: {}",
                box.getLong("submissionSeq"), box.getLong("questionSeq"), uuid);
        return sqlSession.insert(NAMESPACE + ".insertQuestionAnswer", box);
    }
}