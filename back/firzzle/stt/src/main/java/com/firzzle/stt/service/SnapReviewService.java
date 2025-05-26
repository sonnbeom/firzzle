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
import org.springframework.util.FileSystemUtils;
import com.firzzle.stt.util.S3Uploader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * SnapReviewService는 콘텐츠 영상에서 특정 시점의 스냅 이미지를 생성하여 저장하는 서비스입니다.
 * 내부 처리(yt-dlp + ffmpeg) 또는 외부 API 호출을 통해 이미지 생성을 수행합니다.
 */
@Service
@RequiredArgsConstructor
public class SnapReviewService {

    private static final Logger logger = LoggerFactory.getLogger(SnapReviewService.class);
    private static final boolean DEV_MODE = true; // ✅ 개발 모드 시 내부 처리 사용

    @Value("${external.api.url}")
    private String externalUrl;

    @Value("${external.api.key}")
    private String secretKey;

    private final WebClient.Builder webClientBuilder;
    private final ContentMapper contentMapper;
    private final FrameMapper frameMapper;
    private final S3Uploader s3Uploader;

    /**
     * SnapReview 생성 진입점 - 내부 또는 외부 방식 선택
     */
    @Async
    public CompletableFuture<Void> generateSnapReview(Long contentSeq, List<String> timeline) {
        if (DEV_MODE) {
            return generateWithInternalProcess(contentSeq, timeline); // 내부 ffmpeg 처리
        } else {
            return generateWithExternalApi(contentSeq, timeline); // 외부 API 호출 처리
        }
    }

    /**
     * 내부 처리 방식으로 SnapReview 이미지 생성 및 저장
     */
    @Async
    private CompletableFuture<Void> generateWithInternalProcess(Long contentSeq, List<String> timeline) {
        try {
            String videoUrl = contentMapper.selectUrlByContentSeq(contentSeq);

            ImageRequestDTO requestDTO = new ImageRequestDTO();
            requestDTO.setUrl(videoUrl);
            requestDTO.setTimelines(timeline);

            // 비동기 이미지 처리 및 S3 업로드
            List<String> imageUrls = processVideoAndUploadImages(requestDTO).get();

            // 프레임 정보 DB 저장
            saveFrames(contentSeq, timeline, imageUrls);
            logger.info("✅ SnapReview 내부 처리 완료 - contentSeq={}, 개수={}", contentSeq, imageUrls.size());

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("❌ SnapReview 내부 처리 실패 - contentSeq={}", contentSeq, e);
            throw new BusinessException(ErrorCode.SNAP_REVIEW_PROCESSING_FAILED, "SnapReview 내부 처리 중 오류 발생");
        }
    }

    /**
     * 외부 API를 통해 SnapReview 이미지 생성 요청
     */
    @Async
    public CompletableFuture<Void> generateWithExternalApi(Long contentSeq, List<String> timeline) {
        try {
            String videoUrl = contentMapper.selectUrlByContentSeq(contentSeq);

            ImageRequestDTO requestDTO = new ImageRequestDTO();
            requestDTO.setUrl(videoUrl);
            requestDTO.setTimelines(timeline);

            // 외부 API 호출 및 결과 처리
            return webClientBuilder
                    .baseUrl(externalUrl.trim())
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

                        return Mono.empty();
                    })
                    .then()
                    .toFuture();
        } catch (Exception e) {
            logger.error("❌ SnapReview 비동기 처리 실패 - contentSeq={}", contentSeq, e);
            throw new BusinessException(ErrorCode.SNAP_REVIEW_PROCESSING_FAILED, "SnapReview 생성 중 알 수 없는 오류 발생");
        }
    }

    /**
     * 타임라인 및 이미지 URL을 기반으로 DB에 프레임 데이터 저장
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
                // hh:mm:ss → 초로 변환
                String[] parts = timelineStr.split(":");
                seconds = Integer.parseInt(parts[0]) * 3600 +
                          Integer.parseInt(parts[1]) * 60 +
                          Integer.parseInt(parts[2]);
            } catch (Exception e) {
                logger.warn("⚠️ 잘못된 시간 형식: {} -> 기본값 0으로 대체", timelineStr);
                seconds = 0;
            }

            // FrameDTO 생성 및 DB 저장
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

    /**
     * yt-dlp, ffmpeg를 사용해 영상으로부터 프레임 이미지 추출 후 S3 업로드
     */
    @Async
    public CompletableFuture<List<String>> processVideoAndUploadImages(ImageRequestDTO request) {
        String tempDir = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID();
        File tempFolder = new File(tempDir);
        tempFolder.mkdirs();

        try {
            // yt-dlp로 영상 스트리밍 URL 추출
            ProcessBuilder urlPb = new ProcessBuilder(
                "yt-dlp", "-f", "best[height<=480][ext=mp4]/best[height<=480]", "-g", request.getUrl()
            );
            urlPb.redirectErrorStream(true);
            Process urlProc = urlPb.start();

            String streamUrl;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(urlProc.getInputStream()))) {
                streamUrl = br.readLine();
            }

            int exitCode = urlProc.waitFor();
            if (exitCode != 0 || streamUrl == null || streamUrl.isBlank()) {
                throw new RuntimeException("스트림 URL 추출 실패 (exit=" + exitCode + ")");
            }

            List<String> imageUrls = new ArrayList<>();
            for (int i = 0; i < request.getTimelines().size(); i++) {
                String time = request.getTimelines().get(i);
                String imagePath = tempDir + "/image_" + i + ".jpg";

                // ffmpeg로 특정 시간의 프레임 추출
                ProcessBuilder ffmpegPb = new ProcessBuilder(
                    "ffmpeg", "-ss", time, "-i", streamUrl,
                    "-vframes", "1", "-vf", "scale=854:480", "-q:v", "3", imagePath
                );
                ffmpegPb.redirectErrorStream(true);
                Process ffmpegProc = ffmpegPb.start();

                try (BufferedReader err = new BufferedReader(new InputStreamReader(ffmpegProc.getInputStream()))) {
                    while (err.readLine() != null) {}
                }

                if (ffmpegProc.waitFor() != 0) {
                    throw new RuntimeException("ffmpeg 실행 실패 (time=" + time + ")");
                }

                File imgFile = new File(imagePath);
                if (!imgFile.exists()) {
                    throw new RuntimeException("이미지 파일이 생성되지 않음: " + imagePath);
                }

                // S3 업로드
                String url = s3Uploader.upload(imgFile, "images/");
                imageUrls.add(url);
            }

            return CompletableFuture.completedFuture(imageUrls);

        } catch (Exception e) {
            logger.error("영상 처리 중 오류 발생", e);
            throw new RuntimeException("영상 처리 실패", e);
        } finally {
            // 임시 디렉토리 정리
            FileSystemUtils.deleteRecursively(tempFolder);
        }
    }
}
