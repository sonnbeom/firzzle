package com.firzzle.llm.util;

import java.util.*;
import java.util.stream.Collectors;

public class ScriptUtils {

    // 자막 리스트에서 start~end 사이의 라인만 추출
    public static List<String> extractChunk(List<String> lines, String start, String end) {
        return lines.stream()
            .dropWhile(line -> compareTimestamp(line, start) < 0) // 시작 시간 이후부터
            .takeWhile(line -> compareTimestamp(line, end) < 0)   // 끝 시간 미만까지
            .collect(Collectors.toList());
    }

    // 자막 라인의 타임스탬프와 targetTime을 비교 (초 단위 차이)
    private static int compareTimestamp(String line, String targetTime) {
        String lineTime = extractTimestamp(line);
        return toSeconds(lineTime) - toSeconds(targetTime);
    }

    // 두 타임스탬프 문자열을 초 단위로 비교 (a - b)
    public static int compareTime(String a, String b) {
        return toSeconds(a) - toSeconds(b);
    }

    public static int toSeconds(String time) {
        if (time.contains(":")) {
            String[] parts = time.split(":");
            return Integer.parseInt(parts[0]) * 3600
                 + Integer.parseInt(parts[1]) * 60
                 + Integer.parseInt(parts[2]);
        } else {
            return Integer.parseInt(time); // 초 단위
        }
    }

 // 자막 라인에서 [숫자] 형식의 초단위 타임스탬프 추출
    public static String extractTimestamp(String line) {
        int start = line.indexOf('[') + 1;
        int end = line.indexOf(']');
        if (start >= 0 && end > start) {
            return line.substring(start, end).trim(); // "5" 이런 식의 문자열
        }
        return "0";
    }

    // 자막 텍스트 중 start~end 사이의 타임스탬프 라인만 문자열로 추출
    public static String extractChunkText(List<String> lines, String start, String end) {
        StringBuilder sb = new StringBuilder();
        boolean inRange = false;

        for (String line : lines) {
            String timestamp = ScriptUtils.extractTimestamp(line);
            if (timestamp == null) continue;

            if (!inRange && ScriptUtils.compareTime(timestamp, start) >= 0) {
                inRange = true;
            }

            if (inRange && ScriptUtils.compareTime(timestamp, end) >= 0) {
                break;
            }

            if (inRange) {
                sb.append(line).append("\n");
            }
        }

        return sb.toString().trim();
    }

    // LLM 응답 텍스트에서 JSON 배열만 추출 (```json ... ``` 또는 [ ... ] 형태)
    public static String extractJsonOnly(String raw) {
        if (raw.contains("```")) {
            int start = raw.indexOf("```") + 3;
            int end = raw.lastIndexOf("```");
            String block = raw.substring(start, end).trim();

            if (block.startsWith("json")) {
                block = block.substring(4).trim();
            }

            return block;
        }

        // 일반 JSON 배열 처리
        int start = raw.indexOf("[");
        int end = raw.lastIndexOf("]") + 1;

        if (start == -1 || end <= start) {
            throw new IllegalArgumentException("JSON 배열을 찾을 수 없음");
        }

        return raw.substring(start, end).trim();
    }
}
