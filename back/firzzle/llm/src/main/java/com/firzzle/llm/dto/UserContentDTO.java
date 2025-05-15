package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class UserContentDTO {
    private Long userContentSeq;    // 사용자 콘텐츠 일련 번호
    private Long userSeq;           // 내부 사용자 고유 식별자
    private Long contentSeq;        // 콘텐츠 seq
    private int progress;          // 진행률
    private String lastAccessedAt; // 마지막 접근 시간 (YYYYMMDDHHMMSS)
    private String indate;         // 등록 일시 (YYYYMMDDHHMMSS)
}
