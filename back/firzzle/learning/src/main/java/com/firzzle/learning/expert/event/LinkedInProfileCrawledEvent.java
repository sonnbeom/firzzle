package com.firzzle.learning.expert.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Class Name : LinkedInProfileCrawledEvent.java
 * @Description : LinkedIn 프로필 크롤링 완료 이벤트 (Kafka)
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedInProfileCrawledEvent {

    /**
     * 프로필 일련번호
     */
    private Long profileSeq;

    /**
     * 일괄 처리를 위한 프로필 일련번호 목록
     */
    private List<Long> profileSeqs;

    /**
     * 검색 키워드
     */
    private String keyword;

    /**
     * 프로필 개수
     */
    private Integer count;

    /**
     * 프로필 URL
     */
    private String linkedinUrl;

    /**
     * 프로필 이름
     */
    private String name;

    /**
     * 직함
     */
    private String headline;

    /**
     * 회사
     */
    private String company;

    /**
     * 프로필 요약
     */
    private String summary;

    /**
     * 스킬 목록
     */
    private List<String> skills;

    /**
     * 이벤트 타임스탬프 (YYYYMMDDHHMMSS)
     */
    private String timestamp;

    /**
     * 이벤트 타입
     */
    private String eventType = "PROFILE_CRAWLED";
}