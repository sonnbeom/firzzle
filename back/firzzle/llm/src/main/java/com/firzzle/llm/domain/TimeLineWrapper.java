package com.firzzle.llm.domain;

import java.util.List;
import lombok.Data;

/**
 * @ClassName TimeLineWrapper
 * @Description 키워드 및 타임라인 정보를 담는 GPT 응답 DTO
 * @Author Firzzle
 * @Since 2025.05.19
 */
@Data
public class TimeLineWrapper {
    
    /** 자막에서 추출된 주요 키워드 목록 (스크립트 내에 실제 등장하는 단어만 포함) */
    private List<String> keywords;
    
    /** 타임라인 시작 시점 리스트 (각 요약 구간의 시작 시간) */
    private List<TimeLine> timeline;
}
