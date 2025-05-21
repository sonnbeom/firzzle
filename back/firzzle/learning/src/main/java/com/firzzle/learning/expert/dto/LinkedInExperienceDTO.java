package com.firzzle.learning.expert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : LinkedInExperienceDTO.java
 * @Description : LinkedIn 경력 정보 DTO
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LinkedIn 경력 정보 DTO")
public class LinkedInExperienceDTO {

    @Schema(description = "경력 일련번호")
    private Long experienceSeq;

    @Schema(description = "프로필 일련번호")
    private Long profileSeq;

    @Schema(description = "직책", example = "시니어 개발자")
    private String title;

    @Schema(description = "회사", example = "ABC 기업")
    private String company;

    @Schema(description = "기간", example = "2020년 1월 - 현재")
    private String duration;

    @Schema(description = "설명", example = "AI 기반 추천 시스템 개발")
    private String description;

    @Schema(description = "등록일시", example = "20250518153045")
    private String indate;
}