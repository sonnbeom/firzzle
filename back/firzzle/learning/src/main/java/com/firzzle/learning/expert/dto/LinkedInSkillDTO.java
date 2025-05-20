package com.firzzle.learning.expert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Class Name : LinkedInSkillDTO.java
 * @Description : LinkedIn 스킬 정보 DTO
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LinkedIn 스킬 정보 DTO")
public class LinkedInSkillDTO {

    @Schema(description = "스킬 일련번호")
    private Long skillSeq;

    @Schema(description = "프로필 일련번호")
    private Long profileSeq;

    @Schema(description = "스킬명", example = "Machine Learning")
    private String skillName;

    @Schema(description = "등록일시", example = "20250518153045")
    private String indate;
}