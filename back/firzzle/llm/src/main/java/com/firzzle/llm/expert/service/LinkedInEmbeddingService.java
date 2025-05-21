package com.firzzle.llm.expert.service;

import com.firzzle.llm.service.EmbeddingService;
import com.firzzle.llm.expert.dto.LinkedInEmbeddingRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LinkedIn 프로필 임베딩 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedInEmbeddingService {

    private final EmbeddingService embeddingService;

    /**
     * LinkedIn 프로필 정보를 텍스트로 변환합니다.
     *
     * @param profile LinkedIn 프로필 데이터
     * @return 임베딩용 텍스트
     */
    public String convertProfileToText(LinkedInEmbeddingRequestDTO profile) {
        StringBuilder sb = new StringBuilder();

        // 기본 정보 추가
        if (profile.getName() != null) sb.append("이름: ").append(profile.getName()).append(". ");
        if (profile.getHeadline() != null) sb.append("직함: ").append(profile.getHeadline()).append(". ");
        if (profile.getCompany() != null) sb.append("회사: ").append(profile.getCompany()).append(". ");

        // 요약 정보 추가
        if (profile.getSummary() != null && !profile.getSummary().isBlank()) {
            sb.append("요약: ").append(profile.getSummary()).append(". ");
        }

        // 스킬 정보 추가
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            sb.append("스킬: ").append(String.join(", ", profile.getSkills())).append(".");
        }

        return sb.toString();
    }

    /**
     * LinkedIn 프로필 정보를 임베딩합니다.
     *
     * @param profile LinkedIn 프로필 데이터
     * @return 임베딩 벡터
     */
    public List<Float> createEmbedding(LinkedInEmbeddingRequestDTO profile) {
        // 프로필 정보를 텍스트로 변환
        String profileText = convertProfileToText(profile);
        log.info("LinkedIn 프로필 임베딩 텍스트 생성: profileSeq={}, text={}", profile.getProfileSeq(), profileText);

        // OpenAI API를 사용하여 임베딩 생성
        List<Float> embedding = embeddingService.embed(profileText);
        log.info("LinkedIn 프로필 임베딩 완료: profileSeq={}, 벡터 크기={}", profile.getProfileSeq(), embedding.size());

        return embedding;
    }

    /**
     * 태그 문자열을 임베딩합니다.
     *
     * @param tags 태그 문자열 (쉼표로 구분)
     * @return 임베딩 벡터
     */
    public List<Float> createTagsEmbedding(String tags) {
        if (tags == null || tags.isBlank()) {
            throw new IllegalArgumentException("태그가 비어있습니다.");
        }

        log.info("태그 임베딩 생성: tags={}", tags);
        List<Float> embedding = embeddingService.embed(tags);
        log.info("태그 임베딩 완료: 벡터 크기={}", embedding.size());

        return embedding;
    }

    /**
     * LinkedIn 프로필 임베딩용 파라미터 생성
     *
     * @param profile LinkedIn 프로필 데이터
     * @return 저장용 파라미터 맵
     */
    public Map<String, Object> createProfilePayload(LinkedInEmbeddingRequestDTO profile) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("profileSeq", profile.getProfileSeq());
        payload.put("linkedinUrl", profile.getLinkedinUrl());
        payload.put("name", profile.getName());
        payload.put("headline", profile.getHeadline());
        payload.put("company", profile.getCompany());

        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            payload.put("skills", profile.getSkills());
        }

        return payload;
    }
}