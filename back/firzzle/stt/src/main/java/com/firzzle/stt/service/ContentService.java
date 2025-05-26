package com.firzzle.stt.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.FormatDate;
import com.firzzle.stt.dto.ContentDTO;

import lombok.RequiredArgsConstructor;
import com.firzzle.stt.mapper.ContentMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final ContentMapper contentMapper;
    // private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 콘텐츠 등록
     * YouTube URL로부터 콘텐츠를 등록하고 분석 큐에 추가합니다.
     *
     * @param box - 등록할 콘텐츠 정보 (YouTube URL 포함)
     * @return DataBox - 등록된 콘텐츠 정보
     */
    @Transactional
    public ContentDTO insertContent(ContentDTO content) {
        try {    
            // 2. indate 설정 (yyyyMMddHHmmss)
            try {
                String now = FormatDate.getDate("yyyyMMddHHmmss");
                System.out.println("now: "+now);
                content.setIndate(now);
            } catch (Exception e) {
                logger.warn("날짜 포맷팅 실패, 기본값 사용");
                content.setIndate("");
            }
    
            // 3. processStatus 기본값 설정
            if (content.getProcessStatus() == null) {
                content.setProcessStatus("Q"); // 대기 상태
            }
    
            // 4. insert
            int result = contentMapper.insertContent(content);
            if (result == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 등록 실패");
            }
    
            logger.info("콘텐츠 등록 완료: videoId={}, title={}", content.getVideoId(), content.getTitle());
            return content;
    
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("콘텐츠 등록 중 예외 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 등록 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 주어진 videoId로 콘텐츠가 DB에 존재하는지 확인
     *
     * @param videoId - YouTube 영상 ID
     * @return true: 존재함 / false: 존재하지 않음
     */
    public boolean isContentExistsByVideoId(String videoId) {
        Long existingContentSeq = contentMapper.existsByVideoId(videoId);
        if (existingContentSeq != null) {
            logger.info("이미 등록된 콘텐츠입니다. videoId={}", videoId);
            return true; // 중복 시 조용히 리턴
        }
        return false;
    }
}