package com.firzzle.stt.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;

public class SubtitleUtil {

    // 시작 타임스탬프만 캡쳐
    private static final Pattern TIME_PATTERN =
        Pattern.compile("^(\\d{2}:\\d{2}:\\d{2},\\d{3})\\s+-->.*$");

    /**
     * SRT 파일 경로를 받아, 중복을 제거한
     * “[hh:mm:ss,SSS] 새로운 텍스트” 꼴의 문자열을 반환
     */
    public static String cleanSrtToText(Path srtPath) throws IOException {
        List<String> lines = Files.readAllLines(srtPath);
        String lastRaw = "";               // 이전 블럭의 원본 텍스트
        List<String> output = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            // 자막 인덱스(숫자)나 빈 줄은 건너뛴다
            if (line.matches("^\\d+$") || line.isEmpty()) continue;

            Matcher m = TIME_PATTERN.matcher(line);
            if (m.find()) {
                String ts = m.group(1);
                // 이 타임스탬프 블럭의 모든 자막 텍스트를 한 줄로 모아둔다
                StringBuilder sb = new StringBuilder();
                for (int j = i + 1; j < lines.size(); j++) {
                    String t = lines.get(j).trim();
                    // 다음 타임스탬프, 숫자 인덱스, 빈줄 만나면 중단
                    if (t.isEmpty() || t.matches("^\\d+$") 
                        || TIME_PATTERN.matcher(t).find()) {
                        break;
                    }
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(t);
                }

                String raw = sb.toString()
                              .replaceAll("\\s+", " ")
                              .trim();
                // 원본(raw) 이 이전 블럭과 완전 동일하면 건너뛰기
                if (!raw.isEmpty() && !raw.equals(lastRaw)) {
                    // 앞 블럭과 겹치는 앞부분만 제거
                    String unique = removeOverlap(lastRaw, raw);
                    if (!unique.isEmpty()) {
                        output.add("[" + ts + "] " + unique);
                        lastRaw = raw;
                    }
                }
            }
        }

        // 한 줄짜리 결과로 합쳐서 리턴
        return String.join("\n", output);
    }

    /**
     * previousText 와 currentText 의 **겹치는 접두사**를 잘라내고
     * 나머지 신규 부분만 반환
     */
    private static String removeOverlap(String previousText, String currentText) {
        if (previousText.isEmpty()) {
            return currentText;
        }
        // 완전 동일하게 접두사로 들어가 있으면 바로 잘라버린다
        if (currentText.startsWith(previousText)) {
            return currentText.substring(previousText.length()).trim();
        }
        // 단어 단위로 최대 overlap 길이 탐색
        String[] prev = previousText.split("\\s+");
        String[] curr = currentText.split("\\s+");
        int max = Math.min(prev.length, curr.length);

        for (int len = max; len > 0; len--) {
            boolean ok = true;
            for (int i = 0; i < len; i++) {
                if (!prev[prev.length - len + i].equals(curr[i])) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                // 처음 len 단어 빼고 나머지를 합쳐서 반환
                StringBuilder sb = new StringBuilder();
                for (int i = len; i < curr.length; i++) {
                    sb.append(curr[i]).append(" ");
                }
                return sb.toString().trim();
            }
        }
        // overlap 하나도 없으면 그대로
        return currentText;
    }
}
