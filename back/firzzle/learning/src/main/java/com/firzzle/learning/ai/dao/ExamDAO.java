package com.firzzle.learning.ai.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Class Name : ExamDAO.java
 * @Description : 시험 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 5. 9.
 */
@Repository
public class ExamDAO extends MyBatisSupport {

    private static final Logger logger = LoggerFactory.getLogger(ExamDAO.class);
    private static final String NAMESPACE = "ExamMapper";

    /**
     * 시험 내역 조회 (커서 기반 페이징)
     */
    public List<DataBox> selectExamHistoryWithCursor(RequestBox box) {
        logger.debug("시험 내역 조회 DAO (커서 기반) - 사용자: {}, 콘텐츠: {}, 커서: {}, 정렬: {} {}, 크기: {}",
                box.getString("uuid"), box.getLong("contentSeq"), box.getLong("cursor"),
                box.getString("orderBy"), box.getString("direction"), box.getInt("size"));

        return sqlSession.selectDataBoxList(NAMESPACE + ".selectExamHistoryWithCursor", box);
    }

    /**
     * 시험 문제 저장
     */
    public Long insertExam(RequestBox box) {
        logger.debug("시험 문제 저장 DAO - 사용자: {}, 콘텐츠: {}", box.get("uuid"), box.get("contentSeq"));
        sqlSession.insert(NAMESPACE + ".insertExam", box);
        return box.getLong("examSeq"); // examSeq 반환
    }

    /**
     * 사용자별 콘텐츠에 대한 시험 문제 개수 조회
     */
    public int selectExamCount(String uuid, Long contentSeq) {
        RequestBox box = new RequestBox("examCountBox");
        box.put("uuid", uuid);
        box.put("contentSeq", contentSeq);
        logger.debug("시험 문제 개수 조회 DAO - 사용자: {}, 콘텐츠: {}", uuid, contentSeq);
        return (int) sqlSession.selectOne(NAMESPACE + ".selectExamCount", box);
    }

    /**
     * 시험 문제 정보 조회
     */
    public DataBox selectExam(String uuid, Long contentSeq, int questionNumber) {
        RequestBox box = new RequestBox("examSelectBox");
        box.put("uuid", uuid);
        box.put("contentSeq", contentSeq);
        box.put("questionNumber", questionNumber);
        logger.debug("시험 문제 정보 조회 DAO - 사용자: {}, 콘텐츠: {}, 문제번호: {}",
                uuid, contentSeq, questionNumber);
        return sqlSession.selectDataBox(NAMESPACE + ".selectExam", box);
    }

    /**
     * 이전 시험 질문/응답 이력 조회
     */
    public List<DataBox> selectRecentExams(RequestBox box) {
        logger.debug("최근 시험 응답 조회 - 사용자: {}, 콘텐츠: {}, 개수: {}",
                box.getString("uuid"), box.getLong("contentSeq"), box.getInt("limit"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectRecentExams", box);
    }

    /**
     * 시험 정보 업데이트
     */
    public int updateExam(RequestBox box) {
        logger.debug("시험 정보 업데이트 DAO - 시험번호: {}", box.get("examSeq"));
        return sqlSession.update(NAMESPACE + ".updateExam", box);
    }

    /**
     * 특정 문제에 이미 답변을 제출했는지 확인
     */
    public boolean hasAnsweredQuestion(String uuid, Long contentSeq, String questionContent) {
        RequestBox box = new RequestBox("hasAnsweredBox");
        box.put("uuid", uuid);
        box.put("contentSeq", contentSeq);
        box.put("questionContent", questionContent);

        Integer count = (Integer) sqlSession.selectOne(NAMESPACE + ".hasAnsweredQuestion", box);
        logger.debug("답변 제출 여부 확인 DAO - 사용자: {}, 콘텐츠: {}, 결과: {}", uuid, contentSeq, count > 0);
        return count > 0;
    }

    /**
     * 답변하지 않은 문제가 있는지 확인
     */
    public boolean hasUnansweredQuestions(String uuid, Long contentSeq) {
        RequestBox box = new RequestBox("unansweredBox");
        box.put("uuid", uuid);
        box.put("contentSeq", contentSeq);

        Integer count = (Integer) sqlSession.selectOne(NAMESPACE + ".hasUnansweredQuestions", box);
        logger.debug("미답변 문제 확인 DAO - 사용자: {}, 콘텐츠: {}, 결과: {}", uuid, contentSeq, count > 0);
        return count > 0;
    }

    /**
     * 가장 최근 문제 번호 조회
     */
    public int getLatestQuestionNumber(String uuid, Long contentSeq) {
        RequestBox box = new RequestBox("latestQuestionBox");
        box.put("uuid", uuid);
        box.put("contentSeq", contentSeq);

        Integer count = (Integer) sqlSession.selectOne(NAMESPACE + ".getLatestQuestionNumber", box);
        logger.debug("최근 문제 번호 조회 DAO - 사용자: {}, 콘텐츠: {}, 결과: {}", uuid, contentSeq, count);
        return count;
    }

    /**
     * 특정 문제 번호에 대한 상태 확인
     */
    public DataBox checkQuestionStatus(String uuid, Long contentSeq, int questionNumber) {
        RequestBox box = new RequestBox("checkStatusBox");
        box.put("uuid", uuid);
        box.put("contentSeq", contentSeq);
        box.put("questionNumber", questionNumber);

        logger.debug("문제 상태 확인 DAO - 사용자: {}, 콘텐츠: {}, 문제번호: {}", uuid, contentSeq, questionNumber);
        return sqlSession.selectDataBox(NAMESPACE + ".checkQuestionStatus", box);
    }
}