package com.firzzle.llm.expert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * LinkedIn 프로필 임베딩 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LinkedIn 프로필 임베딩 요청 DTO")
public class LinkedInEmbeddingRequestDTO {

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

    @Schema(description = "요약")
    private String summary;

    @Schema(description = "스킬 목록")
    private List<String> skills;
}