package com.firzzle.learning.expert.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.learning.expert.dto.ExpertRecommendationResponseDTO;
import com.firzzle.learning.expert.dto.LinkedInEducationDTO;
import com.firzzle.learning.expert.dto.LinkedInExperienceDTO;
import com.firzzle.learning.expert.dto.LinkedInExpertDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Class Name : LlmApiClient.java
 * @Description : LLM 서비스 API 클라이언트
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Slf4j
@Component
public class LlmApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${llm.service.url}")
    private String llmServiceUrl;

    // 생성자 주입
    public LlmApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 현재 요청에서 Authorization 헤더를 가져옵니다.
     * @return Authorization 헤더 값, 없으면 null
     */
    private String getAuthorizationHeader() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                return request.getHeader("Authorization");
            }
        } catch (Exception e) {
            log.warn("요청에서 Authorization 헤더를 가져오는 중 오류 발생: {}", e.getMessage());
        }
        return null;
    }

    /**
     * LLM 서비스에 태그 기반으로 유사한 LinkedIn 프로필 검색 요청
     *
     * @param contentSeq 콘텐츠 일련번호
     * @param tags 태그 문자열 (쉼표로 구분)
     * @param minScore 최소 유사도 점수 (0-1)
     * @param limit 검색할 총 프로필 수
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return 유사한 프로필 목록
     */
    public ExpertRecommendationResponseDTO getSimilarLinkedInProfiles(
            Long contentSeq, String tags, Float minScore, Integer limit, Integer page, Integer pageSize) {
        try {
            log.info("LLM 서비스에 유사 LinkedIn 프로필 검색 요청 - 콘텐츠: {}, 태그: {}", contentSeq, tags);

            // API URL 구성
            String url = UriComponentsBuilder.fromHttpUrl(llmServiceUrl)
                    .path("/service/api/v1/llm/expert/embeddings/linkedin/similar")
                    .queryParam("contentSeq", contentSeq)
                    .queryParam("tags", tags)
                    .queryParam("minScore", minScore)
                    .queryParam("limit", limit)
                    .queryParam("page", page)
                    .queryParam("pageSize", pageSize)
                    .build()
                    .toUriString();

            log.debug("최종 요청 URL: {}", url);

            // 헤더 설정 - Authorization 헤더 추가
            HttpHeaders headers = new HttpHeaders();
            String authHeader = getAuthorizationHeader();
            if (authHeader != null && !authHeader.isEmpty()) {
                headers.set("Authorization", authHeader);
                log.debug("Authorization 헤더 추가: {}", authHeader);
            } else {
                log.warn("Authorization 헤더가 없습니다. 인증 없이 요청합니다.");
            }

            // HTTP 엔티티 생성
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 요청 실행
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseBody = response.getBody();
                log.debug("LLM 서비스 응답: {}", responseBody);

                // ObjectMapper를 사용하여 수동으로 파싱
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode dataNode = rootNode.path("data");

                if (dataNode.isMissingNode() || dataNode.isNull()) {
                    log.warn("LLM 서비스 응답에 data 필드가 없습니다.");
                    return createEmptyResponse(contentSeq, tags, page, pageSize);
                }

                // 응답 데이터 구조 확인
                log.debug("data 노드 구조: {}", dataNode.toString());

                // 필드 추출
                Long respContentSeq = dataNode.has("contentSeq") ? dataNode.get("contentSeq").asLong() : contentSeq;
                String respTags = dataNode.has("tags") ? dataNode.get("tags").asText() : tags;
                Integer respPage = dataNode.has("page") ? dataNode.get("page").asInt() : page;
                Integer respPageSize = dataNode.has("pageSize") ? dataNode.get("pageSize").asInt() : pageSize;
                Integer totalElements = dataNode.has("totalElements") ? dataNode.get("totalElements").asInt() : 0;
                Integer totalPages = dataNode.has("totalPages") ? dataNode.get("totalPages").asInt() : 0;
                Boolean last = dataNode.has("last") ? dataNode.get("last").asBoolean() : true;
                Boolean hasNext = dataNode.has("hasNext") ? dataNode.get("hasNext").asBoolean() : false;

                // 여기서 profiles 필드 찾기
                List<LinkedInExpertDTO> experts = new ArrayList<>();
                JsonNode profilesNode = dataNode.path("profiles");

                if (!profilesNode.isMissingNode() && profilesNode.isArray()) {
                    log.debug("profiles 필드 발견: {}", profilesNode.size());

                    for (JsonNode profileNode : profilesNode) {
                        LinkedInExpertDTO expert = LinkedInExpertDTO.builder()
                                .profileSeq(profileNode.has("profileSeq") ? profileNode.get("profileSeq").asLong() : null)
                                .linkedinUrl(profileNode.has("linkedinUrl") ? profileNode.get("linkedinUrl").asText() : "")
                                .name(profileNode.has("name") ? profileNode.get("name").asText() : "")
                                .headline(profileNode.has("headline") ? profileNode.get("headline").asText() : "")
                                .company(profileNode.has("company") ? profileNode.get("company").asText() : "")
                                .location(profileNode.has("location") ? profileNode.get("location").asText() : "")
                                .profileImageUrl(profileNode.has("profileImageUrl") ? profileNode.get("profileImageUrl").asText() : "")
                                .similarity(profileNode.has("similarity") ? (float)profileNode.get("similarity").asDouble() : 0f)
                                .build();

                        // 경력, 학력, 스킬 정보 추출
                        if (profileNode.has("experiences") && profileNode.get("experiences").isArray()) {
                            expert.setExperiences(objectMapper.convertValue(profileNode.get("experiences"),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, LinkedInExperienceDTO.class)));
                        }

                        if (profileNode.has("educations") && profileNode.get("educations").isArray()) {
                            expert.setEducations(objectMapper.convertValue(profileNode.get("educations"),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, LinkedInEducationDTO.class)));
                        }

                        if (profileNode.has("skills") && profileNode.get("skills").isArray()) {
                            expert.setSkills(objectMapper.convertValue(profileNode.get("skills"),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
                        }

                        experts.add(expert);
                    }
                } else {
                    log.warn("profiles 필드를 찾을 수 없거나 배열이 아닙니다.");
                }

                // 결과 DTO 생성
                return ExpertRecommendationResponseDTO.builder()
                        .contentSeq(respContentSeq)
                        .tags(respTags)
                        .experts(experts)
                        .page(respPage)
                        .pageSize(respPageSize)
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .last(last)
                        .hasNext(hasNext)
                        .build();
            } else {
                log.error("LLM 서비스 호출 실패: statusCode={}", response.getStatusCode());
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "LLM 서비스 호출에 실패했습니다.");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("LLM 서비스 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "LLM 서비스 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 비어있는 응답 DTO 생성
     */
    private ExpertRecommendationResponseDTO createEmptyResponse(
            Long contentSeq, String tags, Integer page, Integer pageSize) {
        return ExpertRecommendationResponseDTO.builder()
                .contentSeq(contentSeq)
                .tags(tags)
                .experts(Collections.emptyList())
                .page(page)
                .pageSize(pageSize)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .hasNext(false)
                .build();
    }
}