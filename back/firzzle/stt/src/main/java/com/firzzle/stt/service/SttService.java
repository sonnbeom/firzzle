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

    @Autowired
    private SttConvertedProducer sttConvertedProducer;

    private static final Logger logger = LoggerFactory.getLogger(SttService.class);

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir; // 파일 업로드 디렉토리

    private final ContentService contentService; 

    /**
     * 유튜브 URL을 기반으로 자막을 추출하고, 메타데이터를 파싱하여 DB에 저장하고,
     * 자막 텍스트를 Kafka로 전송한다.
     *
     * @param url 유튜브 영상 URL
     * @return 스크립트 텍스트
     */
    public String transcribeFromYoutube(String url) throws Exception {
        String videoId = contentService.extractYoutubeId(url);

//        // 중복 콘텐츠 방지
        if (contentService.isContentExistsByVideoId(videoId)) 
            return null;

     // (1) 자막 다운로드
        String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";

        // (1) 자막 다운로드
        ProcessBuilder scriptsExtractor = new ProcessBuilder(
          "yt-dlp",
          "--user-agent", UA,
          "--no-check-certificate",
          "--referer", "https://www.youtube.com",
          "--write-auto-sub",
          "--sub-lang", "ko",
          "--sub-format", "vtt",
          "--convert-subs", "srt",
          "--skip-download",
          "--output", videoId + ".%(ext)s",
          url
        );
        scriptsExtractor.directory(new File(uploadDir));
        scriptsExtractor.redirectErrorStream(true);
        runAndPrint(scriptsExtractor);

        // (2) 자막 파일 읽기
        String scripts = printDownloadedFiles(videoId);

        // (3) 메타데이터 추출
        ProcessBuilder metadataExtractor = new ProcessBuilder(
            "yt-dlp",
            "--user-agent", UA,
            "--no-check-certificate",
            "--referer", "https://www.youtube.com",
            "--skip-download",
            "--print", "%(title)s\n%(description)s\n%(categories.0)s\n%(thumbnail)s\n%(duration)s",
            "--encoding", "utf-8",
            url
        );
        metadataExtractor.redirectErrorStream(true);


        Process process = metadataExtractor.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

        // (4) 메타데이터 파싱
        String title = lines.size() > 0 ? lines.get(0) : "";

        StringBuilder descBuilder = new StringBuilder();
        for (int i = 1; i < lines.size() - 3; i++) {
            descBuilder.append(lines.get(i)).append(" ");
        }
        String description = descBuilder.toString().trim();

        String category = lines.size() >= 3 ? lines.get(lines.size() - 3) : "";
        String thumbnail = lines.size() >= 2 ? lines.get(lines.size() - 2) : "";
        String durationStr = lines.size() >= 1 ? lines.get(lines.size() - 1) : "";

        // (5) ContentDTO 객체 생성 및 저장
        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setVideoId(videoId);
        contentDTO.setUrl(url);
        contentDTO.setTitle(title);
        contentDTO.setDescription(description);
        contentDTO.setCategory(category);
        contentDTO.setThumbnailUrl(thumbnail);
        contentDTO.setDuration(Long.parseLong(durationStr));

        contentService.insertContent(contentDTO);
        Long contentSeq = contentDTO.getContentSeq();
        if (scripts != null) {
            sttConvertedProducer.sendSttResult(contentSeq, scripts); // Kafka 전송
        } else {
            throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
        }

        return scripts;
    }

    /**
     * 자막 파일(.srt) 읽어서 텍스트로 변환
     *
     * @param videoId 유튜브 영상 ID
     * @return 변환된 스크립트 텍스트
     */
    public String printDownloadedFiles(String videoId) throws IOException {
        Path workingDir = Paths.get(uploadDir);
        Path srtPath = workingDir.resolve(videoId + ".ko.srt");

        if (Files.exists(srtPath)) {
            // 텍스트 추출
            String result = SubtitleUtil.cleanSrtToText(srtPath);
            // 파일 삭제
            try {
                Files.deleteIfExists(srtPath);
                logger.info("✅ 자막 파일 삭제 완료: " + srtPath);
            } catch (IOException e) {
                logger.warn("⚠️ 자막 파일 삭제 실패: " + srtPath, e);
            }
            return result;
        } else {
            logger.info("❗ ko.srt 자막 파일이 없습니다.");
            return null;
        }
    }

    /**
     * ProcessBuilder로 실행한 외부 명령어의 출력 결과를 로깅하고 예외 처리
     *
     * @param pb 실행할 프로세스 빌더
     * @throws Exception 프로세스 실행 오류
     */
    private void runAndPrint(ProcessBuilder pb) throws Exception {
    Process process = pb.start();
    List<String> outputLines = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
            logger.info("[process] " + line);
            outputLines.add(line);
        }
    }

    int exitCode = process.waitFor();
    String allOutput = String.join("\n", outputLines);
    logger.error("📌 yt-dlp 전체 로그:\n{}", allOutput); // ✅ 로그 전체 출력

    if (exitCode != 0) {
        if (allOutput.contains("ERROR: Unsupported URL") || allOutput.contains("HTTP Error 404")) {
            throw new BusinessException(ErrorCode.INVALID_YOUTUBE_URL);
        }

        if (allOutput.contains("No subtitles") || allOutput.contains("There are no subtitles")) {
            throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
        }

        throw new RuntimeException("❌ 프로세스 종료 코드: " + exitCode);
    }
}
}

