package com.firzzle.learning.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Class Name : ContentSummaryDAO.java
 * @Description : 콘텐츠 요약 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 5. 3.
 */
@Repository
public class ContentSummaryDAO extends MyBatisSupport {

    private static final Logger logger = LoggerFactory.getLogger(ContentSummaryDAO.class);

    private static final String NAMESPACE = "ContentSummaryMapper";

    /**
     * 콘텐츠 요약 정보 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox (contentSeq, level 필수)
     * @return DataBox - 콘텐츠 요약 정보
     */
    public DataBox selectContentSummary(RequestBox box) {
        logger.debug("콘텐츠 요약 정보 조회 DAO - 콘텐츠 일련번호: {}, 난이도: {}",
                box.getLong("contentSeq"), box.getString("level"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectContentSummary", box);
    }

    /**
     * 요약 관련 섹션 목록 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox (summarySeq 필수)
     * @return List<DataBox> - 섹션 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectSummarySections(RequestBox box) {
        logger.debug("요약 관련 섹션 목록 조회 DAO - 요약 일련번호: {}", box.getLong("summarySeq"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectSummarySections", box);
    }
}