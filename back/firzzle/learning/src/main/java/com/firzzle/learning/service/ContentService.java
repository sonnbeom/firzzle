package com.firzzle.learning.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.MyBatisTransactionManager;
import com.firzzle.common.library.RequestBox;
import com.firzzle.learning.dao.ContentDAO;
import com.firzzle.learning.kafka.producer.LearningProducer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Class Name : ContentService.java
 * @Description : 콘텐츠 관리 서비스
 * @author Firzzle
 * @since 2025. 4. 30.
 */
@Service
@RequiredArgsConstructor
public class ContentService {

    private static final Logger logger = LoggerFactory.getLogger(ContentService.class);

    private final ContentDAO contentDAO;
    private final LearningProducer learningProducer;
    private final MyBatisSupport myBatisSupport;

    @Value("${app.kafka.topic.content-analysis}")
    private String contentAnalysisTopic;

    // YouTube ID 추출 정규식 패턴
    private static final Pattern YOUTUBE_ID_PATTERN =
            Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})");
//            Pattern.compile(".*");  // 모든 문자열 패턴 매칭

    /**
     * 콘텐츠 등록
     * YouTube URL로부터 콘텐츠를 등록하고 분석 큐에 추가합니다.
     *
     * @param box - 등록할 콘텐츠 정보 (YouTube URL 포함)
     * @return DataBox - 등록된 콘텐츠 정보
     */
    public DataBox insertContent(RequestBox box) {
        logger.debug("콘텐츠 등록 요청 - YouTube URL: {}, UUID: {}",
                box.getString("youtubeUrl"), box.getString("uuid"));

        DataBox result = null;

        try {
            // 1. YouTube ID 추출
            String videoId = extractYoutubeId(box.getString("youtubeUrl"));
            if (videoId == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 YouTube URL입니다.");
            }

            // 2. 중복 체크
            RequestBox checkBox = new RequestBox("checkBox");
            checkBox.put("videoId", videoId);
            DataBox existingContent = contentDAO.selectContentByVideoId(checkBox);
            Long contentSeq = null;

            if (existingContent != null) {
                // 콘텐츠 일련번호 가져오기
                contentSeq = existingContent.getLong2("d_content_seq");
                logger.debug("이미 등록된 YouTube 동영상입니다. 사용자-콘텐츠 매핑만 생성합니다. ContentSeq: {}", contentSeq);

                // 이미 해당 사용자가 등록한 콘텐츠인지 확인
                RequestBox userContentCheckBox = new RequestBox("userContentCheckBox");
                userContentCheckBox.put("contentSeq", contentSeq);
                userContentCheckBox.put("uuid", box.getString("uuid"));
                int userContentCount = contentDAO.selectUserContentCount(userContentCheckBox);

                if (userContentCount > 0) {
                    logger.debug("이미 해당 사용자가 등록한 콘텐츠입니다. ContentSeq: {}, UUID: {}",
                            contentSeq, box.getString("uuid"));

                    // 이미 등록된 콘텐츠 정보 조회 및 반환
                    RequestBox selectBox = new RequestBox("selectBox");
                    selectBox.put("contentSeq", contentSeq);
                    selectBox.put("uuid", box.getString("uuid"));
                    result = contentDAO.selectContentDataBox(selectBox);

                    // 이미 분석 완료된 콘텐츠는 taskId를 포함하지 않음
                    return result;
                } else {
                    // 해당 사용자가 등록하지 않은 기존 콘텐츠인 경우, 사용자-콘텐츠 매핑 추가
                    RequestBox userContentBox = new RequestBox("userContentBox");
                    userContentBox.put("contentSeq", contentSeq);
                    userContentBox.put("uuid", box.getString("uuid"));
                    userContentBox.put("indate", FormatDate.getDate("yyyyMMddHHmmss"));
                    contentDAO.insertUserContent(userContentBox);

                    // 콘텐츠 정보 조회 및 반환
                    RequestBox selectBox = new RequestBox("selectBox");
                    selectBox.put("contentSeq", contentSeq);
                    selectBox.put("uuid", box.getString("uuid"));
                    result = contentDAO.selectContentDataBox(selectBox);

                    // 이미 분석 완료된 콘텐츠는 taskId를 포함하지 않고 반환
                    return result;
                }
            }

            // 분석 작업 큐에 등록
            String uuid = box.getString("uuid");
            String youtubeUrl = box.getString("youtubeUrl");

            // taskId 생성 및 분석 큐에 등록
            String taskId = sendToAnalysisQueue(uuid, youtubeUrl);

            // 결과 DataBox 생성 (신규 등록이므로 콘텐츠 정보는 아직 없음)
            if (result == null) {
                result = new DataBox();
            }
            // taskId 추가
            result.put("taskId", taskId);

            logger.info("신규 콘텐츠 등록 요청 완료 - 사용자: {}, URL: {}, TaskId: {}",
                    uuid, youtubeUrl, taskId);

            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠 등록 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 등록 중 오류가 발생했습니다.");
        }
    }

    /**
     * 콘텐츠 정보 조회
     * 특정 콘텐츠의 상세 정보를 조회합니다.
     *
     * @param box - 조회할 콘텐츠 일련번호, UUID
     * @return DataBox - 조회된 콘텐츠 정보
     */
    public DataBox selectContent(RequestBox box) {
        logger.debug("콘텐츠 정보 조회 요청 - UserContentSeq: {}, UUID: {}",
                box.getLong("userContentSeq"), box.getString("uuid"));

        DataBox content = contentDAO.selectContentByUserContentSeq(box);
        if (content == null) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "요청한 콘텐츠를 찾을 수 없습니다.");
        }

        logger.debug("콘텐츠 정보 조회 완료 - ContentSeq: {}, Title: {}",
                content.getString("d_content_seq"), content.getString("d_title"));
        return content;
    }

    /**
     * 콘텐츠 목록 조회
     * 조건에 맞는 콘텐츠 목록을 페이지네이션하여 조회합니다.
     *
     * @param box - 페이지 요청 정보
     * @return List<DataBox> - 조회된 콘텐츠 목록
     */
    public List<DataBox> selectContentList(RequestBox box) {
        logger.debug("콘텐츠 목록 조회 요청 - Page: {}, Size: {}, UUID: {}",
                box.getInt("p_pageno"), box.getInt("p_pagesize"), box.getString("uuid"));

        List<DataBox> contentList = contentDAO.selectContentListDataBox(box);
        logger.debug("콘텐츠 목록 조회 완료 - 조회된 콘텐츠 수: {}", contentList.size());

        return contentList;
    }

    /**
     * 콘텐츠 개수 조회
     *
     * @param box - 조건이 포함된 RequestBox
     * @return int - 조회된 콘텐츠 개수
     */
    public int selectContentCount(RequestBox box) {
        logger.debug("콘텐츠 개수 조회 요청 - UUID: {}", box.getString("uuid"));
        return contentDAO.selectContentCount(box);
    }

    /**
     * 태그별 콘텐츠 개수 조회
     *
     * @param box - 태그 조건이 포함된 RequestBox
     * @return int - 조회된 콘텐츠 개수
     */
    public int selectContentCountByTag(RequestBox box) {
        return contentDAO.selectContentCountByTag(box);
    }

    /**
     * 콘텐츠 정보 수정
     * 콘텐츠의 기본 정보를 수정합니다.
     *
     * @param box - 수정할 콘텐츠 정보
     * @return DataBox - 수정된 콘텐츠 정보
     */
    public DataBox updateContent(RequestBox box) {
        logger.debug("콘텐츠 정보 수정 요청 - ContentSeq: {}", box.getLong("contentSeq"));

        MyBatisTransactionManager transaction = myBatisSupport.getTransactionManager();
        DataBox result = null;

        try {
            // 트랜잭션 시작
            transaction.start();

            // 1. 기존 콘텐츠 조회
            RequestBox selectBox = new RequestBox("selectBox");
            selectBox.put("contentSeq", box.getLong("contentSeq"));
            DataBox existingContent = contentDAO.selectContentDataBox(selectBox);

            if (existingContent == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "수정할 콘텐츠를 찾을 수 없습니다.");
            }

            // 2. 콘텐츠 업데이트
            int updateResult = contentDAO.updateContent(box);
            if (updateResult == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 수정에 실패했습니다.");
            }

            // 3. 태그 처리 (기존 태그 삭제 후 새로 등록)
            String tags = box.getString("tags");
            if (StringUtils.hasText(tags)) {
                // 기존 태그 삭제
                RequestBox deleteTagBox = new RequestBox("deleteTagBox");
                deleteTagBox.put("contentSeq", box.getLong("contentSeq"));
                contentDAO.deleteContentTags(deleteTagBox);

                // 새로운 태그 등록
                List<String> tagList = Arrays.stream(tags.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());

                if (!tagList.isEmpty()) {
                    RequestBox tagBox = new RequestBox("tagBox");
                    tagBox.put("contentSeq", box.getLong("contentSeq"));
                    tagBox.put("tags", tagList);
                    contentDAO.insertContentTags(tagBox);
                }
            }

            logger.info("콘텐츠 정보 수정 완료 - ContentSeq: {}", box.getLong("contentSeq"));

            // 4. 수정된 콘텐츠 정보 조회 및 반환
            result = contentDAO.selectContentDataBox(selectBox);

            // 성공 시 커밋
            transaction.commit();
            return result;

        } catch (BusinessException e) {
            transaction.rollback();
            throw e;
        } catch (Exception e) {
            transaction.rollback();
            logger.error("콘텐츠 수정 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 수정 중 오류가 발생했습니다.");
        } finally {
            // 트랜잭션 종료
            transaction.end();
        }
    }

    /**
     * 콘텐츠 삭제
     * 콘텐츠를 논리적으로 삭제합니다. (delete_yn = 'Y')
     *
     * @param box - 삭제할 콘텐츠 정보
     * @return boolean - 삭제 성공 여부
     */
    public boolean deleteContent(RequestBox box) {
        logger.debug("콘텐츠 삭제 요청 - ContentSeq: {}", box.getLong("contentSeq"));

        MyBatisTransactionManager transaction = myBatisSupport.getTransactionManager();
        boolean result = false;

        try {
            // 트랜잭션 시작
            transaction.start();

            // 1. 기존 콘텐츠 조회
            RequestBox selectBox = new RequestBox("selectBox");
            selectBox.put("contentSeq", box.getLong("contentSeq"));
            DataBox existingContent = contentDAO.selectContentDataBox(selectBox);

            if (existingContent == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "삭제할 콘텐츠를 찾을 수 없습니다.");
            }

            // 2. 콘텐츠 삭제 (논리적 삭제)
            int deleteResult = contentDAO.deleteContent(box);
            result = deleteResult > 0;

            // 성공 시 커밋
            transaction.commit();

            logger.info("콘텐츠 삭제 완료 - ContentSeq: {}, 결과: {}", box.getLong("contentSeq"), result);
            return result;

        } catch (BusinessException e) {
            transaction.rollback();
            throw e;
        } catch (Exception e) {
            transaction.rollback();
            logger.error("콘텐츠 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 삭제 중 오류가 발생했습니다.");
        } finally {
            // 트랜잭션 종료
            transaction.end();
        }
    }

    /**
     * 콘텐츠 분석 상태 업데이트
     * 콘텐츠의 분석 상태 및 관련 데이터를 업데이트합니다.
     *
     * @param box - 업데이트할 콘텐츠 정보
     * @return DataBox - 업데이트된 콘텐츠 정보
     */
    public DataBox updateAnalysisStatus(RequestBox box) {
        logger.debug("콘텐츠 분석 상태 업데이트 요청 - ContentSeq: {}, Status: {}",
                box.getLong("contentSeq"), box.getString("status"));

        MyBatisTransactionManager transaction = myBatisSupport.getTransactionManager();
        DataBox result = null;

        try {
            // 트랜잭션 시작
            transaction.start();

            // 1. 기존 콘텐츠 조회
            RequestBox selectBox = new RequestBox("selectBox");
            selectBox.put("contentSeq", box.getLong("contentSeq"));
            DataBox existingContent = contentDAO.selectContentDataBox(selectBox);

            if (existingContent == null) {
                throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "업데이트할 콘텐츠를 찾을 수 없습니다.");
            }

            // 2. 상태 검증
            String status = box.getString("status");
            if (!isValidStatus(status)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 분석 상태 코드입니다.");
            }

            // 3. 상태 업데이트
            int updateResult = contentDAO.updateAnalysisStatus(box);
            if (updateResult == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 분석 상태 업데이트에 실패했습니다.");
            }

            logger.info("콘텐츠 분석 상태 업데이트 완료 - ContentSeq: {}, Status: {}",
                    box.getLong("contentSeq"), box.getString("status"));

            // 4. 업데이트된 콘텐츠 정보 조회 및 반환
            result = contentDAO.selectContentDataBox(selectBox);

            // 성공 시 커밋
            transaction.commit();
            return result;

        } catch (BusinessException e) {
            transaction.rollback();
            throw e;
        } catch (Exception e) {
            transaction.rollback();
            logger.error("콘텐츠 분석 상태 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 분석 상태 업데이트 중 오류가 발생했습니다.");
        } finally {
            // 트랜잭션 종료
            transaction.end();
        }
    }

    /**
     * 태그별 콘텐츠 목록 조회
     * 특정 태그를 가진 콘텐츠 목록을 페이지네이션하여 조회합니다.
     *
     * @param box - 태그와 페이지 정보가 포함된 RequestBox
     * @return List<DataBox> - 조회된 콘텐츠 목록
     */
    public List<DataBox> selectContentListByTag(RequestBox box) {
        logger.debug("태그별 콘텐츠 목록 조회 요청 - Tag: {}, Page: {}, Size: {}",
                box.getString("tag"), box.getInt("p_pageno"), box.getInt("p_pagesize"));

        List<DataBox> contentList = contentDAO.selectContentListByTagDataBox(box);
        logger.debug("태그별 콘텐츠 목록 조회 완료 - Tag: {}, 조회된 콘텐츠 수: {}",
                box.getString("tag"), contentList.size());

        return contentList;
    }

    /**
     * YouTube URL에서 ID 추출
     *
     * @param youtubeUrl - YouTube URL
     * @return String - YouTube ID
     */
    private String extractYoutubeId(String youtubeUrl) {
        if (!StringUtils.hasText(youtubeUrl)) {
            return null;
        }

        Matcher matcher = YOUTUBE_ID_PATTERN.matcher(youtubeUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * YouTube 썸네일 URL 생성
     *
     * @param videoId - YouTube ID
     * @return String - 썸네일 URL
     */
    private String generateThumbnailUrl(String videoId) {
        return "https://img.youtube.com/vi/" + videoId + "/mqdefault.jpg";
    }

    /**
     * 분석 상태 코드 유효성 검사
     *
     * @param status - 분석 상태 코드
     * @return boolean - 유효 여부
     */
    private boolean isValidStatus(String status) {
        return status != null && (status.equals("Q") || status.equals("P") ||
                status.equals("C") || status.equals("F"));
    }

    /**
     * 콘텐츠 분석 큐에 등록하고 taskId 반환
     *
     * @param uuid - 사용자 일련번호
     * @param url - YouTube URL
     * @return String - 작업 추적 ID (taskId)
     */
    private String sendToAnalysisQueue(String uuid, String url) {
        try {
            // taskId 생성
            String taskId = UUID.randomUUID().toString();

            // 기존 메시지 형식: "uuid|url" -> "uuid|url|taskId"
            String message = uuid + "|" + url + "|" + taskId;

            // LearningProducer 사용
            learningProducer.sendToStt(message);

            logger.debug("콘텐츠 분석 큐에 등록 완료 - 사용자: {}, URL: {}, TaskId: {}", uuid, url, taskId);

            // 생성된 taskId 반환
            return taskId;
        } catch (Exception e) {
            logger.error("콘텐츠 분석 큐 등록 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "분석 요청 처리 중 오류가 발생했습니다.");
        }
    }
}