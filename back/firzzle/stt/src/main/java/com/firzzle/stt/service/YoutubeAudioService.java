package com.firzzle.stt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.firzzle.stt.domain.YoutubeMetaData;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Slf4j
@Service
public class YoutubeAudioService {

    @Value("${whisper.working.dir}")
    private String whisperDir;

    public Path downloadAndConvertToWav(String youtubeUrl, String uuid) throws IOException, InterruptedException {
        Path mp3Path = Paths.get(whisperDir, uuid + ".mp3");
        Path wavPath = Paths.get(whisperDir, uuid + ".wav");

        // yt-dlp 다운로드
        ProcessBuilder ytdlp = new ProcessBuilder(
            "yt-dlp",
            "-x", "--audio-format", "mp3",
            "--write-auto-sub", "--sub-lang", "ko",
            "--write-info-json",
            "-o", uuid + ".%(ext)s",
            youtubeUrl
        );
        ytdlp.directory(new File(whisperDir));
        ytdlp.redirectErrorStream(true);
        runAndPrint(ytdlp);

        // ffmpeg 변환
        ProcessBuilder ffmpeg = new ProcessBuilder(
            "ffmpeg",
            "-i", mp3Path.getFileName().toString(),
            wavPath.getFileName().toString()
        );
        ffmpeg.directory(new File(whisperDir));
        ffmpeg.redirectErrorStream(true);
        runAndPrint(ffmpeg);

        Files.deleteIfExists(mp3Path); // mp3 삭제
        return wavPath;
    }

    public String processYoutubeData(String uuid) throws IOException, InterruptedException {
        // 메타데이터 읽기
        YoutubeMetaData metaData = loadMetaData(uuid);
        if (metaData == null) {
            log.info("❗ 메타데이터 파싱 실패: null 반환");
        } else {
            log.info("✅ 메타데이터 제목: " + metaData.getTitle());
            log.info("✅ 메타데이터 채널명: " + metaData.getChannel());
        }

        // 자막 파일 경로
        Path subtitlePath = Paths.get(whisperDir, uuid + ".ko.vtt");

        if (Files.exists(subtitlePath)) {
            log.info("✅ 자막 파일 발견: " + subtitlePath.toAbsolutePath());
            String subtitles = Files.readString(subtitlePath, StandardCharsets.UTF_8);
            log.info("✅ 자막 내용 (앞부분):\n" + subtitles.substring(0, Math.min(subtitles.length(), 500)));
            return subtitles;
        } else {
            log.info("❗ 자막 파일 없음, STT 변환 진행");
            Path wavPath = Paths.get(whisperDir, uuid + ".wav");
            if (!Files.exists(wavPath)) {
                throw new FileNotFoundException("❌ WAV 파일이 없습니다: " + wavPath);
            }
            String sttResult = runWhisperSTT(wavPath);
            log.info("✅ STT 결과 (앞부분):\n" + sttResult.substring(0, Math.min(sttResult.length(), 500)));
            return sttResult;
        }
    }

    private void runAndPrint(ProcessBuilder pb) throws IOException, InterruptedException {
        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[process] " + line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("❌ 프로세스 종료 코드: " + exitCode);
        }
    }

    private String runWhisperSTT(Path wavPath) throws IOException, InterruptedException {
        ProcessBuilder whisper = new ProcessBuilder(
            "whisper",
            wavPath.toAbsolutePath().toString(),
            "--model", "small",
            "--language", "ko",
            "--output_format", "txt"
        );
        whisper.directory(new File(whisperDir));
        whisper.redirectErrorStream(true);
        runAndPrint(whisper);

        Path txtPath = Paths.get(whisperDir, wavPath.getFileName().toString().replace(".wav", ".txt"));
        if (!Files.exists(txtPath)) {
            throw new FileNotFoundException("❌ STT 결과 파일이 없습니다: " + txtPath);
        }
        return Files.readString(txtPath, StandardCharsets.UTF_8);
    }

    public YoutubeMetaData loadMetaData(String uuid) throws IOException {
        Path jsonPath = Paths.get(whisperDir, uuid + ".info.json");
        if (!Files.exists(jsonPath)) {
            throw new FileNotFoundException("❌ 메타데이터 파일이 존재하지 않습니다: " + jsonPath);
        }

        log.info("✅ 메타데이터 파일 발견: " + jsonPath.toAbsolutePath());

        String json = Files.readString(jsonPath);
        log.info("✅ 메타데이터 파일 내용 (앞부분):\n" + json.substring(0, Math.min(json.length(), 500)));

        try {
            return YoutubeMetaData.fromJson(json);
        } catch (Exception e) {
            System.err.println("❌ 메타데이터 파싱 오류: " + e.getMessage());
            return null;
        }
    }
}
