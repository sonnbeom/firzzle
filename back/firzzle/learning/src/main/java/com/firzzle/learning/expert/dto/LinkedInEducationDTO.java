package com.firzzle.learning.expert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : LinkedInEducationDTO.java
 * @Description : LinkedIn 학력 정보 DTO
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LinkedIn 학력 정보 DTO")
public class LinkedInEducationDTO {

    @Schema(description = "학력 일련번호")
    private Long educationSeq;

    @Schema(description = "프로필 일련번호")
    private Long profileSeq;

    @Schema(description = "학교", example = "서울대학교")
    private String school;

    @Schema(description = "학위", example = "석사")
    private String degree;

    @Schema(description = "전공", example = "컴퓨터 공학")
    private String fieldOfStudy;

    @Schema(description = "기간", example = "2015년 - 2017년")
    private String duration;

    @Schema(description = "등록일시", example = "20250518153045")
    private String indate;
}