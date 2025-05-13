package com.firzzle.llm.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.llm.domain.ContentBlock;
import com.firzzle.llm.domain.ParsedSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JsonParser {

    private final ObjectMapper mapper = new ObjectMapper();
    
//    public static ParsedSummary parseSummary(String raw) {
//        String easy = "", high = "";
//        Pattern easyPattern = Pattern.compile("\\[쉬운 설명\\](.*?)\\[전문가 설명\\]", Pattern.DOTALL);
//        Pattern highPattern = Pattern.compile("\\[전문가 설명\\](.*)", Pattern.DOTALL);
//
//        Matcher easyMatcher = easyPattern.matcher(raw);
//        Matcher highMatcher = highPattern.matcher(raw);
//
//        if (easyMatcher.find()) {
//            easy = easyMatcher.group(1).trim();
//        }
//        if (highMatcher.find()) {
//            high = highMatcher.group(1).trim();
//        }
//
//        return new ParsedSummary(easy, high);
//    }

    public static List<JsonNode> parseFlexibleJsonChunks(List<String> rawJsonChunks) {
        ObjectMapper mapper = new ObjectMapper();
        List<JsonNode> all = new ArrayList<>();

        for (String chunk : rawJsonChunks) {
            try {
                JsonNode node = mapper.readTree(chunk);
                if (node.isArray()) {
                    for (JsonNode sub : node) {
                        all.add(sub);
                    }
                } else if (node.isObject()) {
                    all.add(node);
                } else {
                    System.out.println("⚠️ JSON 타입이 예상과 다름: " + chunk);
                }
            } catch (Exception e) {
                System.out.println("❌ JSON 파싱 실패: " + e.getMessage());
            }
        }
        return all;
    }
    
    public static List<ContentBlock> parseToContentBlockList(String json) {
        try {
            // GPT 응답에 붙는 ```json ... ``` 제거
            String cleanJson = json
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();
            System.out.print(cleanJson);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(cleanJson, new TypeReference<List<ContentBlock>>() {});
        } catch (Exception e) {
            throw new RuntimeException("❌ 요약 응답 리스트 파싱 실패: " + json, e);
        }
    }
}

