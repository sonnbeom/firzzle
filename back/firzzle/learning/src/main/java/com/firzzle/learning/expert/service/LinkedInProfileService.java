package com.firzzle.learning.expert.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.utils.DateUtil;
import com.firzzle.learning.expert.dao.LinkedInProfileDAO;
import com.firzzle.learning.expert.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Class Name : LinkedInProfileService.java
 * @Description : LinkedIn 프로필 저장 및 조회 서비스
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Service
public class LinkedInProfileService {

    private final Logger logger = LoggerFactory.getLogger(LinkedInProfileService.class);

    private final LinkedInProfileDAO linkedInProfileDAO;

    @Autowired
    public LinkedInProfileService(LinkedInProfileDAO linkedInProfileDAO) {
        this.linkedInProfileDAO = linkedInProfileDAO;
    }

    /**
     * LinkedInProfileDAO getter
     */
    public LinkedInProfileDAO getLinkedInProfileDAO() {
        return linkedInProfileDAO;
    }

    /**
     * RequestBox에 안전하게 값을 넣기 위한 메서드
     * null 값은 빈 문자열로 대체
     */
    private void safelyPut(RequestBox box, String key, Object value) {
        if (value != null) {
            box.put(key, value);
        } else {
            if (key.equals("relevance")) {
                box.put(key, Float.valueOf(0));
            } else if (key.contains("Seq")) {
                box.put(key, 0L);
            } else {
                box.put(key, "");
            }
        }
    }

    /**
     * LinkedIn 프로필 목록을 저장합니다.
     *
     * @param profiles LinkedIn 프로필 목록
     * @return 저장된 프로필 일련번호 목록
     */
    public List<Long> saveLinkedInProfiles(List<LinkedInProfileDTO> profiles) {
        List<Long> savedProfileSeqs = new ArrayList<>();

        try {
            for (LinkedInProfileDTO profile : profiles) {
                // 1. 기존 프로필 확인
                RequestBox checkBox = new RequestBox("checkBox");
                safelyPut(checkBox, "linkedinUrl", profile.getLinkedinUrl());
                DataBox existingProfile = linkedInProfileDAO.selectProfileByLinkedInUrl(checkBox);

                Long profileSeq;

                if (existingProfile != null) {
                    // 기존 프로필 있는 경우
                    profileSeq = existingProfile.getLong2("d_profile_seq");
                    logger.info("기존 LinkedIn 프로필 사용: {}, 일련번호: {}",
                            profile.getName() != null ? profile.getName() : "", profileSeq);
                } else {
                    // 2. 프로필 기본 정보 저장
                    String currentDatetime = DateUtil.getCurrentDatetime();

                    RequestBox profileBox = new RequestBox("profileBox");
                    safelyPut(profileBox, "linkedinUrl", profile.getLinkedinUrl());
                    safelyPut(profileBox, "name", profile.getName());
                    safelyPut(profileBox, "headline", profile.getHeadline());
                    safelyPut(profileBox, "company", profile.getCompany());
                    safelyPut(profileBox, "location", profile.getLocation());
                    safelyPut(profileBox, "summary", profile.getSummary());
                    safelyPut(profileBox, "profileImageUrl", profile.getProfileImageUrl());
                    safelyPut(profileBox, "relevance", profile.getRelevance());
                    safelyPut(profileBox, "indate", currentDatetime);
                    safelyPut(profileBox, "ldate", currentDatetime);

                    linkedInProfileDAO.insertLinkedInProfile(profileBox);
                    profileSeq = profileBox.getLong("profileSeq");
                    logger.info("LinkedIn 프로필 저장 완료: {}, 일련번호: {}",
                            profile.getName() != null ? profile.getName() : "", profileSeq);

                    // 3. 경력 정보 저장
                    if (profile.getExperiences() != null) {
                        for (LinkedInExperienceDTO exp : profile.getExperiences()) {
                            RequestBox expBox = new RequestBox("expBox");
                            safelyPut(expBox, "profileSeq", profileSeq);
                            safelyPut(expBox, "title", exp.getTitle());
                            safelyPut(expBox, "company", exp.getCompany());
                            safelyPut(expBox, "duration", exp.getDuration());
                            safelyPut(expBox, "description", exp.getDescription());
                            safelyPut(expBox, "indate", exp.getIndate() != null ? exp.getIndate() : currentDatetime);

                            linkedInProfileDAO.insertLinkedInExperience(expBox);
                        }
                        logger.debug("경력 정보 {} 건 저장 완료", profile.getExperiences().size());
                    }

                    // 4. 학력 정보 저장
                    if (profile.getEducations() != null) {
                        for (LinkedInEducationDTO edu : profile.getEducations()) {
                            RequestBox eduBox = new RequestBox("eduBox");
                            safelyPut(eduBox, "profileSeq", profileSeq);
                            safelyPut(eduBox, "school", edu.getSchool());
                            safelyPut(eduBox, "degree", edu.getDegree());
                            safelyPut(eduBox, "fieldOfStudy", edu.getFieldOfStudy());
                            safelyPut(eduBox, "duration", edu.getDuration());
                            safelyPut(eduBox, "indate", edu.getIndate() != null ? edu.getIndate() : currentDatetime);

                            linkedInProfileDAO.insertLinkedInEducation(eduBox);
                        }
                        logger.debug("학력 정보 {} 건 저장 완료", profile.getEducations().size());
                    }

                    // 5. 스킬 정보 저장
                    if (profile.getSkills() != null) {
                        for (LinkedInSkillDTO skill : profile.getSkills()) {
                            RequestBox skillBox = new RequestBox("skillBox");
                            safelyPut(skillBox, "profileSeq", profileSeq);
                            safelyPut(skillBox, "skillName", skill.getSkillName());
                            safelyPut(skillBox, "indate", skill.getIndate() != null ? skill.getIndate() : currentDatetime);

                            linkedInProfileDAO.insertLinkedInSkill(skillBox);
                        }
                        logger.debug("스킬 정보 {} 건 저장 완료", profile.getSkills().size());
                    }
                }

                savedProfileSeqs.add(profileSeq);
            }

            return savedProfileSeqs;
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 저장 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * LinkedIn 프로필 목록을 조회합니다. (페이지네이션)
     *
     * @param searchDTO 검색 및 페이지 정보
     * @return LinkedIn 프로필 응답 DTO (페이지네이션 포함)
     */
    public LinkedInCrawlerResponseDTO getLinkedInProfiles(LinkedInCrawlerRequestDTO searchDTO) {
        try {
            RequestBox box = new RequestBox("searchBox");

            // 페이지 정보 설정
            int page = searchDTO.getPage() != null ? searchDTO.getPage() : 1;
            int pageSize = searchDTO.getLimit() != null ? searchDTO.getLimit() : 10;

            safelyPut(box, "p_pageno", page);
            safelyPut(box, "p_pagesize", pageSize);

            // 검색 조건 설정
            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                safelyPut(box, "keyword", searchDTO.getKeyword());
            }

            if (searchDTO.getCompany() != null && !searchDTO.getCompany().isEmpty()) {
                safelyPut(box, "company", searchDTO.getCompany());
            }

            // 시작 인덱스 계산
            int start = (page - 1) * pageSize;
            safelyPut(box, "p_startNumber", start);
            safelyPut(box, "p_limitNumber", pageSize);

            // 정렬 설정
            safelyPut(box, "p_order", "d_relevance");
            safelyPut(box, "p_sortorder", "DESC");

            // 1. 프로필 목록 조회
            List<DataBox> profileDataBoxes;
            int totalCount;

            if (box.getString("keyword") != null && !box.getString("keyword").isEmpty()) {
                // 키워드 검색
                profileDataBoxes = linkedInProfileDAO.searchLinkedInProfiles(box);
                totalCount = linkedInProfileDAO.searchLinkedInProfilesCount(box);
            } else {
                // 일반 조회
                profileDataBoxes = linkedInProfileDAO.selectLinkedInProfiles(box);
                totalCount = linkedInProfileDAO.selectLinkedInProfilesCount(box);
            }

            // 2. DTO 변환
            List<LinkedInProfileDTO> profileDTOs = convertToProfileDTOs(profileDataBoxes);

            // 3. 응답 생성
            return LinkedInCrawlerResponseDTO.paginationBuilder()
                    .message("LinkedIn 프로필 조회가 완료되었습니다.")
                    .keyword(searchDTO.getKeyword())
                    .profileCount(profileDTOs.size())
                    .profiles(profileDTOs)
                    .crawledAt(DateUtil.getCurrentDatetime())
                    .page(page)
                    .pageSize(pageSize)
                    .totalElements(totalCount)
                    .build();

        } catch (Exception e) {
            logger.error("LinkedIn 프로필 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 조회 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * LinkedIn 프로필을 일련번호로 조회합니다.
     *
     * @param profileSeq 프로필 일련번호
     * @return LinkedIn 프로필 DTO
     */
    public LinkedInProfileDTO getLinkedInProfileBySeq(Long profileSeq) {
        try {
            // DataBox를 RequestBox로 포장
            RequestBox box = new RequestBox("profileBox");
            safelyPut(box, "profileSeq", profileSeq);

            // 기본 프로필 정보 조회
            List<DataBox> profileDataBoxes = linkedInProfileDAO.selectLinkedInProfiles(box);

            if (profileDataBoxes == null || profileDataBoxes.isEmpty()) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "해당 일련번호의 LinkedIn 프로필을 찾을 수 없습니다: " + profileSeq);
            }

            // 프로필 DTO 변환 및 관련 데이터 조회
            List<LinkedInProfileDTO> profileDTOs = convertToProfileDTOs(profileDataBoxes);

            return profileDTOs.get(0);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 조회 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * LinkedIn 프로필을 URL로 조회합니다.
     *
     * @param linkedinUrl LinkedIn URL
     * @return LinkedIn 프로필 DTO
     */
    public LinkedInProfileDTO getLinkedInProfileByUrl(String linkedinUrl) {
        try {
            RequestBox box = new RequestBox("urlBox");
            safelyPut(box, "linkedinUrl", linkedinUrl);

            // 프로필 기본 정보 조회
            DataBox profileDataBox = linkedInProfileDAO.selectProfileByLinkedInUrl(box);

            if (profileDataBox == null) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "해당 URL의 LinkedIn 프로필을 찾을 수 없습니다: " + linkedinUrl);
            }

            // 프로필 DTO 생성
            LinkedInProfileDTO profileDTO = createProfileDTOFromDataBox(profileDataBox);

            // 연관 데이터 조회 (경력, 학력, 스킬)
            addRelatedDataToProfileDTO(profileDTO);

            return profileDTO;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 조회 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * LinkedIn 프로필을 삭제합니다. (논리적 삭제)
     *
     * @param profileSeq 프로필 일련번호
     * @return 삭제 여부
     */
    @Transactional
    public boolean deleteLinkedInProfile(Long profileSeq) {
        try {
            RequestBox box = new RequestBox("deleteBox");
            safelyPut(box, "profileSeq", profileSeq);
            safelyPut(box, "ldate", DateUtil.getCurrentDatetime());

            int affected = linkedInProfileDAO.updateLinkedInProfileDeleteYn(box);

            return affected > 0;

        } catch (Exception e) {
            logger.error("LinkedIn 프로필 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * DataBox 목록을 LinkedInProfileDTO 목록으로 변환합니다.
     */
    private List<LinkedInProfileDTO> convertToProfileDTOs(List<DataBox> profileDataBoxes) {
        List<LinkedInProfileDTO> profileDTOs = new ArrayList<>();

        if (profileDataBoxes == null) {
            return profileDTOs;
        }

        for (DataBox dataBox : profileDataBoxes) {
            if (dataBox != null) {
                LinkedInProfileDTO profileDTO = createProfileDTOFromDataBox(dataBox);

                // 연관 데이터 조회 (경력, 학력, 스킬)
                addRelatedDataToProfileDTO(profileDTO);

                profileDTOs.add(profileDTO);
            }
        }

        return profileDTOs;
    }

    /**
     * DataBox에서 LinkedInProfileDTO 생성
     */
    private LinkedInProfileDTO createProfileDTOFromDataBox(DataBox dataBox) {
        try {
            if (dataBox == null) {
                return new LinkedInProfileDTO();
            }

            return LinkedInProfileDTO.builder()
                    .profileSeq(dataBox.getLong2("d_profile_seq"))
                    .linkedinUrl(dataBox.getString("d_linkedin_url"))
                    .name(dataBox.getString("d_name"))
                    .headline(dataBox.getString("d_headline"))
                    .company(dataBox.getString("d_company"))
                    .location(dataBox.getString("d_location"))
                    .summary(dataBox.getString("d_summary"))
                    .profileImageUrl(dataBox.getString("d_profile_image_url"))
                    .relevance(dataBox.getFloat("d_relevance"))
                    .indate(dataBox.getString("d_indate"))
                    .ldate(dataBox.getString("d_ldate"))
                    .deleteYn(dataBox.getString("d_delete_yn"))
                    .experiences(new ArrayList<>())
                    .educations(new ArrayList<>())
                    .skills(new ArrayList<>())
                    .build();
        } catch (Exception e) {
            logger.error("DataBox에서 ProfileDTO 변환 중 오류 발생: {}", e.getMessage(), e);
            return new LinkedInProfileDTO(); // 오류 발생 시 빈 객체 반환
        }
    }

    /**
     * 프로필 DTO에 연관 데이터 추가 (경력, 학력, 스킬)
     */
    private void addRelatedDataToProfileDTO(LinkedInProfileDTO profileDTO) {
        if (profileDTO == null || profileDTO.getProfileSeq() == null) {
            return;
        }

        Long profileSeq = profileDTO.getProfileSeq();

        try {
            // 경력 정보 조회
            RequestBox expBox = new RequestBox("expBox");
            safelyPut(expBox, "profileSeq", profileSeq);
            List<DataBox> expDataBoxes = linkedInProfileDAO.selectExperiencesByProfileSeq(expBox);

            List<LinkedInExperienceDTO> experiences = new ArrayList<>();
            if (expDataBoxes != null) {
                for (DataBox exp : expDataBoxes) {
                    if (exp != null) {
                        LinkedInExperienceDTO expDTO = LinkedInExperienceDTO.builder()
                                .experienceSeq(exp.getLong2("d_experience_seq"))
                                .profileSeq(exp.getLong2("d_profile_seq"))
                                .title(exp.getString("d_title"))
                                .company(exp.getString("d_company"))
                                .duration(exp.getString("d_duration"))
                                .description(exp.getString("d_description"))
                                .indate(exp.getString("d_indate"))
                                .build();
                        experiences.add(expDTO);
                    }
                }
            }
            profileDTO.setExperiences(experiences);

            // 학력 정보 조회
            RequestBox eduBox = new RequestBox("eduBox");
            safelyPut(eduBox, "profileSeq", profileSeq);
            List<DataBox> eduDataBoxes = linkedInProfileDAO.selectEducationsByProfileSeq(eduBox);

            List<LinkedInEducationDTO> educations = new ArrayList<>();
            if (eduDataBoxes != null) {
                for (DataBox edu : eduDataBoxes) {
                    if (edu != null) {
                        LinkedInEducationDTO eduDTO = LinkedInEducationDTO.builder()
                                .educationSeq(edu.getLong2("d_education_seq"))
                                .profileSeq(edu.getLong2("d_profile_seq"))
                                .school(edu.getString("d_school"))
                                .degree(edu.getString("d_degree"))
                                .fieldOfStudy(edu.getString("d_field_of_study"))
                                .duration(edu.getString("d_duration"))
                                .indate(edu.getString("d_indate"))
                                .build();
                        educations.add(eduDTO);
                    }
                }
            }
            profileDTO.setEducations(educations);

            // 스킬 정보 조회
            RequestBox skillBox = new RequestBox("skillBox");
            safelyPut(skillBox, "profileSeq", profileSeq);
            List<DataBox> skillDataBoxes = linkedInProfileDAO.selectSkillsByProfileSeq(skillBox);

            List<LinkedInSkillDTO> skills = new ArrayList<>();
            if (skillDataBoxes != null) {
                for (DataBox skill : skillDataBoxes) {
                    if (skill != null) {
                        LinkedInSkillDTO skillDTO = LinkedInSkillDTO.builder()
                                .skillSeq(skill.getLong2("d_skill_seq"))
                                .profileSeq(skill.getLong2("d_profile_seq"))
                                .skillName(skill.getString("d_skill_name"))
                                .indate(skill.getString("d_indate"))
                                .build();
                        skills.add(skillDTO);
                    }
                }
            }
            profileDTO.setSkills(skills);
        } catch (Exception e) {
            logger.error("프로필 관련 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            // 오류가 발생해도 계속 진행 - 부분적으로 데이터가 누락될 수 있음
        }
    }
}