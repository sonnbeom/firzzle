package com.firzzle.learning.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Class Name : ContentDAO.java
 * @Description : 콘텐츠 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 4. 30.
 */
@Repository
public class ContentDAO extends MyBatisSupport {
    
    private static final Logger logger = LoggerFactory.getLogger(ContentDAO.class);

    private static final String NAMESPACE = "ContentMapper";

    /**
     * 콘텐츠 등록
     *
     * @param box - 등록할 콘텐츠 정보
     * @return int - 영향받은 행 수
     */
    public int insertContent(RequestBox box) {
        logger.debug("콘텐츠 등록 DAO - 콘텐츠명: {}", box.getString("title"));
        return sqlSession.insert(NAMESPACE + ".insertContent", box);
    }

    /**
     * 사용자 콘텐츠 매핑 등록
     *
     * @param box - 등록할 사용자 콘텐츠 매핑 정보
     * @return int - 영향받은 행 수
     */
    public int insertUserContent(RequestBox box) {
        logger.debug("사용자 콘텐츠 매핑 등록 DAO - 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("contentSeq"), box.getString("uuid"));
        return sqlSession.insert(NAMESPACE + ".insertUserContent", box);
    }

    /**
     * 콘텐츠 태그 등록
     *
     * @param box - 등록할 태그 정보
     * @return int - 영향받은 행 수
     */
    public int insertContentTags(RequestBox box) {
        logger.debug("콘텐츠 태그 등록 DAO - 콘텐츠일련번호: {}", box.getLong("contentSeq"));
        return sqlSession.insert(NAMESPACE + ".insertContentTags", box);
    }

    /**
     * 콘텐츠 태그 삭제
     *
     * @param box - 삭제할 태그 정보
     * @return int - 영향받은 행 수
     */
    public int deleteContentTags(RequestBox box) {
        logger.debug("콘텐츠 태그 삭제 DAO - 콘텐츠일련번호: {}", box.getLong("contentSeq"));
        return sqlSession.delete(NAMESPACE + ".deleteContentTags", box);
    }

    /**
     * 콘텐츠 정보 조회 (DataBox 반환)
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return DataBox - 조회된 콘텐츠 정보
     */
    public DataBox selectContentDataBox(RequestBox box) {
        logger.debug("콘텐츠 정보 DataBox 조회 DAO - 콘텐츠일련번호: {}", box.get("contentSeq"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectContentDataBox", box);
    }

    /**
     * 사용자 콘텐츠 정보를 통해 콘텐츠 정보 조회 (DataBox 반환)
     *
     * @param box - 요청 정보가 담긴 RequestBox (userContentSeq, uuid 포함)
     * @return DataBox - 조회된 콘텐츠 정보
     */
    public DataBox selectContentByUserContentSeq(RequestBox box) {
        logger.debug("사용자 콘텐츠 정보로 콘텐츠 정보 DataBox 조회 DAO - 사용자콘텐츠일련번호: {}, UUID: {}",
                box.get("userContentSeq"), box.getString("uuid"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectContentByUserContentSeq", box);
    }

    /**
     * YouTube ID로 콘텐츠 정보 조회
     *
     * @param box - 확인할 YouTube ID 정보가 담긴 RequestBox
     * @return DataBox - 조회된 콘텐츠 정보
     */
    public DataBox selectContentByVideoId(RequestBox box) {
        logger.debug("YouTube ID로 콘텐츠 정보 조회 DAO - videoId: {}", box.get("videoId"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectContentByVideoId", box);
    }

    /**
     * 사용자-콘텐츠 매핑 개수 조회
     *
     * @param box - 확인할 사용자-콘텐츠 정보가 담긴 RequestBox
     * @return int - 조회된 매핑 개수
     */
    public int selectUserContentCount(RequestBox box) {
        logger.debug("사용자-콘텐츠 매핑 개수 조회 DAO - contentSeq: {}, UUID: {}",
                box.getLong("contentSeq"), box.getString("uuid"));
        return (int) sqlSession.selectOne(NAMESPACE + ".selectUserContentCount", box);
    }

    /**
     * 콘텐츠 목록 조회 (DataBox 반환)
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return List<DataBox> - 조회된 콘텐츠 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectContentListDataBox(RequestBox box) {
        logger.debug("콘텐츠 목록 DataBox 조회 DAO - 페이지: {}, 사이즈: {}",
                box.get("p_pageno"), box.get("p_pagesize"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectContentListDataBox", box);
    }

    /**
     * 콘텐츠 개수 조회 (RequestBox 사용)
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return int - 콘텐츠 총 개수
     */
    public int selectContentCount(RequestBox box) {
        logger.debug("콘텐츠 개수 조회 DAO (RequestBox)");
        return (int) sqlSession.selectOne(NAMESPACE + ".selectContentCount", box);
    }

    /**
     * 태그별 콘텐츠 개수 조회 (RequestBox 사용)
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return int - 태그별 콘텐츠 총 개수
     */
    public int selectContentCountByTag(RequestBox box) {
        logger.debug("태그별 콘텐츠 개수 조회 DAO (RequestBox) - 태그: {}", box.get("tag"));
        return (int) sqlSession.selectOne(NAMESPACE + ".selectContentCountByTag", box);
    }

    /**
     * 콘텐츠 정보 수정 (RequestBox 사용)
     *
     * @param box - 수정할 콘텐츠 정보가 담긴 RequestBox
     * @return int - 영향받은 행 수
     */
    public int updateContent(RequestBox box) {
        logger.debug("콘텐츠 정보 수정 DAO - 콘텐츠일련번호: {}", box.get("contentSeq"));
        return sqlSession.update(NAMESPACE + ".updateContentBox", box);
    }

    /**
     * 콘텐츠 삭제
     *
     * @param box - 삭제할 콘텐츠 정보가 담긴 RequestBox
     * @return int - 영향받은 행 수
     */
    public int deleteContent(RequestBox box) {
        logger.debug("콘텐츠 삭제 DAO - 콘텐츠일련번호: {}", box.get("contentSeq"));
        return sqlSession.update(NAMESPACE + ".deleteContent", box);
    }

    /**
     * 콘텐츠 분석 상태 업데이트 (RequestBox 사용)
     *
     * @param box - 업데이트 정보가 담긴 RequestBox
     * @return int - 영향받은 행 수
     */
    public int updateAnalysisStatus(RequestBox box) {
        logger.debug("콘텐츠 분석 상태 업데이트 DAO - 콘텐츠일련번호: {}, 상태: {}",
                box.get("contentSeq"), box.get("status"));
        return sqlSession.update(NAMESPACE + ".updateAnalysisStatusBox", box);
    }

    /**
     * YouTube ID로 콘텐츠 존재 여부 확인
     *
     * @param box - 확인할 YouTube ID 정보가 담긴 RequestBox
     * @return int - 해당 YouTube ID를 가진 콘텐츠 개수
     */
    public int selectCountByVideoId(RequestBox box) {
        logger.debug("YouTube ID로 콘텐츠 존재 여부 확인 DAO - videoId: {}", box.get("videoId"));
        return (int) sqlSession.selectOne(NAMESPACE + ".selectCountByVideoId", box);
    }

    /**
     * 태그별 콘텐츠 목록 조회 (DataBox 반환)
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return List<DataBox> - 조회된 콘텐츠 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectContentListByTagDataBox(RequestBox box) {
        logger.debug("태그별 콘텐츠 목록 DataBox 조회 DAO - 태그: {}", box.get("tag"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectContentListByTagDataBox", box);
    }
}