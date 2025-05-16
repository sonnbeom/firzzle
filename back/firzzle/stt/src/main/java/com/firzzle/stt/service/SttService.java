package com.firzzle.stt.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Async;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.stt.dto.ContentDTO;
import com.firzzle.stt.dto.LlmRequest;
import com.firzzle.stt.dto.UserContentDTO;
import com.firzzle.stt.kafka.producer.SttConvertedProducer;
import com.firzzle.stt.mapper.UserContentMapper;
import com.firzzle.stt.mapper.UserMapper;
import com.firzzle.stt.util.SubtitleUtil;
import com.firzzle.stt.util.TimeUtil;

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
    public CompletableFuture<LlmRequest> transcribeFromYoutube(String uuid, String url) {
        return CompletableFuture.supplyAsync(() -> contentService.extractYoutubeId(url))
                .thenCompose(videoId -> DEV_MODE
                        ? extractSubtitleViaLocalProxy(uuid, url, videoId)
                        : extractSubtitleDirect(uuid, url, videoId));
    }

    @Async
    public CompletableFuture<LlmRequest> extractSubtitleViaLocalProxy(String uuid, String url, String videoId) {
        return CompletableFuture.supplyAsync(() -> userMapper.selectUserSeqByUuid(uuid))
                .thenCompose(userSeq -> {
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
                            .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.SCRIPT_NOT_FOUND)))
                            .toFuture()
                            .thenApply(response -> {
                                if (!response.containsKey("script"))
                                    throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);

                                ContentDTO contentDTO = mapToContentDTO(videoId, url, response);
                                return processFinalResult(userSeq, contentDTO, (String) response.get("script"));
                            });
                });
    }

    @Async
    public CompletableFuture<LlmRequest> extractSubtitleDirect(String uuid, String url, String videoId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Long userSeq = userMapper.selectUserSeqByUuid(uuid);
                runAndPrint(new ProcessBuilder("yt-dlp", "--no-check-certificate", "--referer", "https://www.youtube.com",
                        "--write-auto-sub", "--sub-lang", "ko", "--sub-format", "vtt", "--convert-subs", "srt",
                        "--skip-download", "--output", videoId + ".%(ext)s", url)
                        .directory(new File(uploadDir)).redirectErrorStream(true));

                String scripts = printDownloadedFiles(videoId);
                if (scripts == null) {
                    throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
                }

                ProcessBuilder metadataExtractor = new ProcessBuilder("yt-dlp", "--no-check-certificate", "--referer",
                        "https://www.youtube.com", "--skip-download", "--print",
                        "%(title)s\n%(description)s\n%(categories.0)s\n%(thumbnail)s\n%(duration)s", "--encoding",
                        "utf-8", url).redirectErrorStream(true);

                List<String> lines = readProcessOutput(metadataExtractor.start());
                ContentDTO contentDTO = parseMetadata(videoId, url, lines);
                return processFinalResult(userSeq, contentDTO, scripts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Async
    @Transactional
    public LlmRequest processFinalResult(Long userSeq, ContentDTO contentDTO, String script) {
        contentService.insertContent(contentDTO);
        saveUserContent(userSeq, contentDTO.getContentSeq());

        if (script != null) {
            sttConvertedProducer.sendSttResult(contentDTO.getContentSeq(), script);
        } else {
            throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
        }
        return new LlmRequest(contentDTO.getContentSeq(), script);
    }

    
    private void saveUserContent(Long userSeq, Long contentSeq) {
        UserContentDTO userContentDTO = new UserContentDTO();
        userContentDTO.setUserSeq(userSeq);
        userContentDTO.setContentSeq(contentSeq);
        userContentDTO.setLastAccessedAt(TimeUtil.getCurrentTimestamp14());
        userContentDTO.setIndate(TimeUtil.getCurrentTimestamp14());
        userContentMapper.insertUserContent(userContentDTO);
    }

    public String printDownloadedFiles(String videoId) throws IOException {
        Path srtPath = Paths.get(uploadDir).resolve(videoId + ".ko.srt");
        if (Files.exists(srtPath)) {
            String result = SubtitleUtil.cleanSrtToText(srtPath);
            try {
                Files.deleteIfExists(srtPath);
                logger.info("✅ 자막 파일 삭제 완료: {}", srtPath);
            } catch (IOException e) {
                logger.warn("⚠️ 자막 파일 삭제 실패: {}", srtPath, e);
            }
            return result;
        } else {
            logger.info("❗ ko.srt 자막 파일이 없습니다.");
            return null;
        }
    }

    private void runAndPrint(ProcessBuilder pb) throws Exception {
        Process process = pb.start();
        List<String> outputLines = readProcessOutput(process);
        logger.error("📌 yt-dlp 전체 로그:\n{}", String.join("\n", outputLines));

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String output = String.join("\n", outputLines);
            if (output.contains("Unsupported URL") || output.contains("HTTP Error 404")) {
                throw new BusinessException(ErrorCode.INVALID_YOUTUBE_URL);
            } else if (output.contains("No subtitles") || output.contains("There are no subtitles")) {
                throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
            } else {
                throw new RuntimeException("❌ 프로세스 종료 코드: " + exitCode);
            }
        }
    }

    private List<String> readProcessOutput(Process process) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("[process] {}", line);
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

        Object durationObj = response.get("duration");
        if (durationObj instanceof Number) {
            contentDTO.setDuration(((Number) durationObj).longValue());
        } else if (durationObj instanceof String) {
            contentDTO.setDuration(Long.parseLong((String) durationObj));
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
        contentDTO.setDuration(Long.parseLong(lines.size() >= 1 ? lines.get(lines.size() - 1) : "0"));
        return contentDTO;
    }
}
