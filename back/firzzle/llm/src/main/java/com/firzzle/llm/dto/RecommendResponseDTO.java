package com.firzzle.llm.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @Class Name : RecommendationsResponseDTO.java
 * @Description : Qdrant 벡터 검색 응답 DTO (추천된 유사 콘텐츠 정보 포함)
 * @author Firzzle
 * @since 2025. 5. 15.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "유사한 영상 추천 결과 DTO")
public class RecommendResponseDTO {
	 @Schema(
		        description = "추천된 콘텐츠 목록. 각 Map은 추천 콘텐츠의 필드(contentSeq, title 등)를 포함하며, score, matchedTags 등 부가 정보가 포함될 수 있음",
		        example = "[{\"contentSeq\": 1, \"title\": \"AI 강의\", \"score\": 0.94}]"
		    )
	 private List<RecommendContentDTO> content;

    @Schema(description = "추천 대상 키워드 (originTags)", example = "딥러닝, 신경망")
    private String originTags;

    @Schema(description = "현재 페이지 번호", example = "1")
    private int p_pageno;

    @Schema(description = "페이지당 항목 수", example = "10")
    private int p_pagesize;

    @Schema(description = "전체 콘텐츠 개수", example = "46")
    private int totalElements;

    @Schema(description = "전체 페이지 수", example = "5")
    private int totalPages;

    @Schema(description = "마지막 페이지 여부", example = "true")
    private boolean last;

    @Schema(description = "다음 페이지 존재 여부", example = "false")
    private boolean hasNext;
}
