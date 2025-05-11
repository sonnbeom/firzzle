package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class SummaryDTO {
	private long summarySeq;     // 요약 일련 번호 (PK)
    private long contentSeq;     // 콘텐츠 seq (FK)
    private String level;       // 난이도: 'E' (easy), 'H' (high)
    private String indate;      // 생성 시간 (형식: YYYYMMDDHHMMSS)
}
