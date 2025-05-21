package com.firzzle.llm.expert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * LinkedIn 프로필 유사도 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LinkedIn 프로필 유사도 DTO")
public class LinkedInProfileSimilarityDTO {

    @Schema(description = "프로필 일련번호")
    private Long profileSeq;

    @Schema(description = "LinkedIn URL")
    private String linkedinUrl;

    @Schema(description = "이름")
    private String name;

    @Schema(description = "직함")
    private String headline;

    @Schema(description = "회사")
    private String company;

    @Schema(description = "위치")
    private String location;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;

    @Schema(description = "유사도 점수")
    private Float similarity;
}