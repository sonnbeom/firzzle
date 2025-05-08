package com.firzzle.stt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.firzzle.stt.kafka.producer.SttConvertedProducer;
import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.stt.dto.YoutubeMetaData;
import com.firzzle.stt.executor.WhisperExecutor;
import com.firzzle.stt.util.*;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
    
    @Value("${whisper.working.dir}")
    private String whisperDir;

    private final ContentService contentService; 
    //private final YoutubeAudioService youtubeAudioService;
    

    // 유튜브 영상 기반 스크립트 출력
    public String transcribeFromYoutube(String url) throws Exception {
        String videoId = contentService.extractYoutubeId(url);
        
        // 메타 데이터 및 stt 결과 다운로드
    	ProcessBuilder scriptsExtractor = new ProcessBuilder(
        	    "yt-dlp",
        	    "--write-auto-sub",                      // 자동 자막 다운로드
        	    "--sub-lang", "ko",                      // 한국어 자막
        	    "--sub-format", "vtt",           // (중요) 원래 vtt로 받아야 해
        	    "--convert-subs", "srt",         // (추가) vtt -> srt로 변환
        	    "--skip-download",                       // (✅) 영상 자체는 다운로드 안 함
        	    "--output", videoId + ".%(ext)s",            // 저장 이름 포맷
        	    url
        	);
        
        scriptsExtractor.directory(new File(whisperDir)); // working directory 지정
        scriptsExtractor.redirectErrorStream(true); // 에러 출력도 합치기
        runAndPrint(scriptsExtractor);
        String scripts = printDownloadedFiles(videoId);
        
        
        
        if (scripts != null) {
        	sttConvertedProducer.sendSttResult(scripts); 
        } else {
            throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
        }
        ProcessBuilder metadataExtractor = new ProcessBuilder(
        	    "yt-dlp",
        	    "--skip-download",
        	    "--print", "%(title)s\n%(description)s\n%(categories.0)s\n%(thumbnail)s\n%(duration)s",
        	    "--encoding", "utf-8",  // 인코딩 옵션 추가
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

    	// 최소한 title은 존재해야 함
    	String title = lines.size() > 0 ? lines.get(0) : "";
    	// description은 2번째 줄부터 마지막에서 3줄 전까지 이어붙임
    	StringBuilder descBuilder = new StringBuilder();
    	for (int i = 1; i < lines.size() - 3; i++) {
    	    descBuilder.append(lines.get(i)).append(" ");
    	}
    	String description = descBuilder.toString().trim();

    	// 마지막 3줄을 category, thumbnail, duration으로 처리
    	String category = lines.size() >= 3 ? lines.get(lines.size() - 3) : "";
    	String thumbnail = lines.size() >= 2 ? lines.get(lines.size() - 2) : "";
    	String durationStr = lines.size() >= 1 ? lines.get(lines.size() - 1) : "";

    	// 출력 확인
    	logger.info("🎬 Title: {}", title);
    	logger.info("📝 Description: {}", description);
    	logger.info("📂 Category: {}", category);
    	logger.info("🖼️ Thumbnail URL: {}", thumbnail);
    	logger.info("⏱️ Duration String: '{}'", durationStr);
        
    	return scripts;
    }
    
    public String printDownloadedFiles(String videoId) throws IOException {
        Path workingDir = Paths.get(whisperDir);  // whisperDir은 @Value로 받아온 작업 폴더야
        // srt 자막 파일 읽기
        Path srtPath = workingDir.resolve(videoId + ".ko.srt");
        if (Files.exists(srtPath)) {
        	String srtText = SubtitleUtil.cleanSrtToText(srtPath);
        	return srtText;
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
                logger.info("[process] " + line);
                outputLines.add(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String allOutput = String.join("\n", outputLines);

            // 유튜브 URL이 잘못된 경우
            if (allOutput.contains("ERROR: Unsupported URL") || allOutput.contains("HTTP Error 404")) {
                throw new BusinessException(ErrorCode.INVALID_YOUTUBE_URL);
            }

            // 자막이 없을 경우
            if (allOutput.contains("No subtitles") || allOutput.contains("There are no subtitles")) {
                throw new BusinessException(ErrorCode.SCRIPT_NOT_FOUND);
            }

            // 기타 모든 실패
            throw new RuntimeException("❌ 프로세스 종료 코드: " + exitCode);
        }
    }
    
    public YoutubeMetaData loadMetaData(String videoId) throws IOException {
        Path jsonPath = Paths.get(whisperDir, videoId + ".info.json");
        if (!Files.exists(jsonPath)) {
            throw new FileNotFoundException("❌ 메타데이터 파일이 존재하지 않습니다: " + jsonPath);
        }

        logger.info("✅ 메타데이터 파일 발견: " + jsonPath.toAbsolutePath());

        String json = Files.readString(jsonPath);
        logger.info("✅ 메타데이터 파일 내용 (앞부분):\n" + json.substring(0, Math.min(json.length(), 500)));

        try {
            return YoutubeMetaData.fromJson(json);
        } catch (Exception e) {
            System.err.println("❌ 메타데이터 파싱 오류: " + e.getMessage());
            return null;
        }
    }
}
