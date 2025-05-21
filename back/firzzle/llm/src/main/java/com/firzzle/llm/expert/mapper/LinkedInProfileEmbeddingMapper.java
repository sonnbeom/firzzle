package com.firzzle.llm.expert.mapper;

import com.firzzle.llm.expert.dto.LinkedInEmbeddingRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * LinkedIn 프로필 데이터 접근 인터페이스
 */
@Mapper
public interface LinkedInProfileEmbeddingMapper {

    /**
     * 프로필 일련번호로 LinkedIn 프로필 정보 조회
     *
     * @param profileSeq 프로필 일련번호
     * @return LinkedIn 프로필 정보
     */
    LinkedInEmbeddingRequestDTO selectProfileForEmbedding(@Param("profileSeq") Long profileSeq);

    /**
     * 프로필 일련번호 목록으로 LinkedIn 프로필 정보 조회
     *
     * @param profileSeqs 프로필 일련번호 목록
     * @return LinkedIn 프로필 정보 목록
     */
    List<LinkedInEmbeddingRequestDTO> selectProfilesForEmbedding(@Param("profileSeqs") List<Long> profileSeqs);

    /**
     * 프로필의 스킬 목록 조회
     *
     * @param profileSeq 프로필 일련번호
     * @return 스킬 이름 목록
     */
    List<String> selectSkillsByProfileSeq(@Param("profileSeq") Long profileSeq);
}