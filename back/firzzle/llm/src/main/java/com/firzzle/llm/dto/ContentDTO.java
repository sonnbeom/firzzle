package com.firzzle.llm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * MyBatis 매핑용 DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {
    private Long contentSeq;
    private String videoId;
    private String url;
    private String title;
    private String description;
    private String category;
    private String thumbnailUrl;
    private Long duration;
    private String processStatus;
    private String indate;
    private String completedAt;
    private String deleteYn;
    private String tags; // 태그가 필요하다면
}
