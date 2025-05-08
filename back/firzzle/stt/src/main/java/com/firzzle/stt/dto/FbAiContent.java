package com.firzzle.stt.dto;

import lombok.Data;

@Data
public class FbAiContent {
    private Integer contentSeq;        // 콘텐츠 일련 번호 (PK, auto_increment)
    private String videoId;            // 유튜브 video id
    private String url;                // 콘텐츠 URL (NOT NULL)
    private String title;              // 콘텐츠 제목 (NOT NULL)
    private String description;        // 콘텐츠 설명
    private String category;           // 콘텐츠 카테고리
    private String thumbnailUrl;       // 썸네일 URL
    private Integer duration;          // 영상 길이(초) (NOT NULL)
    private String processStatus;      // 처리 상태 (Q, P, C, F)
    private String indate;             // 등록일시 (YYYYMMDDHHMMSS) (NOT NULL)
    private String completedAt;        // 처리 완료 일시 (YYYYMMDDHHMMSS)
    private String deleteYn;           // 삭제 여부 (Y/N)
}
