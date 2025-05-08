package com.firzzle.stt.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;

public class SubtitleUtil {

    private static final Pattern TIME_PATTERN =
        Pattern.compile("^(\\d{2}:\\d{2}:\\d{2},\\d{3})\\s+-->.*$");

    private static final int MIN_CHAR_THRESHOLD = 15;       // 최소 문장 길이
    private static final long MIN_TIME_DIFF_MILLIS = 2500;  // 병합 기준 시간(ms)

    /**
     * SRT 파일 경로를 받아 중복 제거 및 병합 후
     * "[초] 텍스트" 형식의 문자열 반환
     */
    public static String cleanSrtToText(Path srtPath) throws IOException {
        List<String> lines = Files.readAllLines(srtPath);
        List<String> output = new ArrayList<>();
        String lastRaw = "";
        String lastTimestamp = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.matches("^\\d+$") || line.isEmpty()) continue;

            Matcher matcher = TIME_PATTERN.matcher(line);
            if (matcher.find()) {
                String timestamp = matcher.group(1);
                StringBuilder sb = new StringBuilder();

                for (int j = i + 1; j < lines.size(); j++) {
                    String t = lines.get(j).trim();
                    if (t.isEmpty() || t.matches("^\\d+$") || TIME_PATTERN.matcher(t).find()) break;
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(t);
                }

                String currentRaw = sb.toString().replaceAll("\\s+", " ").trim();
                if (currentRaw.isEmpty() || currentRaw.equals(lastRaw)) continue;

                String unique = removeOverlap(lastRaw, currentRaw);
                if (unique.isEmpty()) continue;

                boolean shouldMerge = unique.length() < MIN_CHAR_THRESHOLD;
                if (!shouldMerge && lastTimestamp != null) {
                    long diff = parseTimestampToMillis(timestamp) - parseTimestampToMillis(lastTimestamp);
                    shouldMerge = diff < MIN_TIME_DIFF_MILLIS;
                }

                if (shouldMerge && !output.isEmpty()) {
                    int lastIndex = output.size() - 1;
                    output.set(lastIndex, output.get(lastIndex) + " " + unique);
                } else {
                    int seconds = parseTimestampToSeconds(timestamp);
                    output.add("[" + seconds + "] " + unique);
                    lastTimestamp = timestamp;
                }

                lastRaw = currentRaw;
            }
        }

        return String.join("\n", output);
    }

    /** "hh:mm:ss,SSS" 문자열을 {시, 분, 초, 밀리초}로 파싱 */
    private static int[] parseTimeParts(String ts) {
        String[] parts = ts.split("[:,]");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid timestamp format: " + ts);
        }
        return new int[]{
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2]),
            Integer.parseInt(parts[3])
        };
    }

    /** 초 단위 변환 (밀리초 버림) */
    private static int parseTimestampToSeconds(String ts) {
        int[] parts = parseTimeParts(ts);
        return parts[0] * 3600 + parts[1] * 60 + parts[2];
    }

    /** 밀리초 단위 변환 */
    private static long parseTimestampToMillis(String ts) {
        int[] parts = parseTimeParts(ts);
        return parts[0] * 3600000L + parts[1] * 60000L + parts[2] * 1000L + parts[3];
    }

    /** 이전 텍스트와 현재 텍스트의 겹치는 접두사를 제거하고 새로운 부분만 반환 */
    private static String removeOverlap(String prev, String curr) {
        if (prev.isEmpty()) return curr;
        if (curr.startsWith(prev)) return curr.substring(prev.length()).trim();

        String[] prevWords = prev.split("\\s+");
        String[] currWords = curr.split("\\s+");
        int maxOverlap = Math.min(prevWords.length, currWords.length);

        for (int len = maxOverlap; len > 0; len--) {
            boolean match = true;
            for (int i = 0; i < len; i++) {
                if (!prevWords[prevWords.length - len + i].equals(currWords[i])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return String.join(" ", Arrays.copyOfRange(currWords, len, currWords.length)).trim();
            }
        }
        return curr;
    }
}
