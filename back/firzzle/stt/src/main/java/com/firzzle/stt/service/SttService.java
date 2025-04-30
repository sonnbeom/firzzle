package com.firzzle.stt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.firzzle.stt.kafka.producer.SttConvertedProducer;
import com.firzzle.stt.executor.WhisperExecutor;
import com.firzzle.stt.service.YoutubeAudioService;
import com.firzzle.stt.util.*;

import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class SttService {
	
//    @Autowired
//    private SttConvertedProducer sttConvertedProducer;

    @Value("${whisper.working.dir}")
    private String whisperDir;

    private final WhisperExecutor whisperExecutor;
    private final YoutubeAudioService youtubeAudioService;

    public SttService(WhisperExecutor whisperExecutor, YoutubeAudioService youtubeAudioService) {
        this.whisperExecutor = whisperExecutor;
        this.youtubeAudioService = youtubeAudioService;
    }

    // 파일 기반 STT 변환
    public String transcribeFromFile(MultipartFile file) throws Exception {
        String uuid = UUID.randomUUID().toString();
        Path wavPath = FileUtils.saveUploadedFile(file, uuid, whisperDir);
        try {
            return whisperExecutor.run(uuid, wavPath);
        } finally {
            Files.deleteIfExists(wavPath);
        }
    }

    // 유튜브 영상 기반 STT 변환  
    public String transcribeFromYoutube(String url) throws Exception {
        String uuid = UUID.randomUUID().toString();
        
        // 메타 데이터 및 stt 결과 다운로드
        ProcessBuilder ytdlp = new ProcessBuilder(
        	    "yt-dlp",
        	    "--write-auto-sub",                      // 자동 자막 다운로드
        	    "--sub-lang", "ko",                      // 한국어 자막
        	    "--sub-format", "vtt",           // (중요) 원래 vtt로 받아야 해
        	    "--convert-subs", "srt",         // (추가) vtt -> srt로 변환
        	    "--write-info-json",                     // info.json 저장 (메타데이터 저장)
        	    "--skip-download",                       // (✅) 영상 자체는 다운로드 안 함
        	    "--output", uuid + ".%(ext)s",            // 저장 이름 포맷
        	    url
        	);
        ytdlp.directory(new File(whisperDir)); // working directory 지정
        ytdlp.redirectErrorStream(true); // 에러 출력도 합치기
        runAndPrint(ytdlp);
        String scripts = printDownloadedFiles(uuid);
        
        if(scripts != null) {
        	return scripts;
        }else {
            Path wavPath = youtubeAudioService.downloadAndConvertToWav(url, uuid);
            try {
                return whisperExecutor.run(uuid, wavPath);
            } finally {
                Files.deleteIfExists(wavPath);
            }
        }
    }
    
    public String printDownloadedFiles(String uuid) throws IOException {
        Path workingDir = Paths.get(whisperDir);  // whisperDir은 @Value로 받아온 작업 폴더야

        // 1. info.json 파일 읽기
        Path infoJsonPath = workingDir.resolve(uuid + ".info.json");
        if (Files.exists(infoJsonPath)) {
            String infoJson = Files.readString(infoJsonPath, StandardCharsets.UTF_8);
            System.out.println("✅ [info.json] 메타데이터:");
            System.out.println(infoJson);
        } else {
            System.out.println("❗ info.json 파일이 없습니다.");
        }

        // 2. srt 자막 파일 읽기
        Path srtPath = workingDir.resolve(uuid + ".ko.srt");
        if (Files.exists(srtPath)) {
//            String srtText = Files.readString(srtPath, StandardCharsets.UTF_8);
        	String srtText = SubtitleUtil.cleanSrtToText(srtPath);
        	return srtText;
        } else {
            System.out.println("❗ ko.srt 자막 파일이 없습니다.");
            return null;
        }
    }
    
    private void runAndPrint(ProcessBuilder pb) throws IOException, InterruptedException {
        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[process] " + line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("❌ 프로세스 종료 코드: " + exitCode);
        }
    }
}
