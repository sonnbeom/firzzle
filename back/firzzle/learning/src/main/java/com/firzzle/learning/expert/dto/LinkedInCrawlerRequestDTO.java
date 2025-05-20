package com.firzzle.learning.expert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Class Name : LinkedInCrawlerRequestDTO.java
 * @Description : LinkedIn 크롤링 요청 DTO
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LinkedIn 크롤링 요청 DTO")
public class LinkedInCrawlerRequestDTO {

    @Schema(description = "검색 키워드", example = "llm specialist")
    private String keyword;

    @Schema(description = "수집할 프로필 수 (최대 5개 권장)", example = "3")
    private Integer limit;

    @Schema(description = "콘텐츠 일련번호 (선택적)", example = "1234")
    private Long contentSeq;

    @Schema(description = "LinkedIn URL 목록 (직접 지정 시)", example = "[\"https://kr.linkedin.com/in/example1\", \"https://kr.linkedin.com/in/example2\"]")
    private List<String> linkedinUrls;

    // 페이지네이션을 위한 추가 필드
    @Schema(description = "페이지 번호", example = "1")
    private Integer page;

    // 회사 필터링을 위한 필드
    @Schema(description = "회사명 필터", example = "Google")
    private String company;
}