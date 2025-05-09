package com.firzzle.stt.executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;

@Component
public class WhisperExecutor {

    @Value("${whisper.executable.path}")
    private String whisperExecutable;

    @Value("${whisper.model.path}")
    private String modelPath;

    @Value("${whisper.working.dir}")
    private String whisperDir;

    public String run(String uuid, Path wavPath) throws IOException, InterruptedException {
        File exeFile = new File(whisperExecutable);
        if (!exeFile.exists()) {
            throw new FileNotFoundException("Whisper 실행 파일 없음: " + whisperExecutable);
        }

        ProcessBuilder whisper = new ProcessBuilder(
                whisperExecutable,
                "-m", modelPath,
                "-f", wavPath.getFileName().toString(),
                "-otxt",
                "-osrt", 
                "-of", uuid,
                "--language", "ko",
                "--no-fallback",
                "--max-context", "0",
                "--beam-size", "1",
                "--best-of", "1",
                "--temperature", "0.5",
                "-t", "6"
        );
        whisper.directory(new File(whisperDir));
        whisper.redirectErrorStream(true);
        runAndPrint(whisper);

        Path resultPath = Paths.get(whisperDir, uuid + ".srt");
        if (!Files.exists(resultPath)) {
            throw new IOException("결과 파일이 존재하지 않습니다: " + resultPath);
        }

        long fileSize = Files.size(resultPath);
        if (fileSize == 0 || fileSize < 10) {
            throw new IOException("결과 파일이 비어 있거나 너무 작습니다. Whisper 실행 실패 가능성 있음.");
        }

        String resultText = tryReadText(resultPath);
        Files.deleteIfExists(resultPath);
        return resultText;
    }

    private String tryReadText(Path path) throws IOException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(path), decoder))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // 번호 줄은 무시 (숫자만 있는 줄)
                if (line.matches("\\d+")) {
                    continue;
                }

                // 시간 정보 줄 (00:00:00,000 --> 00:00:04,780 형식)
                if (line.contains("-->")) {
                    String[] times = line.split("-->");
                    if (times.length > 0) {
                        String startTime = times[0].trim().split(",")[0]; // 00:00:00 추출
                        sb.append(startTime).append("\n");
                    }
                    continue;
                }

                // 나머지는 텍스트 줄로 간주
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private void runAndPrint(ProcessBuilder pb) throws IOException, InterruptedException {
        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[whisper] " + line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Whisper 프로세스 종료 코드: " + exitCode);
        }
    }
}