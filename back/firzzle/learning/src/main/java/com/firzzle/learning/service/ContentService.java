package com.firzzle.learning.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.request.PageRequestDTO;
import com.firzzle.learning.dao.ContentDAO;
import com.firzzle.learning.dto.ContentRequestDTO;
import com.firzzle.learning.dto.ContentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Class Name : ContentService.java
 * @Description : 콘텐츠 관리 서비스
 * @author Firzzle
 * @since 2025. 4. 30.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentDAO contentDAO;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic.content-analysis}")
    private String contentAnalysisTopic;

    // YouTube ID 추출 정규식 패턴
    private static final Pattern YOUTUBE_ID_PATTERN =
            Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})");

    /**
     * 콘텐츠 등록
     * YouTube URL로부터 콘텐츠를 등록하고 분석 큐에 추가합니다.
     *
     * @param contentRequestDTO - 등록할 콘텐츠 정보 (YouTube URL 포함)
     * @return ContentResponseDTO - 등록된 콘텐츠 정보
     */
    @Transactional
    public ContentResponseDTO insertContent(ContentRequestDTO contentRequestDTO) {
        log.debug("콘텐츠 등록 요청 - YouTube URL: {}", contentRequestDTO.getYoutubeUrl());

        // 1. YouTube ID 추출
        String videoId = extractYoutubeId(contentRequestDTO.getYoutubeUrl());
        if (videoId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 YouTube URL입니다.");
        }

        // 2. 중복 체크
        int count = contentDAO.selectCountByVideoId(videoId);
        if (count > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 등록된 YouTube 동영상입니다.");
        }

        // 3. ContentResponseDTO 생성
        ContentResponseDTO content = ContentResponseDTO.builder()
                .title(contentRequestDTO.getTitle())
                .description(contentRequestDTO.getDescription())
                .contentType(contentRequestDTO.getCategory())
                .videoId(videoId)
                .url(contentRequestDTO.getYoutubeUrl())
                .thumbnailUrl(generateThumbnailUrl(videoId))
                .duration(0) // 초기값, 분석 후 업데이트 예정
                .processStatus("Q") // 대기중(Queued)
                .tags(contentRequestDTO.getTags())
                .createdAt(LocalDateTime.now())
                .deleteYn("N")
                .build();

        // 4. 콘텐츠 등록
        int result = contentDAO.insertContent(content);
        if (result == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 등록에 실패했습니다.");
        }

        // 5. 태그 처리
        if (StringUtils.hasText(contentRequestDTO.getTags())) {
            List<String> tagList = Arrays.stream(contentRequestDTO.getTags().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());

            if (!tagList.isEmpty()) {
                contentDAO.insertContentTags(content.getContentSeq(), tagList);
            }
        }

        // 6. 분석 작업 큐에 등록
        sendToAnalysisQueue(content.getContentSeq());

        log.info("콘텐츠 등록 완료 - ContentSeq: {}, Title: {}", content.getContentSeq(), content.getTitle());
        return content;
    }

    /**
     * 콘텐츠 정보 조회
     * 특정 콘텐츠의 상세 정보를 조회합니다.
     *
     * @param contentSeq - 조회할 콘텐츠 일련번호
     * @return ContentResponseDTO - 조회된 콘텐츠 정보
     */
    @Transactional(readOnly = true)
    public ContentResponseDTO selectContent(Long contentSeq) {
        log.debug("콘텐츠 정보 조회 요청 - ContentSeq: {}", contentSeq);

        ContentResponseDTO content = contentDAO.selectContent(contentSeq);
        if (content == null) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "요청한 콘텐츠를 찾을 수 없습니다.");
        }

        log.debug("콘텐츠 정보 조회 완료 - ContentSeq: {}, Title: {}", content.getContentSeq(), content.getTitle());
        return content;
    }

//    /**
//     * 콘텐츠 정보 조회
//     * 특정 콘텐츠의 상세 정보를 조회합니다.
//     *
//     * @param box - 조회할 콘텐츠 일련번호
//     * @return ContentResponseDTO - 조회된 콘텐츠 정보
//     */
//    @Transactional(readOnly = true)
//    public DataBox selectContent(RequestBox box) {
////        log.debug("콘텐츠 정보 조회 요청 - ContentSeq: {}", contentSeq);
//
//        DataBox content = contentDAO.selectContentDataBox(box);
//        if (content == null) {
//            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "요청한 콘텐츠를 찾을 수 없습니다.");
//        }
//
////        log.debug("콘텐츠 정보 조회 완료 - ContentSeq: {}, Title: {}", content.getContentSeq(), content.getTitle());
//        return content;
//    }

    /**
     * 콘텐츠 목록 조회
     * 조건에 맞는 콘텐츠 목록을 페이지네이션하여 조회합니다.
     *
     * @param pageRequestDTO - 페이지 요청 정보
     * @return List<ContentResponseDTO> - 조회된 콘텐츠 목록
     */
    @Transactional(readOnly = true)
    public List<ContentResponseDTO> selectContentList(PageRequestDTO pageRequestDTO) {
        log.debug("콘텐츠 목록 조회 요청 - Page: {}, Size: {}",
                pageRequestDTO.getPageNumber(), pageRequestDTO.getPageSize());

        List<ContentResponseDTO> contentList = contentDAO.selectContentList(pageRequestDTO);
        log.debug("콘텐츠 목록 조회 완료 - 조회된 콘텐츠 수: {}", contentList.size());

        return contentList;
    }

    /**
     * 콘텐츠 정보 수정
     * 콘텐츠의 기본 정보를 수정합니다.
     *
     * @param contentSeq - 수정할 콘텐츠 일련번호
     * @param contentRequestDTO - 수정할 콘텐츠 정보
     * @return ContentResponseDTO - 수정된 콘텐츠 정보
     */
    @Transactional
    public ContentResponseDTO updateContent(Long contentSeq, ContentRequestDTO contentRequestDTO) {
        log.debug("콘텐츠 정보 수정 요청 - ContentSeq: {}", contentSeq);

        // 1. 기존 콘텐츠 조회
        ContentResponseDTO existingContent = contentDAO.selectContent(contentSeq);
        if (existingContent == null) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "수정할 콘텐츠를 찾을 수 없습니다.");
        }

        // 2. 콘텐츠 정보 업데이트
        existingContent.setTitle(contentRequestDTO.getTitle());
        existingContent.setDescription(contentRequestDTO.getDescription());
        existingContent.setContentType(contentRequestDTO.getCategory());
        existingContent.setTags(contentRequestDTO.getTags());

        // 3. 콘텐츠 업데이트
        int result = contentDAO.updateContent(existingContent);
        if (result == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 수정에 실패했습니다.");
        }

        // 4. 태그 처리 (기존 태그 삭제 후 새로 등록)
        if (StringUtils.hasText(contentRequestDTO.getTags())) {
            // 기존 태그 삭제는 별도로 구현 필요
            // 실제 구현 시에는 fb_ai_content_tags 테이블에서 해당 content_seq와 연관된 태그를 먼저 삭제해야 함

            List<String> tagList = Arrays.stream(contentRequestDTO.getTags().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());

            if (!tagList.isEmpty()) {
                contentDAO.insertContentTags(contentSeq, tagList);
            }
        }

        log.info("콘텐츠 정보 수정 완료 - ContentSeq: {}, Title: {}", contentSeq, existingContent.getTitle());
        return existingContent;
    }

    /**
     * 콘텐츠 삭제
     * 콘텐츠를 논리적으로 삭제합니다. (delete_yn = 'Y')
     *
     * @param contentSeq - 삭제할 콘텐츠 일련번호
     * @return boolean - 삭제 성공 여부
     */
    @Transactional
    public boolean deleteContent(Long contentSeq) {
        log.debug("콘텐츠 삭제 요청 - ContentSeq: {}", contentSeq);

        // 1. 기존 콘텐츠 조회
        ContentResponseDTO existingContent = contentDAO.selectContent(contentSeq);
        if (existingContent == null) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "삭제할 콘텐츠를 찾을 수 없습니다.");
        }

        // 2. 콘텐츠 삭제 (논리적 삭제)
        int result = contentDAO.deleteContent(contentSeq);

        log.info("콘텐츠 삭제 완료 - ContentSeq: {}, 결과: {}", contentSeq, result > 0);
        return result > 0;
    }

    /**
     * 콘텐츠 분석 상태 업데이트
     * 콘텐츠의 분석 상태 및 관련 데이터를 업데이트합니다.
     *
     * @param contentSeq - 업데이트할 콘텐츠 일련번호
     * @param status - 업데이트할 분석 상태 (Q: 대기중, P: 분석중, C: 완료, F: 실패)
     * @param analysisData - 분석 결과 데이터 (JSON 형식)
     * @return ContentResponseDTO - 업데이트된 콘텐츠 정보
     */
    @Transactional
    public ContentResponseDTO updateAnalysisStatus(Long contentSeq, String status, String analysisData) {
        log.debug("콘텐츠 분석 상태 업데이트 요청 - ContentSeq: {}, Status: {}", contentSeq, status);

        // 1. 기존 콘텐츠 조회
        ContentResponseDTO existingContent = contentDAO.selectContent(contentSeq);
        if (existingContent == null) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "업데이트할 콘텐츠를 찾을 수 없습니다.");
        }

        // 2. 상태 검증
        if (!isValidStatus(status)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 분석 상태 코드입니다.");
        }

        // 3. 상태 업데이트
        int result = contentDAO.updateAnalysisStatus(contentSeq, status, analysisData);
        if (result == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "콘텐츠 분석 상태 업데이트에 실패했습니다.");
        }

        // 4. 업데이트된 콘텐츠 정보 조회
        ContentResponseDTO updatedContent = contentDAO.selectContent(contentSeq);

        log.info("콘텐츠 분석 상태 업데이트 완료 - ContentSeq: {}, Status: {}", contentSeq, status);
        return updatedContent;
    }

    /**
     * 태그별 콘텐츠 목록 조회
     * 특정 태그를 가진 콘텐츠 목록을 페이지네이션하여 조회합니다.
     *
     * @param tag - 조회할 태그
     * @param pageRequestDTO - 페이지 요청 정보
     * @return List<ContentResponseDTO> - 조회된 콘텐츠 목록
     */
    @Transactional(readOnly = true)
    public List<ContentResponseDTO> selectContentListByTag(String tag, PageRequestDTO pageRequestDTO) {
        log.debug("태그별 콘텐츠 목록 조회 요청 - Tag: {}, Page: {}, Size: {}",
                tag, pageRequestDTO.getPageNumber(), pageRequestDTO.getPageSize());

        List<ContentResponseDTO> contentList = contentDAO.selectContentListByTag(tag, pageRequestDTO);
        log.debug("태그별 콘텐츠 목록 조회 완료 - Tag: {}, 조회된 콘텐츠 수: {}", tag, contentList.size());

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
     * 콘텐츠 분석 큐에 등록
     *
     * @param contentSeq - 분석할 콘텐츠 일련번호
     */
    private void sendToAnalysisQueue(Long contentSeq) {
        try {
            // 분석 요청 메시지 생성 (JSON 형식)
            String message = String.format("{\"contentSeq\":%d,\"timestamp\":\"%s\"}",
                    contentSeq, LocalDateTime.now().toString());

            // Kafka 메시지 발송
            kafkaTemplate.send(contentAnalysisTopic, message);
            log.debug("콘텐츠 분석 큐에 등록 완료 - ContentSeq: {}", contentSeq);
        } catch (Exception e) {
            log.error("콘텐츠 분석 큐 등록 중 오류 발생: {}", e.getMessage(), e);
            // 큐 등록 실패는 비즈니스 로직에 영향을 주지 않도록 예외를 던지지 않고 로그만 남김
        }
    }
}