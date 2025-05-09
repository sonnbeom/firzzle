package com.firzzle.stt.util;

import java.util.*;
import java.util.regex.*;

public class SubtitleMerger {

    public static String mergeSubtitles(String rawText) {
        Pattern pattern = Pattern.compile("\\[(\\d{2}:\\d{2}:\\d{2})\\](.*?)(?=\\[\\d{2}:\\d{2}:\\d{2}\\]|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(rawText);

        List<String[]> parsedLines = new ArrayList<>();
        while (matcher.find()) {
            String time = matcher.group(1).trim();
            String text = matcher.group(2).replace("\n", " ").trim();
            parsedLines.add(new String[]{time, text});
        }

        StringBuilder result = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        String bufferStartTime = null;

        for (String[] line : parsedLines) {
            String time = line[0];
            String text = line[1];

            // 마침표/물음표/느낌표 기준으로 문장 나누기
            String[] sentences = text.split("(?<=[.!?])");
            for (String sentence : sentences) {
                sentence = sentence.trim();
                if (sentence.isEmpty()) continue;

                if (buffer.length() == 0) {
                    bufferStartTime = time;
                }

                buffer.append(" ").append(sentence);

                if (sentence.matches(".*[.!?]$")) {
                    result.append("[").append(bufferStartTime).append("] ")
                          .append(buffer.toString().trim()).append("\n");
                    buffer.setLength(0);
                    bufferStartTime = null;
                }
            }
        }

        if (buffer.length() > 0 && bufferStartTime != null) {
            result.append("[").append(bufferStartTime).append("] ")
                  .append(buffer.toString().trim()).append("\n");
        }

        return result.toString().trim();
    }

    public static void main(String[] args) {
        String input = "[00:00:01] 저는 어차피 일이 안 될 거라고[00:00:01] 생각해요. 애초에 노동은[00:00:02] 징벌이었어요. 2천만 원 주면 할 거[00:00:05] 같잖아요. 할 거 같네. 나도 할 거[00:00:07] 같네.";
        String output = mergeSubtitles(input);
        System.out.println(output);
    }
}
