package com.firzzle.learning.expert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @Class Name : LinkedInProfileDTO.java
 * @Description : LinkedIn 프로필 DTO
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LinkedIn 프로필 DTO")
public class LinkedInProfileDTO {

    @Schema(description = "프로필 일련번호")
    private Long profileSeq;

    @Schema(description = "LinkedIn URL", example = "https://kr.linkedin.com/in/johndoe")
    private String linkedinUrl;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "직함", example = "AI 기술 리더")
    private String headline;

    @Schema(description = "회사", example = "ABC 기업")
    private String company;

    @Schema(description = "위치", example = "서울, 대한민국")
    private String location;

    @Schema(description = "요약", example = "10년 경력의 AI 및 머신러닝 전문가")
    private String summary;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;

    @Schema(description = "관련성 점수", example = "0.85")
    private Float relevance;

    @Schema(description = "등록일시", example = "20250518153045")
    private String indate;

    @Schema(description = "수정일시", example = "20250518153045")
    private String ldate;

    @Schema(description = "삭제 여부", example = "N")
    private String deleteYn;

    @Schema(description = "경력 정보 목록")
    private List<LinkedInExperienceDTO> experiences;

    @Schema(description = "학력 정보 목록")
    private List<LinkedInEducationDTO> educations;

    @Schema(description = "스킬 목록")
    private List<LinkedInSkillDTO> skills;

    /**
     * 경력 정보 추가
     */
    public void addExperience(LinkedInExperienceDTO experience) {
        if (this.experiences == null) {
            this.experiences = new ArrayList<>();
        }
        this.experiences.add(experience);
    }

    /**
     * 학력 정보 추가
     */
    public void addEducation(LinkedInEducationDTO education) {
        if (this.educations == null) {
            this.educations = new ArrayList<>();
        }
        this.educations.add(education);
    }

    /**
     * 스킬 정보 추가
     */
    public void addSkill(LinkedInSkillDTO skill) {
        if (this.skills == null) {
            this.skills = new ArrayList<>();
        }
        this.skills.add(skill);
    }

    /**
     * 텍스트 표현 생성
     * (임베딩 생성을 위한 메소드)
     */
    public String toTextRepresentation() {
        StringBuilder sb = new StringBuilder();

        // 기본 정보
        if (name != null) sb.append(name).append(" ");
        if (headline != null) sb.append(headline).append(" ");
        if (company != null) sb.append(company).append(" ");
        if (summary != null) sb.append(summary).append(" ");

        // 스킬 목록
        if (skills != null && !skills.isEmpty()) {
            for (LinkedInSkillDTO skill : skills) {
                if (skill.getSkillName() != null) {
                    sb.append(skill.getSkillName()).append(" ");
                }
            }
        }

        // 경력 정보
        if (experiences != null && !experiences.isEmpty()) {
            for (LinkedInExperienceDTO exp : experiences) {
                if (exp.getTitle() != null) sb.append(exp.getTitle()).append(" ");
                if (exp.getCompany() != null) sb.append(exp.getCompany()).append(" ");
            }
        }

        return sb.toString().trim();
    }
}