package com.firzzle.learning.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Class Name : ContentRecommendationDAO.java
 * @Description : 콘텐츠 추천 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 5. 3.
 */
@Repository
public class ContentRecommendationDAO extends MyBatisSupport {

    private static final Logger logger = LoggerFactory.getLogger(ContentRecommendationDAO.class);

    private static final String NAMESPACE = "ContentRecommendationMapper";

    /**
     * 콘텐츠 추천 목록 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox (contentSeq 필수)
     * @return List<DataBox> - 추천 콘텐츠 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectRecommendedContents(RequestBox box) {
        logger.debug("콘텐츠 추천 목록 조회 DAO - 콘텐츠 일련번호: {}", box.getLong("contentSeq"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectRecommendedContents", box);
    }

    /**
     * 추천 콘텐츠 개수 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return int - 추천 콘텐츠 전체 개수
     */
    public int selectRecommendedContentsCount(RequestBox box) {
        return (int) sqlSession.selectOne(NAMESPACE + ".selectRecommendedContentsCount", box);
    }
}