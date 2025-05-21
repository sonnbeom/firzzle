package com.firzzle.learning.expert.service;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Class Name : ContentTagDAO.java
 * @Description : 콘텐츠 태그 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Slf4j
@Repository
public class ContentTagDAO extends MyBatisSupport {

    private static final String NAMESPACE = "ContentTagMapper";

    /**
     * 콘텐츠 태그 목록 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox (contentSeq 필수)
     * @return List<DataBox> - 태그 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectContentTags(RequestBox box) {
        log.debug("콘텐츠 태그 목록 조회 - 콘텐츠 일련번호: {}", box.getLong("contentSeq"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectContentTags", box);
    }

    /**
     * 콘텐츠 태그 개수 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox (contentSeq 필수)
     * @return int - 태그 개수
     */
    public int selectContentTagsCount(RequestBox box) {
        return (int) sqlSession.selectOne(NAMESPACE + ".selectContentTagsCount", box);
    }
}