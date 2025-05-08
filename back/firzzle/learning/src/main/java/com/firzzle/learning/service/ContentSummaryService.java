package com.firzzle.learning.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.learning.dao.ContentSummaryDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Class Name : ContentSummaryService.java
 * @Description : 콘텐츠 요약 서비스
 * @author Firzzle
 * @since 2025. 5. 3.
 */
@Service
@RequiredArgsConstructor
public class ContentSummaryService {

    private static final Logger logger = LoggerFactory.getLogger(ContentSummaryService.class);

    private final ContentSummaryDAO summaryDAO;
    private final ObjectMapper objectMapper;

    /**
     * 콘텐츠 요약 정보 조회
     * 콘텐츠의 요약 정보를 특정 난이도로 조회합니다.
     *
     * @param box - 요청 정보가 담긴 RequestBox (userContentSeq 필수)
     * @param level - 요약 난이도 (E: easy, H: high)
     * @return DataBox - 콘텐츠 요약 정보
     */
    public DataBox getSummary(RequestBox box, String level) {
        logger.debug("콘텐츠 요약 정보 조회 요청 - UserContentSeq: {}, Level: {}",
                box.getLong("userContentSeq"), level);

        try {
            // 1. 요약 정보 직접 조회
            RequestBox summaryBox = new RequestBox("summaryBox");
            summaryBox.put("userContentSeq", box.getLong("userContentSeq"));
            summaryBox.put("level", level);
            DataBox summaryDataBox = summaryDAO.selectContentSummary(summaryBox);

            if (summaryDataBox == null) {
                logger.warn("해당 콘텐츠의 {} 난이도 요약 정보가 없습니다. - UserContentSeq: {}", level, box.getLong("userContentSeq"));
                return null;
            }

            // 2. 요약 관련 섹션 목록 조회
            RequestBox sectionBox = new RequestBox("sectionBox");
            sectionBox.put("summarySeq", summaryDataBox.getLong2("d_summary_seq"));
            List<DataBox> sectionDataBoxes = summaryDAO.selectSummarySections(sectionBox);

            // 3. 각 섹션의 details JSON 문자열을 파싱하여 List<String>으로 변환
            for (DataBox sectionDataBox : sectionDataBoxes) {
                try {
                    String detailsJson = sectionDataBox.getString("d_details");
                    List<String> detailsList = objectMapper.readValue(detailsJson, new TypeReference<List<String>>() {});
                    sectionDataBox.put("details_list", detailsList);
                } catch (Exception e) {
                    logger.error("섹션 details JSON 파싱 중 오류: {}", e.getMessage());
                    sectionDataBox.put("details_list", List.of());
                }
            }

            // 4. 요약 정보에 섹션 목록 추가
            summaryDataBox.put("sections", sectionDataBoxes);

            logger.debug("콘텐츠 요약 정보 조회 완료 - UserContentSeq: {}, Level: {}, SummarySeq: {}, 섹션 수: {}",
                    box.getLong("userContentSeq"), level, summaryDataBox.getLong2("d_summary_seq"), sectionDataBoxes.size());

            return summaryDataBox;
        } catch (BusinessException e) {
            logger.error("콘텐츠 요약 정보 조회 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠 요약 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 요약 정보 조회 중 오류가 발생했습니다.");
        }
    }
}