package com.firzzle.learning.expert.service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.utils.DateUtil;
import com.firzzle.learning.expert.dto.*;
import com.firzzle.learning.kafka.producer.LearningProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Class Name : LinkedInCrawlingIntegrationService.java
 * @Description : LinkedIn 크롤링 통합 서비스
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Service
public class LinkedInCrawlingIntegrationService {

    private final Logger logger = LoggerFactory.getLogger(LinkedInCrawlingIntegrationService.class);
    
    private final GoogleSearchService googleSearchService;
    private final LinkedInCrawlerService linkedInCrawlerService;
    private final LinkedInProfileService linkedInProfileService;
    private final LearningProducer learningProducer;

    @Value("${linkedin.crawler.max-profiles:10}")
    private int maxProfiles;

    @Value("${app.kafka.topic.linkedin-profile-crawled:linkedin-profile-crawled}")
    private String profileCrawledTopic;

    @Autowired
    public LinkedInCrawlingIntegrationService(
            GoogleSearchService googleSearchService,
            LinkedInCrawlerService linkedInCrawlerService,
            LinkedInProfileService linkedInProfileService,
            LearningProducer learningProducer) {
        this.googleSearchService = googleSearchService;
        this.linkedInCrawlerService = linkedInCrawlerService;
        this.linkedInProfileService = linkedInProfileService;
        this.learningProducer = learningProducer;
    }

    /**
     * LinkedIn 프로필 크롤링 및 저장을 수행합니다.
     *
     * @param requestDTO 크롤링 요청 DTO
     * @return 크롤링 응답 DTO
     */
    @Transactional
    public LinkedInCrawlerResponseDTO crawlAndSaveLinkedInProfiles(LinkedInCrawlerRequestDTO requestDTO) {
        logger.info("LinkedIn 프로필 크롤링 및 저장 시작 - 키워드: {}, 제한: {}",
                requestDTO.getKeyword(), requestDTO.getLimit());

        try {
            // 1. LinkedIn 프로필 URL 목록 획득 (요청에 URL 목록이 있으면 사용, 없으면 검색)
            List<String> linkedInUrls;
            if (requestDTO.getLinkedinUrls() != null && !requestDTO.getLinkedinUrls().isEmpty()) {
                linkedInUrls = requestDTO.getLinkedinUrls();
                logger.info("요청에서 {} 개의 LinkedIn URL을 받았습니다.", linkedInUrls.size());
            } else if (requestDTO.getKeyword() != null && !requestDTO.getKeyword().isEmpty()) {
                // Google 검색을 통해 LinkedIn URL 검색
                int limit = (requestDTO.getLimit() != null && requestDTO.getLimit() > 0) ?
                        Math.min(requestDTO.getLimit(), maxProfiles) : 3;

                logger.info("limit : {}", limit);

                // Google 검색으로 URL을 가져옴 - 더 많은 URL을 가져와서 중복 제거 후에도 충분히 남도록 함
                List<String> foundUrls = googleSearchService.searchLinkedInProfilesDynamic(requestDTO.getKeyword(), limit * 3);
                logger.info("Google 검색을 통해 {} 개의 LinkedIn URL을 찾았습니다.", foundUrls.size());

                // 중복 URL 제거 (DB에 이미 존재하는 URL 확인)
                linkedInUrls = removeDuplicateUrls(foundUrls, limit);
                logger.info("중복 제거 후 {} 개의 LinkedIn URL이 남았습니다.", linkedInUrls.size());
            } else {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "검색 키워드 또는 LinkedIn URL 목록이 필요합니다.");
            }

            if (linkedInUrls.isEmpty()) {
                logger.warn("LinkedIn 프로필 URL을 찾을 수 없습니다.");
                return LinkedInCrawlerResponseDTO.responseBuilder()
                        .message("LinkedIn 프로필 URL을 찾을 수 없습니다.")
                        .keyword(requestDTO.getKeyword())
                        .profileCount(0)
                        .profiles(Collections.emptyList())
                        .crawledAt(DateUtil.getCurrentDatetime())
                        .build();
            }

            // 2. LinkedIn 프로필 크롤링
            List<LinkedInProfileDTO> profiles = linkedInCrawlerService.crawlLinkedInProfiles(linkedInUrls);
            logger.info("{} 개의 LinkedIn 프로필 크롤링 완료", profiles.size());

            // 3. 콘텐츠 관련성 점수 계산
            calculateRelevanceScores(profiles, requestDTO.getKeyword());
            logger.info("관련성 점수 계산 완료");

            // 4. 프로필 데이터 저장
            if (profiles.isEmpty()) {
                logger.warn("크롤링된 LinkedIn 프로필이 없습니다.");
                return LinkedInCrawlerResponseDTO.responseBuilder()
                        .message("크롤링된 LinkedIn 프로필이 없습니다.")
                        .keyword(requestDTO.getKeyword())
                        .profileCount(0)
                        .profiles(Collections.emptyList())
                        .crawledAt(DateUtil.getCurrentDatetime())
                        .build();
            }

            List<Long> savedProfileSeqs = linkedInProfileService.saveLinkedInProfiles(profiles);
            logger.info("{} 개의 LinkedIn 프로필 저장 완료", savedProfileSeqs.size());

            // 5. 이벤트 발행 방식 1: 각 프로필마다 개별 이벤트 발행
//            for (int i = 0; i < profiles.size(); i++) {
//                LinkedInProfileDTO profile = profiles.get(i);
//                Long profileSeq = savedProfileSeqs.get(i);
//                profile.setProfileSeq(profileSeq);
//
//                // 스킬 목록 추출
//                List<String> skillNames = profile.getSkills() != null ?
//                        profile.getSkills().stream()
//                                .map(LinkedInSkillDTO::getSkillName)
//                                .collect(Collectors.toList()) :
//                        Collections.emptyList();
//
//                // 이벤트 생성 및 발행
//                LinkedInProfileCrawledEvent event = LinkedInProfileCrawledEvent.builder()
//                        .profileSeq(profileSeq)
//                        .linkedinUrl(profile.getLinkedinUrl())
//                        .name(profile.getName())
//                        .headline(profile.getHeadline())
//                        .company(profile.getCompany())
//                        .summary(profile.getSummary())
//                        .skills(skillNames)
//                        .timestamp(DateUtil.getCurrentDatetime())
//                        .build();
//
//                kafkaTemplate.send(profileCrawledTopic, event);
//                logger.info("LinkedIn 프로필 크롤링 이벤트 발행: {}, 프로필: {}", profileCrawledTopic, profileSeq);
//            }

            // 5-1. 이벤트 발행 방식 2: 일괄 처리 신호 발행 (LLM 모듈이 DB에서 직접 조회하는 경우)
            // LearningProducer를 통해 일괄 처리 신호 발행
//            learningProducer.sendLinkedInBatchSignal(savedProfileSeqs, requestDTO.getKeyword());

            // 6. 응답 DTO 생성
            String crawledAt = DateUtil.getCurrentDatetime();
            return LinkedInCrawlerResponseDTO.responseBuilder()
                    .message("LinkedIn 프로필 크롤링 및 저장이 완료되었습니다.")
                    .keyword(requestDTO.getKeyword())
                    .profileCount(profiles.size())
                    .profiles(profiles)
                    .crawledAt(crawledAt)
                    .build();

        } catch (BusinessException e) {
            logger.error("LinkedIn 프로필 크롤링 및 저장 중 비즈니스 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 크롤링 및 저장 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 크롤링 및 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * URL 목록에서 이미 DB에 존재하는 URL을 제거하고 최대 limit 개수만큼만 반환합니다.
     *
     * @param urls 확인할 URL 목록
     * @param limit 최대 URL 개수
     * @return 중복이 제거된 URL 목록
     */
    private List<String> removeDuplicateUrls(List<String> urls, int limit) {
        List<String> uniqueUrls = new ArrayList<>();
        int count = 0;

        for (String url : urls) {
            // DB에서 URL이 이미 존재하는지 확인
            RequestBox box = new RequestBox("checkBox");
            box.put("linkedinUrl", url);
            DataBox existingProfile = linkedInProfileService.getLinkedInProfileDAO().selectProfileByLinkedInUrl(box);

            if (existingProfile == null) {
                // 기존에 없는 URL인 경우 추가
                uniqueUrls.add(url);
                count++;

                // limit 개수에 도달하면 중단
                if (count >= limit) {
                    break;
                }
            } else {
                logger.debug("이미 존재하는 LinkedIn URL 제외: {}", url);
            }
        }

        return uniqueUrls;
    }

    /**
     * 키워드에 대한 프로필의 관련성 점수를 계산합니다.
     * 이 예제에서는 간단한 로직을 사용하지만, 실제로는 더 복잡한 AI 기반 알고리즘을 적용할 수 있습니다.
     *
     * @param profiles 프로필 목록
     * @param keyword 검색 키워드
     */
    private void calculateRelevanceScores(List<LinkedInProfileDTO> profiles, String keyword) {
        if (keyword == null || keyword.isEmpty() || profiles.isEmpty()) {
            return;
        }

        String lowerKeyword = keyword.toLowerCase();
        String[] keywordTokens = lowerKeyword.split("\\s+");

        try {
            for (LinkedInProfileDTO profile : profiles) {
                float relevanceScore = 0.5f;  // 기본 점수
                int matches = 0;

                // 직함에 키워드가 포함되어 있는지 확인
                if (profile.getHeadline() != null) {
                    String headline = profile.getHeadline().toLowerCase();
                    for (String token : keywordTokens) {
                        if (headline.contains(token)) {
                            matches++;
                            relevanceScore += 0.05f;
                        }
                    }

                    // 추가 키워드 체크 (역할/직급 관련)
                    if (headline.contains("specialist") || headline.contains("expert")) {
                        relevanceScore += 0.1f;
                    }
                    if (headline.contains("senior") || headline.contains("lead")) {
                        relevanceScore += 0.1f;
                    }
                    if (headline.contains("manager") || headline.contains("director")) {
                        relevanceScore += 0.15f;
                    }
                }

                // 회사명 체크
                if (profile.getCompany() != null) {
                    String company = profile.getCompany().toLowerCase();
                    for (String token : keywordTokens) {
                        if (company.contains(token)) {
                            matches++;
                            relevanceScore += 0.05f;
                        }
                    }
                }

                // 스킬 기반 점수 조정
                if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
                    for (LinkedInSkillDTO skill : profile.getSkills()) {
                        if (skill.getSkillName() != null) {
                            String skillName = skill.getSkillName().toLowerCase();
                            for (String token : keywordTokens) {
                                if (skillName.contains(token)) {
                                    matches++;
                                    relevanceScore += 0.05f;
                                }
                            }
                        }
                    }

                    // 스킬 수에 따른 가중치
                    relevanceScore += Math.min(0.2f, profile.getSkills().size() * 0.02f);
                }

                // 요약 내용 체크
                if (profile.getSummary() != null) {
                    String summary = profile.getSummary().toLowerCase();
                    for (String token : keywordTokens) {
                        if (summary.contains(token)) {
                            matches++;
                            relevanceScore += 0.02f;
                        }
                    }
                }

                // 경력 기반 점수 조정
                if (profile.getExperiences() != null && !profile.getExperiences().isEmpty()) {
                    // 경력 수에 따른 가중치
                    relevanceScore += Math.min(0.2f, profile.getExperiences().size() * 0.05f);
                }

                // 키워드 매칭 보너스
                if (matches > 0) {
                    relevanceScore += Math.min(0.3f, matches * 0.03f);
                }

                // 최종 점수는 0.0 ~ 1.0 사이로 제한
                relevanceScore = Math.min(1.0f, Math.max(0.0f, relevanceScore));

                profile.setRelevance(relevanceScore);
            }

            // 관련성 점수로 정렬
            Collections.sort(profiles, (p1, p2) -> Float.compare(p2.getRelevance(), p1.getRelevance()));

        } catch (Exception e) {
            logger.error("관련성 점수 계산 중 오류 발생: {}", e.getMessage(), e);
            // 오류가 발생해도 프로세스는 계속 진행 (기본 점수 사용)
        }
    }

    /**
     * 키워드를 통해 LinkedIn 프로필을 검색합니다.
     *
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return 프로필 목록 (페이지네이션)
     */
    public LinkedInCrawlerResponseDTO searchLinkedInProfiles(String keyword, Integer page, Integer pageSize) {
        try {
            LinkedInCrawlerRequestDTO searchDTO = LinkedInCrawlerRequestDTO.builder()
                    .keyword(keyword)
                    .page(page)
                    .limit(pageSize)
                    .build();

            return linkedInProfileService.getLinkedInProfiles(searchDTO);
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 검색 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "LinkedIn 프로필 검색 중 오류 발생: " + e.getMessage());
        }
    }
}