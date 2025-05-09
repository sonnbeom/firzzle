package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class SummaryRequest {
    private String videoId;
    private String metadata;
    private String content; // 나중에 db에서 읽어오는 걸로 변환할 예정
}
