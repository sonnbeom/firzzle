package com.firzzle.llm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* @Class Name : RecommendRequestDTO.java
* @Description : 영상 추천 요청
* @author Firzzle
* @since 2025. 5. 19.
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "추천 콘텐츠 정보 + 유사도 정보 DTO")
public class RecommendRequestDTO {
    private int p_pageno = 1;       // ✅ 기본값 1
    private int p_pagesize = 6;     // ✅ 기본값 6
}
