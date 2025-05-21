package com.firzzle.stt.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.StringManager;
import com.firzzle.stt.dto.ContentDTO;
import com.firzzle.stt.dto.LlmRequest;
import com.firzzle.stt.dto.UserContentDTO;
import com.firzzle.stt.kafka.producer.SttConvertedProducer;
import com.firzzle.stt.mapper.UserContentMapper;
import com.firzzle.stt.mapper.UserMapper;
import com.firzzle.stt.util.SubtitleUtil;
import com.firzzle.stt.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import io.netty.resolver.DefaultAddressResolverGroup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class SttService {

    private static final Logger logger = LoggerFactory.getLogger(SttService.class);
    private static final boolean DEV_MODE = true;

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    @Value("${external.api.url:http://localhost:8085}")
    private String externalUrl;

    @Value("${external.api.key}")
    private String secretKey;

    private final WebClient.Builder webClientBuilder;
    private final ContentService contentService;
    private final SttConvertedProducer sttConvertedProducer;
    private final UserContentMapper userContentMapper;
    private final UserMapper userMapper;

    @Async
    public CompletableFuture<LlmRequest> transcribeFromYoutube(String uuid, String url, String taskId) {
        logger.debug("[STT] transcribeFromYoutube called: uuid={}, url={}, taskId={}", uuid, url, taskId);
        return CompletableFuture.supplyAsync(() -> StringManager.extractYoutubeId(url))
                .thenCompose(videoId -> DEV_MODE
                        ? extractSubtitleViaLocalProxy(uuid, url, videoId, taskId, false, null)
                        : extractSubtitleDirect(uuid, url, videoId, taskId, false, null))
                .exceptionally(e -> {
                    logger.error("[STT] transcribeFromYoutube pipeline error", e);
                    throw new BusinessException(ErrorCode.STT_PROCESS_FAILED, "STT 처리 중 오류 발생", e);
                });
    }

    @Async
    public CompletableFuture<LlmRequest> extractSubtitleViaLocalProxy(String uuid, String url, String videoId, String taskId, boolean isError, Exception originalException) {
        // 타입 변환 문제 해결을 위한 수정
        return CompletableFuture.supplyAsync(() -> {
            try {
                Long userSeq = userMapper.selectUserSeqByUuid(uuid);
                HttpClient httpClient = HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE);

                WebClient webClient = WebClient.builder()
                        .clientConnector(new ReactorClientHttpConnector(httpClient))
                        .baseUrl(externalUrl)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .defaultHeader("X-API-KEY", secretKey)
                        .build();

                Map<String, String> requestBody = Map.of("url", url, "videoId", videoId);

                return webClient.post()
                        .uri("/api/v1/extract")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .doOnError(ex -> logger.error("[STT] external extract API error", ex))
                        .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.SCRIPT_NOT_FOUND)))
                        .toFuture()
                        .thenApply(response -> {
                            if (!response.containsKey("script"))
                                throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);

                            ContentDTO contentDTO = mapToContentDTO(videoId, url, response);
                            return processFinalResult(userSeq, contentDTO, (String) response.get("script"), taskId, false, null);
                        }).join(); // CompletableFuture를 블로킹하여 결과를 반환
            } catch (Exception ex) {
                return processFinalResult(null, null, null, taskId, true, new BusinessException(ErrorCode.SUBTITLE_EXTRACTION_FAILED, "자막 추출 중 오류 발생", ex));
            }
        });
    }

    @Async
    public CompletableFuture<LlmRequest> extractSubtitleDirect(String uuid, String url, String videoId, String taskId, boolean isError, Exception originalException) {
        // 타입 변환 문제 해결을 위한 수정
        return CompletableFuture.supplyAsync(() -> {
            try {
                Long userSeq = userMapper.selectUserSeqByUuid(uuid);
                runAndPrint(new ProcessBuilder(
                        "yt-dlp", "--no-check-certificate", "--referer", "https://www.youtube.com",
                        "--write-auto-sub", "--sub-lang", "ko", "--sub-format", "vtt", "--convert-subs", "srt",
                        "--skip-download", "--output", videoId + ".%(ext)s", url)
                        .directory(new File(uploadDir)).redirectErrorStream(true));

                String scripts = printDownloadedFiles(videoId);
                if (scripts == null) throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);

                ProcessBuilder metadataExtractor = new ProcessBuilder(
                        "yt-dlp", "--no-check-certificate", "--referer", "https://www.youtube.com",
                        "--skip-download", "--print",
                        "%(title)s\n%(description)s\n%(categories.0)s\n%(thumbnail)s\n%(duration)s",
                        "--encoding", "utf-8", url)
                        .redirectErrorStream(true);

                List<String> lines = readProcessOutput(metadataExtractor.start());
                ContentDTO contentDTO = parseMetadata(videoId, url, lines);
                return processFinalResult(userSeq, contentDTO, scripts, taskId, false, null);
            } catch (BusinessException ex) {
                return processFinalResult(null, null, null, taskId, true, ex);
            } catch (Exception ex) {
                return processFinalResult(null, null, null, taskId, true, new BusinessException(ErrorCode.YTDLP_EXECUTION_FAILED, "yt-dlp 처리 중 오류 발생", ex));
            }
        });
    }

    @Transactional
    public LlmRequest processFinalResult(Long userSeq, ContentDTO contentDTO, String script, String taskId, boolean isError, Exception e) {
        if(isError) {
            LlmRequest req = new LlmRequest(null, null, null, taskId, true, e);
            sttConvertedProducer.sendSttResult(req);
            return req;
        }

        try {
            contentService.insertContent(contentDTO);

            LlmRequest req;
            // ✅ 여기서 userContentSeq가 자동으로 채워짐
            Long userContentSeq = saveUserContent(userSeq, contentDTO.getContentSeq());
            if (script != null && !script.isBlank()) {
                req = new LlmRequest(userContentSeq, contentDTO.getContentSeq(), script, taskId, false, e);
                sttConvertedProducer.sendSttResult(req);
            } else {
                throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
            }
            return req;
        } catch (BusinessException ex) {
            logger.error("[STT] Content 저장 중 오류 발생", ex);
            LlmRequest req = new LlmRequest(null, null, null, taskId, true, ex);
            sttConvertedProducer.sendSttResult(req);
            throw ex;
        } catch (Exception ex) {
            logger.error("[STT] Content 저장 중 오류 발생", ex);
            BusinessException be = new BusinessException(ErrorCode.SUBTITLE_EXTRACTION_FAILED, "자막 추출에 실패했습니다.");
            LlmRequest req = new LlmRequest(null, null, null, taskId, true, be);
            sttConvertedProducer.sendSttResult(req);
            throw be;
        }
    }

    private Long saveUserContent(Long userSeq, Long contentSeq) {
        UserContentDTO userContentDTO = new UserContentDTO();
        userContentDTO.setUserSeq(userSeq);
        userContentDTO.setContentSeq(contentSeq);
        userContentDTO.setLastAccessedAt(TimeUtil.getCurrentTimestamp14());
        userContentDTO.setIndate(TimeUtil.getCurrentTimestamp14());
        userContentMapper.insertUserContent(userContentDTO);
        return userContentDTO.getUserContentSeq();
    }

    public String printDownloadedFiles(String videoId) throws IOException {
        Path srtPath = Paths.get(uploadDir).resolve(videoId + ".ko.srt");
        if (Files.exists(srtPath)) {
            String result = SubtitleUtil.cleanSrtToText(srtPath);
            Files.deleteIfExists(srtPath);
            return result;
        }
        return null;
    }

    private void runAndPrint(ProcessBuilder pb) throws Exception {
        Process process = pb.start();
        List<String> outputLines = readProcessOutput(process);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String output = String.join("\n", outputLines);
            if (output.contains("Unsupported URL") || output.contains("HTTP Error 404")) {
                throw new BusinessException(ErrorCode.INVALID_YOUTUBE_URL);
            } else if (output.contains("No subtitles") || output.contains("There are no subtitles")) {
                throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
            } else {
                throw new BusinessException(ErrorCode.YTDLP_EXECUTION_FAILED, "yt-dlp 종료 코드: " + exitCode);
            }
        }
    }

    private List<String> readProcessOutput(Process process) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private ContentDTO mapToContentDTO(String videoId, String url, Map<String, Object> response) {
        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setVideoId(videoId);
        contentDTO.setUrl(url);
        contentDTO.setTitle((String) response.getOrDefault("title", ""));
        contentDTO.setDescription((String) response.getOrDefault("description", ""));
        contentDTO.setCategory((String) response.getOrDefault("category", ""));
        contentDTO.setThumbnailUrl((String) response.getOrDefault("thumbnail", ""));

        try {
            Object durationObj = response.get("duration");
            if (durationObj instanceof Number) {
                contentDTO.setDuration(((Number) durationObj).longValue());
            } else if (durationObj instanceof String) {
                contentDTO.setDuration(Long.parseLong((String) durationObj));
            }
        } catch (Exception e) {
            logger.warn("⚠️ duration 파싱 실패: {}", response.get("duration"));
            contentDTO.setDuration(0L);
        }
        return contentDTO;
    }

    private ContentDTO parseMetadata(String videoId, String url, List<String> lines) {
        String title = lines.size() > 0 ? lines.get(0) : "";
        StringBuilder descBuilder = new StringBuilder();
        for (int i = 1; i < lines.size() - 3; i++) descBuilder.append(lines.get(i)).append(" ");

        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setVideoId(videoId);
        contentDTO.setUrl(url);
        contentDTO.setTitle(title);
        contentDTO.setDescription(descBuilder.toString().trim());
        contentDTO.setCategory(lines.size() >= 3 ? lines.get(lines.size() - 3) : "");
        contentDTO.setThumbnailUrl(lines.size() >= 2 ? lines.get(lines.size() - 2) : "");
        try {
            contentDTO.setDuration(Long.parseLong(lines.get(lines.size() - 1)));
        } catch (NumberFormatException e) {
            contentDTO.setDuration(0L);
        }
        return contentDTO;
    }
}