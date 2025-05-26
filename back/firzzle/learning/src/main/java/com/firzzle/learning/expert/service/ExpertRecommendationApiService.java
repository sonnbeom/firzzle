package com.firzzle.learning.expert.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.learning.dto.ExpertRecommendationResponseDTO2;
import com.firzzle.learning.dto.ExpertResponseDTO;
import com.firzzle.learning.expert.client.LlmApiClient;
import com.firzzle.learning.expert.dto.ExpertRecommendationResponseDTO;
import com.firzzle.learning.expert.dto.LinkedInExpertDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Class Name : ExpertRecommendationApiService.java
 * @Description : 전문가 추천 API 서비스
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpertRecommendationApiService {

    private final LlmApiClient llmApiClient;
    private final LinkedInProfileService linkedInProfileService;

    /**
     * 콘텐츠 태그 기반으로 유사한 LinkedIn 전문가를 추천합니다. (내부용)
     *
     * @param contentSeq 콘텐츠 일련번호
     * @param tags 콘텐츠 태그 (쉼표로 구분)
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param minScore 최소 유사도 점수 (0-1)
     * @return 추천 전문가 목록
     */
    public ExpertRecommendationResponseDTO getRecommendedLinkedInExperts(
            Long contentSeq, String tags, Integer page, Integer pageSize, Float minScore) {

        try {
            log.info("LinkedIn 전문가 추천 시작 - 콘텐츠: {}, 태그: {}, 페이지: {}/{}, 유사도: {}",
                    contentSeq, tags, page, pageSize, minScore);

            // 1. LLM 서비스 호출하여 유사한 프로필 검색
            ExpertRecommendationResponseDTO similarityResponse = llmApiClient.getSimilarLinkedInProfiles(
                    contentSeq, tags, minScore, 9, page, pageSize);

            log.debug("similarityResponse.toString() : " + similarityResponse.toString());

            if (similarityResponse == null || similarityResponse.getExperts() == null ||
                    similarityResponse.getExperts().isEmpty()) {
                log.info("유사한 LinkedIn 프로필을 찾을 수 없습니다. 콘텐츠: {}, 태그: {}", contentSeq, tags);
                return createEmptyResponse(contentSeq, tags, page, pageSize);
            }

            // 2. 유사한 프로필 정보 보강 (경력, 학력, 스킬 정보 등)
            enrichProfileDetails(similarityResponse.getExperts());

            log.info("LinkedIn 전문가 추천 완료 - 콘텐츠: {}, 추천수: {}",
                    contentSeq, similarityResponse.getExperts().size());

            return similarityResponse;

        } catch (Exception e) {
            log.error("LinkedIn 전문가 추천 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 전문가 추천 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 콘텐츠 태그 기반으로 유사한 LinkedIn 전문가를 추천합니다. (API 응답용)
     *
     * @param contentSeq 콘텐츠 일련번호
     * @param tags 콘텐츠 태그 (쉼표로 구분)
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param minScore 최소 유사도 점수 (0-1)
     * @return API 명세에 맞는 추천 전문가 목록
     */
    public ExpertRecommendationResponseDTO2 getRecommendedLinkedInExpertsForAPI(
            Long contentSeq, String tags, Integer page, Integer pageSize, Float minScore) {

        // 내부용 메서드 호출
        ExpertRecommendationResponseDTO llmResponse = getRecommendedLinkedInExperts(
                contentSeq, tags, page, pageSize, minScore);

        // LLM 응답을 API 명세 형식으로 변환
        List<ExpertResponseDTO> expertResponses = new ArrayList<>();

        if (llmResponse != null && llmResponse.getExperts() != null) {
            for (LinkedInExpertDTO expert : llmResponse.getExperts()) {
                // LinkedInExpertDTO를 ExpertResponseDTO로 변환
                ExpertResponseDTO expertResponse = ExpertResponseDTO.builder()
                        .expertSeq(expert.getProfileSeq())
                        .name(expert.getName())
                        .title(expert.getHeadline())
                        .company(expert.getCompany())
                        .profileImageUrl(expert.getProfileImageUrl())
                        .linkedinUrl(expert.getLinkedinUrl())
                        .relevance(expert.getSimilarity())
                        .expertise(expert.getSkills() != null ? expert.getSkills() : Collections.emptyList())
                        .build();

                expertResponses.add(expertResponse);
            }
        }

        // 최종 응답 DTO 생성
        return ExpertRecommendationResponseDTO2.recommendationBuilder()
                .content(expertResponses)
                .p_pageno(llmResponse.getPage())
                .p_pagesize(llmResponse.getPageSize())
                .totalElements(llmResponse.getTotalElements())
                .originTags(tags)
                .build();
    }

    /**
     * 비어있는 응답 DTO 생성
     */
    private ExpertRecommendationResponseDTO createEmptyResponse(
            Long contentSeq, String tags, Integer page, Integer pageSize) {
        return ExpertRecommendationResponseDTO.builder()
                .contentSeq(contentSeq)
                .tags(tags)
                .experts(new ArrayList<>())
                .page(page)
                .pageSize(pageSize)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .hasNext(false)
                .build();
    }

    /**
     * 프로필 상세 정보 보강 (경력, 학력, 스킬 정보)
     */
    private void enrichProfileDetails(List<LinkedInExpertDTO> experts) {
        for (LinkedInExpertDTO expert : experts) {
            try {
                // MySQL에서 프로필 상세 정보 조회
                linkedInProfileService.enrichProfileDetails(expert);
            } catch (Exception e) {
                log.warn("프로필 상세 정보 조회 실패: profileSeq={}, error={}",
                        expert.getProfileSeq(), e.getMessage());
                // 기본 정보만 유지하고 계속 진행
            }
        }
    }
}