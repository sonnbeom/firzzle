package com.firzzle.stt.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.stt.dto.ContentDTO;
import com.firzzle.stt.kafka.producer.SttConvertedProducer;
import com.firzzle.stt.util.SubtitleUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SttService {

    private static final Logger logger = LoggerFactory.getLogger(SttService.class);

    private static final boolean DEV_MODE = true; // true: 외부 서버에 요청하여 결과를 받아옴, false: 현재 환경에서 직접 처리

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    @Value("${external.api.url:http://localhost:8085}")
    private String externalUrl;
    
    @Value("${external.api.key}")
    private String secretKey; 

    private final WebClient.Builder webClientBuilder;
    private final ContentService contentService;
    private final SttConvertedProducer sttConvertedProducer;

    public String transcribeFromYoutube(String url) throws Exception {
        String videoId = contentService.extractYoutubeId(url);

        if (contentService.isContentExistsByVideoId(videoId)) return null;

        return DEV_MODE ? extractSubtitleViaLocalProxy(url, videoId) : extractSubtitleDirect(url, videoId);
    }

    public String extractSubtitleViaLocalProxy(String url, String videoId) throws Exception {
        WebClient webClient = webClientBuilder
            .baseUrl(externalUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("X-API-KEY", secretKey) // ✅ 키를 헤더로 추가
            .build();

        Map<String, String> requestBody = Map.of(
                "url", url,
                "videoId", videoId
            );

        Map<String, Object> response = webClient.post()
        	    .uri("/api/v1/extract")
        	    .bodyValue(requestBody)
        	    .retrieve()
        	    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
        	    .block();

        if (response == null || !response.containsKey("script")) {
            throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
        }

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

        return processFinalResult(contentDTO, (String) response.get("script"));
    }

    public String extractSubtitleDirect(String url, String videoId) throws Exception {
        runAndPrint(new ProcessBuilder(
            "yt-dlp", "--no-check-certificate", "--referer", "https://www.youtube.com",
            "--write-auto-sub", "--sub-lang", "ko", "--sub-format", "vtt", "--convert-subs", "srt",
            "--skip-download", "--output", videoId + ".%(ext)s", url
        ).directory(new File(uploadDir)).redirectErrorStream(true));

        String scripts = printDownloadedFiles(videoId);

        ProcessBuilder metadataExtractor = new ProcessBuilder(
            "yt-dlp", "--no-check-certificate", "--referer", "https://www.youtube.com",
            "--skip-download", "--print", "%(title)s\n%(description)s\n%(categories.0)s\n%(thumbnail)s\n%(duration)s",
            "--encoding", "utf-8", url
        ).redirectErrorStream(true);

        Process process = metadataExtractor.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

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

        return processFinalResult(contentDTO, scripts);
    }

    private String processFinalResult(ContentDTO contentDTO, String script) {
        contentService.insertContent(contentDTO);
        if (script != null) {
            sttConvertedProducer.sendSttResult(contentDTO.getContentSeq(), script);
        } else {
            throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
        }
        return script;
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
        List<String> outputLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("[process] {}", line);
                outputLines.add(line);
            }
        }

        int exitCode = process.waitFor();
        String allOutput = String.join("\n", outputLines);
        logger.error("📌 yt-dlp 전체 로그:\n{}", allOutput);

        if (exitCode != 0) {
            if (allOutput.contains("ERROR: Unsupported URL") || allOutput.contains("HTTP Error 404")) {
                throw new BusinessException(ErrorCode.INVALID_YOUTUBE_URL);
            } else if (allOutput.contains("No subtitles") || allOutput.contains("There are no subtitles")) {
                throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
            } else {
                throw new RuntimeException("❌ 프로세스 종료 코드: " + exitCode);
            }
        }
    }
}
