package com.firzzle.learning.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Class Name : ExpertRecommendationDAO.java
 * @Description : 전문가 추천 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 5. 3.
 */
@Repository
public class ExpertRecommendationDAO extends MyBatisSupport {

    private static final Logger logger = LoggerFactory.getLogger(ExpertRecommendationDAO.class);

    private static final String NAMESPACE = "ExpertRecommendationMapper";

    /**
     * 추천 전문가 목록 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox (contentSeq 필수)
     * @return List<DataBox> - 추천 전문가 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectRecommendedExperts(RequestBox box) {
        logger.debug("추천 전문가 목록 조회 DAO - 콘텐츠 일련번호: {}", box.getLong("contentSeq"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectRecommendedExperts", box);
    }

    /**
     * 전문가의 전문 분야 목록 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox (expertSeq 필수)
     * @return List<DataBox> - 전문 분야 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectExpertExpertises(RequestBox box) {
        logger.debug("전문가 전문 분야 목록 조회 DAO - 전문가 일련번호: {}", box.getLong("expertSeq"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectExpertExpertises", box);
    }
}