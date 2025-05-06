package com.firzzle.learning.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.MyBatisTransactionManager;
import com.firzzle.common.library.RequestBox;
import com.firzzle.learning.dao.SnapReviewDAO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Class Name : SnapReviewService.java
 * @Description : 스냅리뷰 관련 서비스
 * @author Firzzle
 * @since 2025. 5. 04.
 */
@Service
@RequiredArgsConstructor
public class SnapReviewService {

    private static final Logger logger = LoggerFactory.getLogger(SnapReviewService.class);

    private final SnapReviewDAO snapReviewDAO;
    private final MyBatisSupport myBatisSupport;

    @Value("${app.url.base}")
    private String baseUrl;

    /**
     * 콘텐츠와 프레임 정보 조회
     * 특정 콘텐츠와 관련된 프레임 정보를 조회합니다.
     *
     * @param box 요청 정보 (userContentSeq, uuid)
     * @return DataBox 콘텐츠와 프레임 정보
     */
    public DataBox selectContentWithFrames(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("콘텐츠와 프레임 정보 조회 요청 - UserContentSeq: {}, UUID: {}", box.getLong("userContentSeq"), uuid);

        try {
            // 콘텐츠 존재 여부 확인
            RequestBox contentBox = new RequestBox("contentBox");
            contentBox.put("userContentSeq", box.getLong("userContentSeq"));
            contentBox.put("uuid", uuid);
            DataBox content = snapReviewDAO.selectContentByUserContentSeq(contentBox);

            logger.debug("contentBox 조회 결과: {}", content != null ? "성공" : "실패");

            if (content == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "요청한 콘텐츠를 찾을 수 없습니다.");
            }

            // "d_" 접두어로 접근
            Long contentSeq = content.getLong2("d_content_seq");
            logger.debug("추출된 contentSeq 값: {}", contentSeq);

            if (contentSeq == null || contentSeq <= 0) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND,
                        "사용자 콘텐츠에 연결된 콘텐츠를 찾을 수 없습니다. UserContentSeq: " + box.getLong("userContentSeq"));
            }

            // 프레임 목록 조회
            RequestBox frameBox = new RequestBox("frameBox");
            frameBox.put("contentSeq", contentSeq);
            frameBox.put("uuid", uuid);
            List<DataBox> frames = snapReviewDAO.selectFramesByContent(frameBox);

            // 각 프레임에 대한 사용자 스냅리뷰 조회 또는 생성
            String currentDate = FormatDate.getDate("yyyyMMddHHmmss");

            for (DataBox frame : frames) {
                Long frameSeq = frame.getLong2("d_frame_seq");

                // 사용자 스냅리뷰 조회
                RequestBox reviewBox = new RequestBox("reviewBox");
                reviewBox.put("contentSeq", contentSeq);
                reviewBox.put("frameSeq", frameSeq);
                reviewBox.put("uuid", uuid);
                DataBox userSnapReview = snapReviewDAO.selectUserSnapReviewByFrameAndContent(reviewBox);

                // 사용자 스냅리뷰가 없으면 생성
                if (userSnapReview == null) {
                    reviewBox.put("indate", currentDate);
                    reviewBox.put("comment", ""); // 빈 코멘트로 초기화
                    int insertResult = snapReviewDAO.insertUserSnapReviewWithFrame(reviewBox);

                    if (insertResult == 0) {
                        logger.warn("프레임에 대한 스냅리뷰 생성 실패 - ContentSeq: {}, FrameSeq: {}", contentSeq, frameSeq);
                    }

                    // 생성된 스냅리뷰 조회
                    userSnapReview = snapReviewDAO.selectUserSnapReviewByFrameAndContent(reviewBox);
                }

                // 프레임에 코멘트 정보 추가
                if (userSnapReview != null) {
                    frame.put("d_comment", userSnapReview.getString("d_comment"));
                } else {
                    frame.put("d_comment", "");
                }
            }

            // 결과 DataBox 생성
            DataBox result = new DataBox("contentWithFramesBox");
            result.put("d_content_seq", contentSeq);
            result.put("d_title", content.getString("d_title"));
            result.put("d_thumbnail_url", content.getString("d_thumbnail_url"));
            result.put("d_indate", currentDate);
            result.put("d_frames", frames);

            logger.debug("콘텐츠와 프레임 정보 조회 완료 - ContentSeq: {}, 프레임 수: {}, UUID: {}",
                    contentSeq, frames.size(), uuid);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠와 프레임 정보 조회 중 오류 발생: {}, UUID: {}", e.getMessage(), uuid, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠와 프레임 정보 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 콘텐츠 목록 및 프레임 정보 조회
     * 사용자의 콘텐츠 목록과 관련 프레임 정보를 조회합니다.
     *
     * @param box 요청 정보 (페이징, 검색 조건 등)
     * @return List<DataBox> 콘텐츠 및 프레임 정보 목록
     */
    public List<DataBox> selectContentListWithFrames(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("콘텐츠 목록 및 프레임 정보 조회 요청 - Page: {}, Size: {}, UUID: {}",
                box.getInt("p_pageno"), box.getInt("p_pagesize"), uuid);

        try {
            // 콘텐츠 및 프레임 목록 조회
            List<DataBox> contentList = snapReviewDAO.selectContentListWithFrames(box);
            logger.debug("콘텐츠 목록 및 프레임 정보 조회 완료 - 조회된 콘텐츠 수: {}, UUID: {}", contentList.size(), uuid);

            return contentList;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠 목록 및 프레임 정보 조회 중 오류 발생: {}, UUID: {}", e.getMessage(), uuid, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 목록 및 프레임 정보 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 콘텐츠 및 프레임 정보 개수 조회
     * 사용자의 콘텐츠 및 프레임 정보 전체 개수를 조회합니다.
     *
     * @param box 요청 정보 (검색 조건 등)
     * @return int 콘텐츠 및 프레임 정보 개수
     */
    public int selectContentWithFramesCount(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("콘텐츠 및 프레임 정보 개수 조회 요청 - UUID: {}", uuid);

        try {
            // 콘텐츠 및 프레임 정보 개수 조회
            int count = snapReviewDAO.selectContentWithFramesCount(box);
            logger.debug("콘텐츠 및 프레임 정보 개수 조회 완료 - 총 개수: {}, UUID: {}", count, uuid);

            return count;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠 및 프레임 정보 개수 조회 중 오류 발생: {}, UUID: {}", e.getMessage(), uuid, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 및 프레임 정보 개수 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 스냅리뷰 공유 코드 생성
     * 스냅리뷰 공유를 위한 코드를 생성합니다.
     *
     * @param box 요청 정보 (userContentSeq, uuid)
     * @return DataBox 생성된 공유 코드 정보
     */
    public DataBox createShareCode(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("스냅리뷰 공유 코드 생성 요청 - UserContentSeq: {}, UUID: {}", box.getLong("userContentSeq"), uuid);

        MyBatisTransactionManager transaction = myBatisSupport.getTransactionManager();
        DataBox result = null;
        String shareCode = null;
        int maxAttempts = 5; // 최대 시도 횟수

        try {
            // 트랜잭션 시작
            transaction.start();

            // 콘텐츠 존재 여부 확인
            RequestBox contentBox = new RequestBox("contentBox");
            contentBox.put("userContentSeq", box.getLong("userContentSeq"));
            contentBox.put("uuid", uuid);
            DataBox content = snapReviewDAO.selectContentByUserContentSeq(contentBox);

            if (content == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "요청한 콘텐츠를 찾을 수 없습니다.");
            }
            Long contentSeq = content.getLong2("d_content_seq");

            if (contentSeq == null || contentSeq == 0) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠 정보가 올바르지 않습니다.");
            }

            DataBox shareCodeData = snapReviewDAO.checkShareCodeExistsByContent(contentBox);

            if (shareCodeData != null) {
                throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 공유코드가 존재합니다.");
            }

            // 고유한 공유 코드 생성 (중복 체크)
            boolean codeGenerated = false;

            for (int attempt = 0; attempt < maxAttempts && !codeGenerated; attempt++) {
                // 12자리 랜덤 코드 생성 (충돌 가능성 최소화)
                shareCode = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

                // 중복 체크
                RequestBox checkBox = new RequestBox("checkBox");
                checkBox.put("shareCode", shareCode);
                DataBox existingCode = snapReviewDAO.checkShareCodeExists(checkBox);

                if (existingCode == null) {
                    codeGenerated = true;
                }
            }

            if (!codeGenerated) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공유 코드 생성에 실패했습니다. 다시 시도해주세요.");
            }

            // 공유 코드 등록
            RequestBox shareBox = new RequestBox("shareBox");
            shareBox.put("shareCode", shareCode);
            shareBox.put("contentSeq", contentSeq);
            shareBox.put("uuid", uuid);
            shareBox.put("indate", FormatDate.getDate("yyyyMMddHHmmss"));

            int insertResult = snapReviewDAO.insertShareCode(shareBox);
            if (insertResult == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공유 코드 생성에 실패했습니다.");
            }

            // 생성된 공유 코드 조회
            shareCodeData = snapReviewDAO.selectShareCodeDataBox(shareBox);

            if (shareCodeData == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공유 코드 조회에 실패했습니다.");
            }

            // 현재 서버의 도메인으로 풀 URL 생성
            String fullShareUrl = baseUrl + "/share/" + shareCode;

            // 결과 DataBox 생성
            result = new DataBox("shareCodeResultBox");
            result.put("d_share_code", shareCode);
            result.put("d_content_seq", contentSeq);
            result.put("d_share_url", fullShareUrl);
            result.put("d_indate", shareCodeData.getString("d_indate"));

            // 성공 시 커밋
            transaction.commit();
            logger.debug("스냅리뷰 공유 코드 생성 완료 - ContentSeq: {}, ShareCode: {}, UUID: {}",
                    contentSeq, shareCode, uuid);
            return result;

        } catch (BusinessException e) {
            transaction.rollback();
            throw e;
        } catch (Exception e) {
            transaction.rollback();
            logger.error("스냅리뷰 공유 코드 생성 중 오류 발생: {}, UUID: {}", e.getMessage(), uuid, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "스냅리뷰 공유 코드 생성 중 오류가 발생했습니다.");
        } finally {
            // 트랜잭션 종료
            transaction.end();
        }
    }

    /**
     * 프레임 정보 일괄 수정
     * 콘텐츠의 프레임 정보를 일괄 수정합니다.
     *
     * @param box 수정 정보 (userContentSeq, frameSeq_n, topic_n, description_n, frameCount)
     * @return List<DataBox> 수정된 프레임 정보 목록
     */
    public List<DataBox> updateFrames(RequestBox box) {
        String uuid = box.getString("uuid");
        logger.debug("프레임 정보 일괄 수정 요청 - UserContentSeq: {}, UUID: {}", box.getLong("userContentSeq"), uuid);

        MyBatisTransactionManager transaction = myBatisSupport.getTransactionManager();
        List<DataBox> result = null;

        try {
            // 트랜잭션 시작
            transaction.start();

            // 콘텐츠 존재 여부 확인
            RequestBox contentBox = new RequestBox("contentBox");
            contentBox.put("userContentSeq", box.getLong("userContentSeq"));
            contentBox.put("uuid", uuid);
            DataBox content = snapReviewDAO.selectContentByUserContentSeq(contentBox);

            if (content == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "요청한 콘텐츠를 찾을 수 없습니다.");
            }
            Long contentSeq = content.getLong2("d_content_seq");

            if (contentSeq == null || contentSeq == 0) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠 정보가 올바르지 않습니다.");
            }

            // 프레임 정보 수정
            int frameCount = box.getInt("frameCount");
            if (frameCount <= 0) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "수정할 프레임 정보가 없습니다.");
            }

            String currentDate = FormatDate.getDate("yyyyMMddHHmmss");

            for (int i = 0; i < frameCount; i++) {
                Long frameSeq = box.getLong("frameSeq_" + i);
                String comment = box.getString("comment_" + i);

                // 프레임 존재 여부 확인
                RequestBox checkBox = new RequestBox("checkBox");
                checkBox.put("frameSeq", frameSeq);
                checkBox.put("contentSeq", contentSeq);
                DataBox frame = snapReviewDAO.selectFrameBySeq(checkBox);

                if (frame == null) {
                    throw new BusinessException(ErrorCode.FRAME_NOT_FOUND, "프레임을 찾을 수 없습니다. (frameSeq: " + frameSeq + ")");
                }

                // 사용자 스냅리뷰 조회 또는 생성
                RequestBox reviewBox = new RequestBox("reviewBox");
                reviewBox.put("contentSeq", contentSeq);
                reviewBox.put("frameSeq", frameSeq);
                reviewBox.put("uuid", uuid);
                DataBox userSnapReview = snapReviewDAO.selectUserSnapReviewByFrameAndContent(reviewBox);

                if (userSnapReview == null) {
                    // 새로운 스냅리뷰 생성
                    reviewBox.put("comment", comment);
                    reviewBox.put("indate", currentDate);
                    int insertResult = snapReviewDAO.insertUserSnapReviewWithFrame(reviewBox);

                    if (insertResult == 0) {
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "사용자 스냅리뷰 생성에 실패했습니다.");
                    }
                } else {
                    // 기존 스냅리뷰 업데이트
                    reviewBox.put("userSnapReviewSeq", userSnapReview.getLong2("d_user_snap_review_seq"));
                    reviewBox.put("comment", comment);
                    reviewBox.put("ldate", currentDate);
                    int updateResult = snapReviewDAO.updateUserSnapReviewComment(reviewBox);

                    if (updateResult == 0) {
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "사용자 스냅리뷰 수정에 실패했습니다.");
                    }
                }
            }

            // 수정된 프레임 목록 조회
            RequestBox frameBox = new RequestBox("frameBox");
            frameBox.put("contentSeq", contentSeq);
            frameBox.put("uuid", uuid);
            result = snapReviewDAO.selectFramesByContent(frameBox);

            // 성공 시 커밋
            transaction.commit();
            logger.debug("프레임 정보 일괄 수정 완료 - ContentSeq: {}, 수정된 프레임 수: {}, UUID: {}",
                    contentSeq, frameCount, uuid);
            return result;

        } catch (BusinessException e) {
            transaction.rollback();
            throw e;
        } catch (Exception e) {
            transaction.rollback();
            logger.error("프레임 정보 일괄 수정 중 오류 발생: {}, UUID: {}", e.getMessage(), uuid, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "프레임 정보 일괄 수정 중 오류가 발생했습니다.");
        } finally {
            // 트랜잭션 종료
            transaction.end();
        }
    }

    /**
     * 공유 코드로 콘텐츠와 프레임 정보 조회
     * 공유 코드를 통해 콘텐츠와 관련된 프레임 정보를 조회합니다.
     *
     * @param box 요청 정보 (shareCode)
     * @return DataBox 콘텐츠와 프레임 정보
     */
    public DataBox selectSharedContentWithFrames(RequestBox box) {
        String shareCode = box.getString("shareCode");
        logger.debug("공유 코드로 콘텐츠와 프레임 정보 조회 요청 - 공유 코드: {}", shareCode);

        try {
            // 공유 코드 존재 여부 확인
            RequestBox shareBox = new RequestBox("shareBox");
            shareBox.put("shareCode", shareCode);
            DataBox shareCodeData = snapReviewDAO.checkShareCodeExists(shareBox);

            if (shareCodeData == null) {
                throw new BusinessException(ErrorCode.SHARE_CODE_NOT_FOUND, "요청한 공유 코드를 찾을 수 없습니다.");
            }

            // 공유 코드로부터 contentSeq 및 userSeq 추출
            Long contentSeq = shareCodeData.getLong2("d_content_seq");
            Long userSeq = shareCodeData.getLong2("d_user_seq");

            if (contentSeq == null || contentSeq <= 0) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "공유 코드에 연결된 콘텐츠를 찾을 수 없습니다.");
            }

            // 콘텐츠 정보 조회
            RequestBox contentBox = new RequestBox("contentBox");
            contentBox.put("contentSeq", contentSeq);
            DataBox content = snapReviewDAO.selectContentDataBox(contentBox);

            if (content == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "요청한 콘텐츠를 찾을 수 없습니다.");
            }

            // 프레임 목록 조회
            RequestBox frameBox = new RequestBox("frameBox");
            frameBox.put("contentSeq", contentSeq);
            List<DataBox> frames = snapReviewDAO.selectFramesByUserSeqContent(frameBox);

            // 공유 코드 생성자의 스냅리뷰를 각 프레임에 연결
            if (frames != null && !frames.isEmpty()) {
                for (DataBox frame : frames) {
                    Long frameSeq = frame.getLong2("d_frame_seq");

                    // 스냅리뷰 조회
                    RequestBox snapReviewBox = new RequestBox("snapReviewBox");
                    snapReviewBox.put("contentSeq", contentSeq);
                    snapReviewBox.put("frameSeq", frameSeq);
                    snapReviewBox.put("sharedUserSeq", userSeq); // userSeq 직접 사용 대신 sharedUserSeq로 전달
                    DataBox userSnapReview = snapReviewDAO.selectUserSnapReviewBySharedUser(snapReviewBox);

                    // 코멘트 정보 추가
                    if (userSnapReview != null) {
                        frame.put("d_comment", userSnapReview.getString("d_comment"));
                    } else {
                        frame.put("d_comment", "");
                    }
                }
            }

            // 결과 DataBox 생성 (getSnapReview와 동일한 포맷)
            DataBox result = new DataBox("sharedContentWithFramesBox");
            result.put("d_content_seq", contentSeq);
            result.put("d_title", content.getString("d_title"));
            result.put("d_thumbnail_url", content.getString("d_thumbnail_url"));
            result.put("d_indate", shareCodeData.getString("d_indate"));
            result.put("d_frames", frames);

            logger.debug("공유 코드로 콘텐츠와 프레임 정보 조회 완료 - 공유 코드: {}, ContentSeq: {}, 프레임 수: {}",
                    shareCode, contentSeq, frames != null ? frames.size() : 0);
            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("공유 코드로 콘텐츠와 프레임 정보 조회 중 오류 발생: {}, 공유 코드: {}", e.getMessage(), shareCode, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공유 코드로 콘텐츠와 프레임 정보 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 콘텐츠로 공유 코드 여부 조회
     *
     * @param box 요청 정보 (콘텐츠 Seq)
     * @return DataBox 콘텐츠와 프레임 정보
     */
    public DataBox selectShareCodeByContent(RequestBox box) {

        logger.info("스냅리뷰 공유 여부 조회 요청 - 사용자 콘텐츠 일련번호: {}, UUID: {}",box.getString("userContentSeq"), box.getString("uuid"));

        DataBox result = null;
        try {
            DataBox shareCodeData = snapReviewDAO.checkShareCodeExistsByContent(box);

            if (shareCodeData == null) {
                return null;
            }

            // 공유 코드로부터 contentSeq 및 userSeq 추출
            Long contentSeq = shareCodeData.getLong2("d_content_seq");
            Long userSeq = shareCodeData.getLong2("d_user_seq");
            String shareCode = shareCodeData.getString("d_share_code");

            if (contentSeq == null || contentSeq <= 0) {
                return null;
            }

            // 현재 서버의 도메인으로 풀 URL 생성
            String fullShareUrl = baseUrl + "/share/" + shareCode;

            // 결과 DataBox 생성
            result = new DataBox("shareCodeResultBox");
            result.put("d_share_code", shareCode);
            result.put("d_share_url", fullShareUrl);
            result.put("d_indate", shareCodeData.getString("d_indate"));
            result.put("d_content_seq", contentSeq);

            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("공유 코드로 콘텐츠와 프레임 정보 조회 중 오류 발생: {}, ", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공유 코드로 콘텐츠와 프레임 정보 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 콘텐츠 공유 취소
     *
     * @param box 요청 정보 (shareCode)
     * @return DataBox 공유 취소 결과
     */
    public DataBox cancelShareCode(RequestBox box) {
        String shareCode = box.getString("shareCode");
        String uuid = box.getString("uuid");

        logger.info("스냅리뷰 공유 취소 요청 - 공유 코드: {}, UUID: {}", shareCode, uuid);

        try {
            // uuid가 존재하는지 확인
            if (uuid == null || uuid.isEmpty()) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요한 서비스입니다.");
            }

            // 공유 코드 유효성 확인
            DataBox shareCodeData = snapReviewDAO.getShareCodeInfo(box);

            if (shareCodeData == null) {
                throw new BusinessException(ErrorCode.SHARE_CODE_NOT_FOUND, "취소할 공유 코드를 찾을 수 없습니다.");
            }

            // 공유 코드가 이미 삭제되었는지 확인
            if ("Y".equals(shareCodeData.getString("d_delete_yn"))) {
                throw new BusinessException(ErrorCode.ALREADY_DELETED, "이미 취소된 공유 코드입니다.");
            }

            // 공유 코드 취소 처리 (논리적 삭제)
            int result = snapReviewDAO.cancelShareCode(box);

            if (result <= 0) {
                throw new BusinessException(ErrorCode.UPDATE_FAILED, "공유 코드 취소 처리에 실패했습니다.");
            }

            // 결과 DataBox 생성
            DataBox resultBox = new DataBox("cancelShareResultBox");
            resultBox.put("d_canceled", true);
            resultBox.put("d_canceled_date", FormatDate.getDate("yyyy-MM-dd HH:mm:ss"));
            resultBox.put("d_share_code", shareCode);

            return resultBox;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("공유 코드 취소 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공유 코드 취소 중 오류가 발생했습니다.");
        }
    }
}