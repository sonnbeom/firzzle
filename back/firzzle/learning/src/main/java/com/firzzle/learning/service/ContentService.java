package com.firzzle.learning.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.*;
import com.firzzle.learning.dao.ContentDAO;
import com.firzzle.learning.kafka.producer.LearningProducer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
            String videoId = StringManager.extractYoutubeId(box.getString("youtubeUrl"));
            if (videoId == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 YouTube URL입니다.");
            }

            // 2. 중복 체크
            RequestBox checkBox = new RequestBox("checkBox");
            checkBox.put("videoId", videoId);
            DataBox existingContent = contentDAO.selectContentByVideoId(checkBox);

            logger.info("existingContent : {}", existingContent);

            Long contentSeq = null;

            if (existingContent != null) {
                // 콘텐츠 일련번호 가져오기
                contentSeq = existingContent.getLong2("d_content_seq");
                logger.info("이미 등록된 YouTube 동영상입니다. 사용자-콘텐츠 매핑만 생성합니다. ContentSeq: {}", contentSeq);

                // 이미 해당 사용자가 등록한 콘텐츠인지 확인
                RequestBox userContentCheckBox = new RequestBox("userContentCheckBox");
                userContentCheckBox.put("contentSeq", contentSeq);
                userContentCheckBox.put("uuid", box.getString("uuid"));
                int userContentCount = contentDAO.selectUserContentCount(userContentCheckBox);

                if (userContentCount > 0) {
                    logger.info("이미 해당 사용자가 등록한 콘텐츠입니다. ContentSeq: {}, UUID: {}",
                            contentSeq, box.getString("uuid"));

                    // 이미 등록된 콘텐츠 정보 조회 및 반환
                    RequestBox selectBox = new RequestBox("selectBox");
                    selectBox.put("contentSeq", contentSeq);
                    selectBox.put("uuid", box.getString("uuid"));
                    result = contentDAO.selectContentDataBoxByUuidContentSeq(selectBox); // 여기

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
                    result = contentDAO.selectContentDataBoxByUuidContentSeq(selectBox); // 여기

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
    public DataBox selectContentByUserContentSeq(RequestBox box) {
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
    public int selectContentCountByUuid(RequestBox box) {
        logger.debug("콘텐츠 개수 조회 요청 - UUID: {}", box.getString("uuid"));
        return contentDAO.selectContentCountByUuid(box);
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