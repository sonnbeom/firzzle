package com.firzzle.llm.expert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 유사 LinkedIn 프로필 검색 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "유사 LinkedIn 프로필 검색 요청 DTO")
public class LinkedInSimilarityRequestDTO {

    @Schema(description = "콘텐츠 일련번호", example = "12345")
    private Long contentSeq;

    @Schema(description = "콘텐츠 태그 (쉼표로 구분)", example = "AI,머신러닝,데이터 사이언스")
    private String tags;

    @Schema(description = "검색할 프로필 수", example = "9")
    private Integer limit;

    @Schema(description = "최소 유사도 점수 (0-1)", example = "0.3")
    private Float minScore;

    @Schema(description = "페이지 번호", example = "1")
    private Integer page;

    @Schema(description = "페이지 크기", example = "3")
    private Integer pageSize;
}