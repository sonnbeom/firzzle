package com.firzzle.learning.dao;

import com.firzzle.common.request.PageRequestDTO;
import com.firzzle.learning.dto.ContentResponseDTO;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Class Name : ContentDAO.java
 * @Description : 콘텐츠 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 4. 30.
 */
@Repository
public class ContentDAO extends MyBatisSupport {

    /** 로거 */
    private static final Logger logger = LoggerFactory.getLogger(ContentDAO.class);

    private static final String NAMESPACE = "ContentMapper";

    /**
     * 콘텐츠 등록
     *
     * @param content - 등록할 콘텐츠 정보
     * @return int - 영향받은 행 수
     */
    public int insertContent(ContentResponseDTO content) {
        logger.debug("콘텐츠 등록 DAO - 콘텐츠명: {}", content.getTitle());
        return sqlSession.insert(NAMESPACE + ".insertContent", content);
    }

    /**
     * 콘텐츠 태그 등록
     *
     * @param contentSeq - 콘텐츠 일련번호
     * @param tags - 등록할 태그 목록
     * @return int - 영향받은 행 수
     */
    public int insertContentTags(Long contentSeq, List<String> tags) {
        logger.debug("콘텐츠 태그 등록 DAO - 콘텐츠일련번호: {}, 태그 수: {}", contentSeq, tags.size());
        Map<String, Object> params = new HashMap<>();
        params.put("contentSeq", contentSeq);
        params.put("tags", tags);
        return sqlSession.insert(NAMESPACE + ".insertContentTags", params);
    }

    /**
     * 콘텐츠 정보 조회
     *
     * @param contentSeq - 조회할 콘텐츠 일련번호
     * @return ContentResponseDTO - 조회된 콘텐츠 정보
     */
    public ContentResponseDTO selectContent(Long contentSeq) {
        logger.debug("콘텐츠 정보 조회 DAO - 콘텐츠일련번호: {}", contentSeq);
        return (ContentResponseDTO) sqlSession.selectOne(NAMESPACE + ".selectContent", contentSeq);
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
     * 콘텐츠 목록 조회
     *
     * @param pageRequestDTO - 페이지 요청 정보
     * @return List<ContentResponseDTO> - 조회된 콘텐츠 목록
     */
    public List<ContentResponseDTO> selectContentList(PageRequestDTO pageRequestDTO) {
        logger.debug("콘텐츠 목록 조회 DAO - 페이지: {}, 사이즈: {}",
                pageRequestDTO.getPageNumber(), pageRequestDTO.getPageSize());
        return sqlSession.selectList(NAMESPACE + ".selectContentList", pageRequestDTO);
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
                box.get("page"), box.get("size"));
        return sqlSession.selectList(NAMESPACE + ".selectContentListDataBox", box);
    }

    /**
     * 콘텐츠 개수 조회
     *
     * @param pageRequestDTO - 페이지 요청 정보 (검색 조건 포함)
     * @return int - 콘텐츠 총 개수
     */
    public int selectContentCount(PageRequestDTO pageRequestDTO) {
        logger.debug("콘텐츠 개수 조회 DAO");
        return (int) sqlSession.selectOne(NAMESPACE + ".selectContentCount", pageRequestDTO);
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
     * 콘텐츠 정보 수정
     *
     * @param content - 수정할 콘텐츠 정보
     * @return int - 영향받은 행 수
     */
    public int updateContent(ContentResponseDTO content) {
        logger.debug("콘텐츠 정보 수정 DAO - 콘텐츠일련번호: {}", content.getContentSeq());
        return sqlSession.update(NAMESPACE + ".updateContent", content);
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
     * @param contentSeq - 삭제할 콘텐츠 일련번호
     * @return int - 영향받은 행 수
     */
    public int deleteContent(Long contentSeq) {
        logger.debug("콘텐츠 삭제 DAO - 콘텐츠일련번호: {}", contentSeq);
        return sqlSession.update(NAMESPACE + ".deleteContent", contentSeq);
    }

    /**
     * 콘텐츠 분석 상태 업데이트
     *
     * @param contentSeq - 업데이트할 콘텐츠 일련번호
     * @param status - 업데이트할 분석 상태
     * @param analysisData - 분석 결과 데이터 (JSON 형식)
     * @return int - 영향받은 행 수
     */
    public int updateAnalysisStatus(Long contentSeq, String status, String analysisData) {
        logger.debug("콘텐츠 분석 상태 업데이트 DAO - 콘텐츠일련번호: {}, 상태: {}", contentSeq, status);
        Map<String, Object> params = new HashMap<>();
        params.put("contentSeq", contentSeq);
        params.put("status", status);
        params.put("analysisData", analysisData);
        return sqlSession.update(NAMESPACE + ".updateAnalysisStatus", params);
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
        return sqlSession.update(NAMESPACE + ".updateAnalysisStatus", box);
    }

    /**
     * YouTube ID로 콘텐츠 존재 여부 확인
     *
     * @param videoId - 확인할 YouTube ID
     * @return int - 해당 YouTube ID를 가진 콘텐츠 개수
     */
    public int selectCountByVideoId(String videoId) {
        logger.debug("YouTube ID로 콘텐츠 존재 여부 확인 DAO - videoId: {}", videoId);
        return (int) sqlSession.selectOne(NAMESPACE + ".selectCountByVideoId", videoId);
    }

    /**
     * 태그별 콘텐츠 목록 조회
     *
     * @param tag - 조회할 태그
     * @param pageRequestDTO - 페이지 요청 정보
     * @return List<ContentResponseDTO> - 조회된 콘텐츠 목록
     */
    public List<ContentResponseDTO> selectContentListByTag(String tag, PageRequestDTO pageRequestDTO) {
        logger.debug("태그별 콘텐츠 목록 조회 DAO - 태그: {}, 페이지: {}, 사이즈: {}",
                tag, pageRequestDTO.getPageNumber(), pageRequestDTO.getPageSize());
        Map<String, Object> params = new HashMap<>();
        params.put("tag", tag);
        params.put("pageRequestDTO", pageRequestDTO);
        return sqlSession.selectList(NAMESPACE + ".selectContentListByTag", params);
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
        return sqlSession.selectList(NAMESPACE + ".selectContentListByTagDataBox", box);
    }
}