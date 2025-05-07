package com.firzzle.learning.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.learning.dao.ExpertRecommendationDAO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Class Name : ExpertRecommendationService.java
 * @Description : 전문가 추천 서비스
 * @author Firzzle
 * @since 2025. 5. 3.
 */
@Service
@RequiredArgsConstructor
public class ExpertRecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(ExpertRecommendationService.class);

    private final ExpertRecommendationDAO expertRecommendationDAO;

    /**
     * 추천 전문가 목록 조회
     * 콘텐츠와 관련된 전문가 추천 목록을 조회합니다.
     *
     * @param box - 요청 정보가 담긴 RequestBox (userContentSeq 필수)
     * @return List<DataBox> - 추천 전문가 목록
     */
    public List<DataBox> getRecommendedExperts(RequestBox box) {
        logger.debug("추천 전문가 목록 조회 요청 - UserContentSeq: {}", box.getLong("userContentSeq"));

        try {
            // 1. 콘텐츠 관련 추천 전문가 목록 조회
            List<DataBox> recommendedExperts = expertRecommendationDAO.selectRecommendedExperts(box);
            logger.debug("추천 전문가 목록 조회 완료 - 전문가 수: {}", recommendedExperts.size());

            // 2. 각 전문가별 전문 분야(expertise) 조회
            for (DataBox expertDataBox : recommendedExperts) {
                RequestBox expertiseBox = new RequestBox("expertiseBox");
                expertiseBox.put("expertSeq", expertDataBox.getLong2("d_expert_seq"));
                List<DataBox> expertiseDataBoxes = expertRecommendationDAO.selectExpertExpertises(expertiseBox);

                // 전문 분야 목록을 전문가 DataBox에 추가
                expertDataBox.put("expertises", expertiseDataBoxes);
            }

            return recommendedExperts;
        } catch (Exception e) {
            logger.error("추천 전문가 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "추천 전문가 목록 조회 중 오류가 발생했습니다.");
        }
    }
}