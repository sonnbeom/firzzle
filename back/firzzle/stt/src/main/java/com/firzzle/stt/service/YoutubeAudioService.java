package com.firzzle.stt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

}
