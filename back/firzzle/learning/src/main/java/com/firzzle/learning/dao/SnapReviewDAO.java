package com.firzzle.learning.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Class Name : SnapReviewDAO.java
 * @Description : 스냅리뷰 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Repository
public class SnapReviewDAO extends MyBatisSupport {

    private static final Logger logger = LoggerFactory.getLogger(SnapReviewDAO.class);

    private static final String NAMESPACE = "SnapReviewMapper";

    /**
     * 콘텐츠 존재 여부 확인
     *
     * @param box - 확인할 콘텐츠 정보
     * @return int - 콘텐츠 개수
     */
    public int selectContentCount(RequestBox box) {
        logger.debug("콘텐츠 존재 여부 확인 DAO - 콘텐츠일련번호: {}, UUID: {}", box.getLong("contentSeq"), box.getString("uuid"));
        return (int) sqlSession.selectOne(NAMESPACE + ".selectContentCount", box);
    }

    /**
     * 콘텐츠 정보 조회
     *
     * @param box - 요청 정보 (contentSeq, uuid)
     * @return DataBox - 조회된 콘텐츠 정보
     */
    public DataBox selectContentDataBox(RequestBox box) {
        logger.debug("콘텐츠 정보 조회 DAO - 콘텐츠일련번호: {}, UUID: {}", box.getLong("contentSeq"), box.getString("uuid"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectContentDataBox", box);
    }

    /**
     * 사용자 콘텐츠 정보를 통해 콘텐츠 정보 조회
     *
     * @param box - 요청 정보 (userContentSeq, uuid)
     * @return DataBox - 조회된 콘텐츠 정보
     */
    public DataBox selectContentByUserContentSeq(RequestBox box) {
        logger.debug("사용자 콘텐츠 정보로 콘텐츠 정보 조회 DAO - 사용자콘텐츠일련번호: {}, UUID: {}",
                box.getLong("userContentSeq"), box.getString("uuid"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectContentByUserContentSeq", box);
    }

    /**
     * 콘텐츠별 프레임 목록 조회
     *
     * @param box - 요청 정보 (contentSeq, uuid)
     * @return List<DataBox> - 조회된 프레임 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectFramesByContent(RequestBox box) {
        logger.debug("콘텐츠별 프레임 목록 조회 DAO - 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("contentSeq"), box.getString("uuid"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectFramesByContent", box);
    }

    /**
     * 콘텐츠별 프레임 목록 조회
     *
     * @param box - 요청 정보 (contentSeq, uuid)
     * @return List<DataBox> - 조회된 프레임 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectFramesByUserSeqContent(RequestBox box) {
        logger.debug("콘텐츠별 프레임 목록 조회 DAO - 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("contentSeq"), box.getString("uuid"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectFramesByContent", box);
    }

    /**
     * 프레임 정보 조회
     *
     * @param box - 요청 정보 (frameSeq, contentSeq, uuid)
     * @return DataBox - 조회된 프레임 정보
     */
    public DataBox selectFrameBySeq(RequestBox box) {
        logger.debug("프레임 정보 조회 DAO - 프레임일련번호: {}, 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("frameSeq"), box.getLong("contentSeq"), box.getString("uuid"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectFrameBySeq", box);
    }

    /**
     * 공유 코드 존재 여부 확인
     *
     * @param box - 확인할 공유 코드 정보
     * @return DataBox - 존재하는 경우 공유 코드 정보 반환, 없는 경우 null
     */
    public DataBox checkShareCodeExists(RequestBox box) {
        logger.debug("공유 코드 존재 여부 확인 DAO - 공유코드: {}", box.getString("shareCode"));
        return sqlSession.selectDataBox(NAMESPACE + ".checkShareCodeExists", box);
    }

    /**
     * 공유 코드 존재 여부 확인 by Content Seq, uuid
     *
     * @param box - 확인할 공유 코드 정보
     * @return DataBox - 존재하는 경우 공유 코드 정보 반환, 없는 경우 null
     */
    public DataBox checkShareCodeExistsByContent(RequestBox box) {
        logger.debug("공유 코드 존재 여부 확인 DAO - 공유코드: {}", box.getString("shareCode"));
        return sqlSession.selectDataBox(NAMESPACE + ".checkShareCodeExistsByContent", box);
    }

    /**
     * 프레임 정보 수정
     *
     * @param box - 수정할 프레임 정보
     * @return int - 영향받은 행 수
     */
    public int updateFrame(RequestBox box) {
        logger.debug("프레임 정보 수정 DAO - 프레임일련번호: {}", box.getLong("frameSeq"));
        return sqlSession.update(NAMESPACE + ".updateFrame", box);
    }

    /**
     * 콘텐츠 및 프레임 목록 조회
     *
     * @param box - 요청 정보 (페이징, 검색 조건 등)
     * @return List<DataBox> - 조회된 콘텐츠 및 프레임 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectContentListWithFrames(RequestBox box) {
        logger.debug("콘텐츠 및 프레임 목록 조회 DAO - UUID: {}", box.getString("uuid"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectContentListWithFrames", box);
    }

    /**
     * 콘텐츠 및 프레임 개수 조회
     *
     * @param box - 요청 정보 (검색 조건 등)
     * @return int - 콘텐츠 및 프레임 개수
     */
    public int selectContentWithFramesCount(RequestBox box) {
        logger.debug("콘텐츠 및 프레임 개수 조회 DAO - UUID: {}", box.getString("uuid"));
        return (int) sqlSession.selectOne(NAMESPACE + ".selectContentWithFramesCount", box);
    }

    /**
     * 사용자 스냅리뷰 조회
     *
     * @param box - 요청 정보 (contentSeq, uuid)
     * @return DataBox - 조회된 사용자 스냅리뷰 정보
     */
    public DataBox selectUserSnapReviewByContent(RequestBox box) {
        logger.debug("사용자 스냅리뷰 조회 DAO - 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("contentSeq"), box.getString("uuid"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectUserSnapReviewByContent", box);
    }

    /**
     * 사용자 스냅리뷰 등록
     *
     * @param box - 등록할 사용자 스냅리뷰 정보
     * @return int - 영향받은 행 수
     */
    public int insertUserSnapReview(RequestBox box) {
        logger.debug("사용자 스냅리뷰 등록 DAO - 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("contentSeq"), box.getString("uuid"));
        return sqlSession.insert(NAMESPACE + ".insertUserSnapReview", box);
    }

    /**
     * 프레임 및 콘텐츠별 사용자 스냅리뷰 조회
     *
     * @param box - 요청 정보 (frameSeq, contentSeq, uuid)
     * @return DataBox - 조회된 사용자 스냅리뷰 정보
     */
    public DataBox selectUserSnapReviewByFrameAndContent(RequestBox box) {
        logger.debug("프레임 및 콘텐츠별 사용자 스냅리뷰 조회 DAO - 프레임일련번호: {}, 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("frameSeq"), box.getLong("contentSeq"), box.getString("uuid"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectUserSnapReviewByFrameAndContent", box);
    }

    /**
     * 사용자 스냅리뷰 코멘트 업데이트
     *
     * @param box - 수정할 사용자 스냅리뷰 정보 (userSnapReviewSeq, comment, ldate, uuid)
     * @return int - 영향받은 행 수
     */
    public int updateUserSnapReviewComment(RequestBox box) {
        logger.debug("사용자 스냅리뷰 코멘트 업데이트 DAO - 스냅리뷰일련번호: {}, UUID: {}",
                box.getLong("userSnapReviewSeq"), box.getString("uuid"));
        return sqlSession.update(NAMESPACE + ".updateUserSnapReviewComment", box);
    }

    /**
     * 프레임 정보가 포함된 사용자 스냅리뷰 등록
     *
     * @param box - 등록할 사용자 스냅리뷰 정보
     * @return int - 영향받은 행 수
     */
    public int insertUserSnapReviewWithFrame(RequestBox box) {
        logger.debug("프레임 정보가 포함된 사용자 스냅리뷰 등록 DAO - 프레임일련번호: {}, 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("frameSeq"), box.getLong("contentSeq"), box.getString("uuid"));
        return sqlSession.insert(NAMESPACE + ".insertUserSnapReviewWithFrame", box);
    }

    /**
     * 공유 코드 등록
     *
     * @param box - 등록할 공유 코드 정보
     * @return int - 영향받은 행 수
     */
    public int insertShareCode(RequestBox box) {
        logger.debug("공유 코드 등록 DAO - 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("contentSeq"), box.getString("uuid"));
        return sqlSession.insert(NAMESPACE + ".insertShareCode", box);
    }

    /**
     * 공유 코드 정보 조회
     *
     * @param box - 요청 정보
     * @return DataBox - 조회된 공유 코드 정보
     */
    public DataBox selectShareCodeDataBox(RequestBox box) {
        logger.debug("공유 코드 정보 조회 DAO - 콘텐츠일련번호: {}, UUID: {}",
                box.getLong("contentSeq"), box.getString("uuid"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectShareCodeDataBox", box);
    }

    /**
     * 특정 사용자의 프레임 및 콘텐츠별 스냅리뷰 조회
     *
     * @param box - 요청 정보 (frameSeq, contentSeq, userSeq)
     * @return DataBox - 조회된 사용자 스냅리뷰 정보
     */
    public DataBox selectUserSnapReviewByFrameAndUserSeq(RequestBox box) {
        logger.debug("특정 사용자의 프레임 및 콘텐츠별 스냅리뷰 조회 DAO - 프레임일련번호: {}, 콘텐츠일련번호: {}, 사용자일련번호: {}",
                box.getLong("frameSeq"), box.getLong("contentSeq"), box.getLong("userSeq"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectUserSnapReviewByFrameAndUserSeq", box);
    }

    /**
     * 공유된 사용자의 프레임 및 콘텐츠별 스냅리뷰 조회
     *
     * @param box - 요청 정보 (frameSeq, contentSeq, sharedUserSeq)
     * @return DataBox - 조회된 사용자 스냅리뷰 정보
     */
    public DataBox selectUserSnapReviewBySharedUser(RequestBox box) {
        logger.debug("공유된 사용자의 프레임 및 콘텐츠별 스냅리뷰 조회 DAO - 프레임일련번호: {}, 콘텐츠일련번호: {}, 공유사용자일련번호: {}",
                box.getLong("frameSeq"), box.getLong("contentSeq"), box.getLong("sharedUserSeq"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectUserSnapReviewBySharedUser", box);
    }

    /**
     * 공유 코드 정보 조회
     *
     * @param box - 요청 정보 (shareCode)
     * @return DataBox - 조회된 공유 코드 정보
     */
    public DataBox getShareCodeInfo(RequestBox box) {
        logger.debug("공유 코드 정보 조회 DAO - 공유코드: {}", box.getString("shareCode"));
        return sqlSession.selectDataBox(NAMESPACE + ".getShareCodeInfo", box);
    }

    /**
     * 공유 코드 취소 (논리적 삭제)
     *
     * @param box - 요청 정보 (shareCode, uuid)
     * @return int - 영향받은 행 수
     */
    public int cancelShareCode(RequestBox box) {
        logger.debug("공유 코드 취소 DAO - 공유코드: {}, UUID: {}",
                box.getString("shareCode"), box.getString("uuid"));
        return sqlSession.update(NAMESPACE + ".cancelShareCode", box);
    }

    /**
     * 공유 코드로 사용자 시퀀스 조회
     *
     * @param box - 요청 정보 (shareCode)
     * @return DataBox - 조회된 사용자 시퀀스 정보
     */
    public DataBox selectUserSeqByShareCode(RequestBox box) {
        logger.debug("공유 코드로 사용자 시퀀스 조회 DAO - 공유코드: {}", box.getString("shareCode"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectUserSeqByShareCode", box);
    }
}