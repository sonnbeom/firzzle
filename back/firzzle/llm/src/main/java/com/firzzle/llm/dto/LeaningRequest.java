package com.firzzle.llm.dto;

import lombok.Data;

@Data
public class LeaningRequest {
    private String userId;      // 사용자 고유 ID (예: UUID 또는 email 등)
    private String videoId;     // 학습 대상 유튜브 영상 ID
    private Integer chance;     // 무력 기회 또는 학습 시도 횟수
    private String learningMode;   // 학습 모드 (예: easy, hard, quiz 등)
    private String prompt;

}
