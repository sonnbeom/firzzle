package com.firzzle.stt.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.stt.dto.FrameDTO;
import com.firzzle.stt.dto.ImageRequestDTO;
import com.firzzle.stt.mapper.ContentMapper;
import com.firzzle.stt.mapper.FrameMapper;
import com.firzzle.stt.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class SnapReviewService {

    private static final Logger logger = LoggerFactory.getLogger(SnapReviewService.class);

    @Value("${external.api.url:http://localhost:8085}")
    private String externalUrl;

    @Value("${external.api.key}")
    private String secretKey;

    private final WebClient.Builder webClientBuilder;
    private final ContentMapper contentMapper;
    private final FrameMapper frameMapper;

    /**
     * 외부 이미지 생성 API 호출
     */
    @Async
    public CompletableFuture<Void> generateSnapReview(Long contentSeq, List<String> timeline) {
        try {
            String videoUrl = contentMapper.selectUrlByContentSeq(contentSeq);

            ImageRequestDTO requestDTO = new ImageRequestDTO();
            requestDTO.setUrl(videoUrl);
            requestDTO.setTimelines(timeline);

            return webClientBuilder
                    .baseUrl(externalUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("X-API-KEY", secretKey)
                    .build()
                    .post()
                    .uri("/api/v1/generate-images")
                    .bodyValue(requestDTO)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                    .doOnError(e -> logger.error("❌ SnapReview 외부 API 호출 실패", e))
                    .onErrorMap(e -> new BusinessException(ErrorCode.SNAP_REVIEW_API_CALL_FAILED, "SnapReview 외부 API 요청 실패"))
                    .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.SCRIPT_NOT_FOUND, "스냅리뷰 이미지 응답 없음")))
                    .flatMap(imageUrls -> {
                        if (imageUrls == null || imageUrls.isEmpty()) {
                            return Mono.error(new BusinessException(ErrorCode.SCRIPT_NOT_FOUND, "스냅리뷰 이미지가 비어 있습니다"));
                        }

                        saveFrames(contentSeq, timeline, imageUrls);
                        logger.info("✅ SnapReview 이미지 생성 완료 - contentSeq={}, 개수={}", contentSeq, imageUrls.size());

                        return Mono.empty(); // void 처리
                    })
                    .then()
                    .toFuture(); // CompletableFuture<Void>
        } catch (Exception e) {
            logger.error("❌ SnapReview 비동기 처리 실패 - contentSeq={}", contentSeq, e);
            throw new BusinessException(ErrorCode.SNAP_REVIEW_PROCESSING_FAILED, "SnapReview 생성 중 알 수 없는 오류 발생");
        }
    }

    /**
     * 프레임 데이터 저장 (트랜잭션 처리)
     */
    @Transactional
    protected void saveFrames(Long contentSeq, List<String> timeline, List<String> imageUrls) {
        if (timeline.size() != imageUrls.size()) {
            throw new BusinessException(ErrorCode.SNAP_REVIEW_IMAGE_MISMATCH, "타임라인과 이미지 수가 일치하지 않습니다");
        }

        String now = TimeUtil.getCurrentTimestamp14();

        for (int i = 0; i < imageUrls.size(); i++) {
            String timelineStr = timeline.get(i);
            int seconds;

            try {
                String[] parts = timelineStr.split(":");
                seconds = Integer.parseInt(parts[0]) * 3600 +
                          Integer.parseInt(parts[1]) * 60 +
                          Integer.parseInt(parts[2]);
            } catch (Exception e) {
                logger.warn("⚠️ 잘못된 시간 형식: {} -> 기본값 0으로 대체", timelineStr);
                seconds = 0;
            }

            FrameDTO frame = FrameDTO.builder()
                    .imageUrl(imageUrls.get(i))
                    .timestamp(seconds)
                    .contentSeq(contentSeq)
                    .ldate(now)
                    .indate(now)
                    .build();

            frameMapper.insertFrame(frame);
            logger.info("🖼️ 프레임 저장 완료 - timestamp={}, url={}", seconds, frame.getImageUrl());
        }
    }
}
