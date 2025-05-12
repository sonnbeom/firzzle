package com.firzzle.stt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.firzzle.stt.kafka.producer.SttConvertedProducer;
import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.stt.dto.ContentDTO;
import com.firzzle.stt.util.*;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SttService {

    private static final Logger logger = LoggerFactory.getLogger(SttService.class);

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    private final ContentService contentService;
    @Autowired
    private SttConvertedProducer sttConvertedProducer;

    public String transcribeFromYoutube(String url) throws Exception {
        String videoId = contentService.extractYoutubeId(url);

        if (contentService.isContentExistsByVideoId(videoId)) {
            return null;
        }

        // 1) 자막 다운로드
        ProcessBuilder scriptsPb = new ProcessBuilder(
            "yt-dlp",
            "--write-auto-sub",
            "--sub-lang", "ko",
            "--sub-format", "vtt",
            "--convert-subs", "srt",
            "--skip-download",
            "--output", videoId + ".%(ext)s",
            url
        );
        scriptsPb.directory(new File(uploadDir));
        scriptsPb.redirectErrorStream(true);
        runAndPrint(scriptsPb, videoId);

        // 2) srt 읽고 임시 파일들 삭제
        String scripts = printAndCleanFiles(videoId);

        // 3) 메타데이터 추출 (생략: 기존 코드 그대로)

        // ... ContentDTO 생성/저장, Kafka 전송 로직 ...

        return scripts;
    }

    private void runAndPrint(ProcessBuilder pb, String videoId) throws Exception {
        Process p = pb.start();
        List<String> out = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                logger.info("[yt-dlp] {}", line);
                out.add(line);
            }
        }
        int code = p.waitFor();
        if (code != 0) {
            String all = String.join("\n", out);
            logger.error("❌ yt-dlp failed (exit {}):\n{}", code, all);

            if (all.contains("Unsupported URL") || all.contains("404")) {
                throw new BusinessException(ErrorCode.INVALID_YOUTUBE_URL);
            }
            if (all.contains("No subtitles") || all.contains("There are no subtitles")) {
                throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
            }
            throw new RuntimeException("yt-dlp unexpected exit code: " + code);
        }
    }

    /**
     * videoId.ko.srt 읽어서 텍스트로 리턴하고, 
     * .ko.vtt/.ko.srt/.info.json/.mp4 등 관련 파일 전부 삭제
     */
    public String printAndCleanFiles(String videoId) throws IOException {
        Path dir = Paths.get(uploadDir);
        Path srt = dir.resolve(videoId + ".ko.srt");
        if (!Files.exists(srt)) {
            logger.info("자막 파일이 없습니다: {}", srt);
            return null;
        }

        // 1) 텍스트 추출
        String text = SubtitleUtil.cleanSrtToText(srt);

        // 2) 관련 파일 일괄 삭제
        String[] exts = { ".ko.srt", ".ko.vtt", ".info.json", ".mp4" };
        for (String ext : exts) {
            Path p = dir.resolve(videoId + ext);
            try {
                if (Files.deleteIfExists(p)) {
                    logger.info("삭제 완료: {}", p);
                }
            } catch (IOException e) {
                logger.warn("삭제 실패: {}", p, e);
            }
        }

        return text;
    }
}


