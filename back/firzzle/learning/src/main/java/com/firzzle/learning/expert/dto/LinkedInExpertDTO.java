package com.firzzle.learning.expert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Class Name : LinkedInExpertDTO.java
 * @Description : LinkedIn 전문가 DTO
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LinkedIn 전문가 DTO")
public class LinkedInExpertDTO {

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

    @Schema(description = "경력 정보 목록")
    private List<LinkedInExperienceDTO> experiences;

    @Schema(description = "학력 정보 목록")
    private List<LinkedInEducationDTO> educations;

    @Schema(description = "스킬 목록")
    private List<String> skills;
}