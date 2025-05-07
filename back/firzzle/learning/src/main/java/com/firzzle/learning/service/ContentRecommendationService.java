package com.firzzle.learning.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.learning.dao.ContentRecommendationDAO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Class Name : ContentRecommendationService.java
 * @Description : 콘텐츠 추천 서비스
 * @author Firzzle
 * @since 2025. 5. 3.
 */
@Service
@RequiredArgsConstructor
public class ContentRecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(ContentRecommendationService.class);

    private final ContentRecommendationDAO recommendationDAO;

    /**
     * 콘텐츠 추천 목록 조회
     * 현재 콘텐츠와 관련된 추천 콘텐츠 목록을 조회합니다.
     *
     * @param box - 요청 정보가 담긴 RequestBox (userContentSeq 필수)
     * @return List<DataBox> - 추천 콘텐츠 목록
     */
    public List<DataBox> getRecommendations(RequestBox box) {
        logger.debug("콘텐츠 추천 목록 조회 요청 - UserContentSeq: {}", box.getLong("userContentSeq"));

        try {
            // 콘텐츠 태그 기반 추천 콘텐츠 목록 조회
            List<DataBox> recommendedContents = recommendationDAO.selectRecommendedContents(box);
            logger.debug("콘텐츠 추천 목록 조회 완료 - 추천 콘텐츠 수: {}", recommendedContents.size());

            return recommendedContents;
        } catch (Exception e) {
            logger.error("콘텐츠 추천 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 추천 목록 조회 중 오류가 발생했습니다.");
        }
    }
}