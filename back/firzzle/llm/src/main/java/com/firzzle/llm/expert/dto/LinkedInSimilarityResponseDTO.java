package com.firzzle.llm.expert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 유사 LinkedIn 프로필 검색 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "유사 LinkedIn 프로필 검색 결과 DTO")
public class LinkedInSimilarityResponseDTO {

    @Schema(description = "콘텐츠 일련번호")
    private Long contentSeq;

    @Schema(description = "콘텐츠 태그")
    private String tags;

    @Schema(description = "유사 프로필 목록")
    private List<LinkedInProfileSimilarityDTO> profiles;

    @Schema(description = "현재 페이지")
    private Integer page;

    @Schema(description = "페이지 크기")
    private Integer pageSize;

    @Schema(description = "전체 항목 수")
    private Integer totalElements;

    @Schema(description = "전체 페이지 수")
    private Integer totalPages;

    @Schema(description = "마지막 페이지 여부")
    private Boolean last;

    @Schema(description = "다음 페이지 존재 여부")
    private Boolean hasNext;
}