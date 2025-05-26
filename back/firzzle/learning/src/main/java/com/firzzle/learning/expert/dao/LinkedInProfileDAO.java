package com.firzzle.learning.expert.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import com.firzzle.learning.expert.service.GoogleSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Class Name : LinkedInProfileDAO.java
 * @Description : LinkedIn 프로필 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Repository
public class LinkedInProfileDAO extends MyBatisSupport {

    private final Logger logger = LoggerFactory.getLogger(LinkedInProfileDAO.class);

    private static final String NAMESPACE = "LinkedInProfileMapper";

    /**
     * LinkedIn 프로필 정보 저장
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 저장된 프로필 일련번호
     */
    public int insertLinkedInProfile(RequestBox box) {
        logger.debug("LinkedIn 프로필 정보 저장 - 이름: {}, URL: {}", box.getString("name"), box.getString("linkedinUrl"));
        return sqlSession.insert(NAMESPACE + ".insertLinkedInProfile", box);
    }

    /**
     * LinkedIn 프로필 경력 정보 저장
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 저장된 경력 일련번호
     */
    public int insertLinkedInExperience(RequestBox box) {
        return sqlSession.insert(NAMESPACE + ".insertLinkedInExperience", box);
    }

    /**
     * LinkedIn 프로필 학력 정보 저장
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 저장된 학력 일련번호
     */
    public int insertLinkedInEducation(RequestBox box) {
        return sqlSession.insert(NAMESPACE + ".insertLinkedInEducation", box);
    }

    /**
     * LinkedIn 프로필 스킬 정보 저장
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 저장된 스킬 일련번호
     */
    public int insertLinkedInSkill(RequestBox box) {
        return sqlSession.insert(NAMESPACE + ".insertLinkedInSkill", box);
    }

    /**
     * LinkedIn URL로 프로필 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 프로필 정보
     */
    public DataBox selectProfileByLinkedInUrl(RequestBox box) {
        return (DataBox) sqlSession.selectOne(NAMESPACE + ".selectProfileByLinkedInUrl", box);
    }

    /**
     * LinkedIn 프로필 목록 조회 (페이지네이션)
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 프로필 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectLinkedInProfiles(RequestBox box) {
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectLinkedInProfiles", box);
    }

    /**
     * LinkedIn 프로필 총 개수 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 프로필 총 개수
     */
    public int selectLinkedInProfilesCount(RequestBox box) {
        return (int) sqlSession.selectOne(NAMESPACE + ".selectLinkedInProfilesCount", box);
    }

    /**
     * LinkedIn 프로필 정보 목록 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 프로필 정보 목록
     */
    @SuppressWarnings("unchecked")
    public DataBox selectProfileByProfileSeq(RequestBox box) {
        return sqlSession.selectDataBox(NAMESPACE + ".selectProfileByProfileSeq", box);
    }

    /**
     * LinkedIn 프로필 경력 정보 목록 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 경력 정보 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectExperiencesByProfileSeq(RequestBox box) {
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectExperiencesByProfileSeq", box);
    }

    /**
     * LinkedIn 프로필 학력 정보 목록 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 학력 정보 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectEducationsByProfileSeq(RequestBox box) {
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectEducationsByProfileSeq", box);
    }

    /**
     * LinkedIn 프로필 스킬 목록 조회
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 스킬 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> selectSkillsByProfileSeq(RequestBox box) {
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectSkillsByProfileSeq", box);
    }

    /**
     * LinkedIn 프로필 삭제 (논리적 삭제)
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 영향받은 행 수
     */
    public int updateLinkedInProfileDeleteYn(RequestBox box) {
        return sqlSession.update(NAMESPACE + ".updateLinkedInProfileDeleteYn", box);
    }

    /**
     * 키워드로 LinkedIn 프로필 검색
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 프로필 목록
     */
    @SuppressWarnings("unchecked")
    public List<DataBox> searchLinkedInProfiles(RequestBox box) {
        return sqlSession.selectDataBoxList(NAMESPACE + ".searchLinkedInProfiles", box);
    }

    /**
     * 키워드로 검색된 LinkedIn 프로필 총 개수
     *
     * @param box - 요청 정보가 담긴 RequestBox
     * @return 프로필 총 개수
     */
    public int searchLinkedInProfilesCount(RequestBox box) {
        return (int) sqlSession.selectOne(NAMESPACE + ".searchLinkedInProfilesCount", box);
    }
}