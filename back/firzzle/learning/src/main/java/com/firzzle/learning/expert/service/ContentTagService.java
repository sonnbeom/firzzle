package com.firzzle.learning.expert.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Class Name : ContentTagService.java
 * @Description : 콘텐츠 태그 관련 서비스
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentTagService {

    private final com.firzzle.learning.expert.service.ContentTagDAO contentTagDAO;

    /**
     * 콘텐츠의 태그 목록을 조회합니다.
     *
     * @param contentSeq 콘텐츠 일련번호
     * @return 태그 목록
     */
    public List<String> getContentTags(Long contentSeq) {
        try {
            RequestBox box = new RequestBox("tagBox");
            box.put("contentSeq", contentSeq);

            List<DataBox> tagDataBoxes = contentTagDAO.selectContentTags(box);
            List<String> tags = new ArrayList<>();

            for (DataBox tagBox : tagDataBoxes) {
                String tag = tagBox.getString("d_tag");
                if (tag != null && !tag.isEmpty()) {
                    tags.add(tag);
                }
            }

            log.debug("콘텐츠 {} 태그 조회 완료: {} 개", contentSeq, tags.size());
            return tags;
        } catch (Exception e) {
            log.error("콘텐츠 태그 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 태그 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 콘텐츠의 태그를 쉼표로 구분된 문자열로 조회합니다.
     *
     * @param contentSeq 콘텐츠 일련번호
     * @return 쉼표로 구분된 태그 문자열
     */
    public String getContentTagsAsString(Long contentSeq) {
        List<String> tags = getContentTags(contentSeq);
        return tags.stream().collect(Collectors.joining(", "));
    }

    /**
     * 콘텐츠의 태그를 쉼표로 구분된 문자열로 조회합니다. (최대 개수 제한)
     *
     * @param contentSeq 콘텐츠 일련번호
     * @param maxCount 최대 태그 개수
     * @return 쉼표로 구분된 태그 문자열
     */
    public String getContentTagsAsString(Long contentSeq, int maxCount) {
        List<String> tags = getContentTags(contentSeq);
        return tags.stream().limit(maxCount).collect(Collectors.joining(", "));
    }
}