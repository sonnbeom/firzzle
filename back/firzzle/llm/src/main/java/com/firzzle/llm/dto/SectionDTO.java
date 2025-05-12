package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class SectionDTO {
    private long sectionSeq;     // 섹션 일련 번호 (PK)
    private long summarySeq;     // 요약 일련 번호 (FK)
    private String title;       // 섹션 제목
    private int startTime;      // 시작 시간 (초 단위)
    private String details;     // 세부 내용 (TEXT)
}
