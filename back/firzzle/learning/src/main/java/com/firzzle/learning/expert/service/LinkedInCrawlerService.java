package com.firzzle.learning.expert.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.utils.DateUtil;
import com.firzzle.learning.expert.dto.LinkedInEducationDTO;
import com.firzzle.learning.expert.dto.LinkedInExperienceDTO;
import com.firzzle.learning.expert.dto.LinkedInProfileDTO;
import com.firzzle.learning.expert.dto.LinkedInSkillDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Class Name : LinkedInCrawlerService.java
 * @Description : LinkedIn 프로필 크롤링 서비스
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Service
public class LinkedInCrawlerService {

    private final Logger logger = LoggerFactory.getLogger(LinkedInCrawlerService.class);

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${apify.api.token:YOUR_APIFY_API_TOKEN}")
    private String apifyApiToken;

    @Value("${apify.actor.id:}")
    private String apifyActorId;

    @Value("${linkedin.crawler.timeout-seconds:600}")
    private int timeoutSeconds;

    @Autowired
    public LinkedInCrawlerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * LinkedIn 프로필 URL 목록을 크롤링합니다.
     * Python 코드와 동일한 Actor ID를 사용합니다.
     *
     * @param profileUrls LinkedIn 프로필 URL 목록
     * @return 크롤링된 LinkedIn 프로필 목록
     */
    public List<LinkedInProfileDTO> crawlLinkedInProfiles(List<String> profileUrls) {
        List<LinkedInProfileDTO> profiles = new ArrayList<>();

        if (profileUrls == null || profileUrls.isEmpty()) {
            logger.warn("크롤링할 LinkedIn 프로필 URL이 없습니다.");
            return profiles;
        }

        logger.info("Apify를 사용하여 {} 개의 LinkedIn 프로필 크롤링 시작", profileUrls.size());

        try {

            // Apify API 엔드포인트 - 액터 실행 및 결과 직접 가져오기
            String apiUrl = "https://api.apify.com/v2/acts/" + apifyActorId + "/run-sync-get-dataset-items?token=" + apifyApiToken;

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Apify actor에 전달할 입력값 설정 (Python 코드와 동일한 형식으로)
            String requestBody = createApifyRequestBody(profileUrls);

            // HTTP 요청 설정
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            // API 요청 실행
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // 응답 본문 추출
            String responseBody = response.getBody();
            logger.info("Apify 크롤링 결과 수신");

            // JSON 파싱
            JsonNode itemsArray = objectMapper.readTree(responseBody);
            logger.info("Apify 크롤링 responseBody : {}", itemsArray);

            if (itemsArray.isArray()) {
                for (JsonNode item : itemsArray) {
                    try {
                        LinkedInProfileDTO profile = parseProfileData(item);
                        if (profile != null) { // HTTP 오류 응답이 아닌 경우에만 추가
                            profiles.add(profile);
                            logger.info("크롤링된 LinkedIn 프로필: {}", profile.getName());
                        }
                    } catch (Exception e) {
                        logger.error("프로필 데이터 파싱 중 오류 발생: {}", e.getMessage(), e);
                    }
                }
            } else {
                logger.warn("반환된 크롤링 결과가 배열 형식이 아닙니다.");
            }

            if (profiles.isEmpty()) {
                logger.warn("크롤링된 LinkedIn 프로필이 없습니다.");
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("LinkedIn 프로필 크롤링 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.LINKEDIN_CRAWLING_FAILED, "LinkedIn 프로필 크롤링 중 오류 발생: " + e.getMessage());
        }

        return profiles;
    }

    /**
     * Apify API 요청 본문을 생성합니다. (최종 수정안)
     * URL 변환 없이 원본 URL 그대로 사용하고, contactCompassToken은 빈 문자열로 설정합니다.
     *
     * @param profileUrls LinkedIn 프로필 URL 목록
     * @return Apify API 요청 본문 (JSON 문자열)
     * @throws Exception JSON 변환 과정에서 오류가 발생할 경우
     */
    private String createApifyRequestBody(List<String> profileUrls) throws Exception {
        Map<String, Object> requestMap = new HashMap<>();

        // 1. urls 배열 생성 (객체 배열)
        List<Map<String, String>> urlsList = new ArrayList<>();
        for (String profileUrl : profileUrls) {
            // URL 변환 없이 원본 URL 그대로 사용
            Map<String, String> urlMap = new HashMap<>();
            urlMap.put("url", profileUrl);
            urlsList.add(urlMap);
        }
        requestMap.put("urls", urlsList);

        // 2. findContacts 설정 (boolean)
        requestMap.put("findContacts", false);

        // 3. contactCompassToken 설정 (빈 문자열)
        // Python에서는 None으로 했지만 API는 문자열을 요구하므로 빈 문자열로 설정
        requestMap.put("contactCompassToken", "");

        // 4. 추가 옵션
        requestMap.put("useCache", false);
        requestMap.put("headless", true);
        requestMap.put("proxyConfiguration", "RECOMMENDED");
        requestMap.put("maxRequestRetries", 3);

        // JSON 문자열로 변환
        String requestBody = objectMapper.writeValueAsString(requestMap);
        logger.debug("Apify 요청 본문: {}", requestBody);

        return requestBody;
    }

    private String extractRunId(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            logger.debug("응답 JSON 구조: {}", root.toString());

            // 새로운 응답 형식 처리: data.items[0].id
            if (root.has("data") && root.get("data").has("items") &&
                    root.get("data").get("items").isArray() &&
                    root.get("data").get("items").size() > 0) {

                JsonNode firstItem = root.get("data").get("items").get(0);
                if (firstItem.has("id")) {
                    return firstItem.get("id").asText();
                }
            }

            // 기존 응답 형식 처리: data.id
            if (root.has("data") && root.get("data").has("id")) {
                return root.get("data").get("id").asText();
            }
        } catch (Exception e) {
            logger.error("실행 ID 추출 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.APIFY_API_ERROR, "실행 ID 추출 중 오류 발생: " + e.getMessage());
        }

        logger.error("Apify 응답에서 실행 ID를 찾을 수 없습니다. 응답: {}", responseJson);
        throw new BusinessException(ErrorCode.APIFY_API_ERROR, "Apify 응답에서 실행 ID를 찾을 수 없습니다.");
    }

    private boolean waitForRunToFinish(String runId) throws Exception {
        String statusUrl = "https://api.apify.com/v2/actor-runs/" + runId + "?token=" + apifyApiToken;
        int maxAttempts = timeoutSeconds / 10; // 10초 간격으로 체크

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();

            // HTTP 요청 설정
            HttpEntity<String> request = new HttpEntity<>(headers);

            // 상태 확인 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    statusUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            // 응답 본문 추출
            String responseBody = response.getBody();

            // JSON 파싱
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.has("data") && root.get("data").has("status")) {
                String status = root.get("data").get("status").asText();

                if ("SUCCEEDED".equals(status)) {
                    logger.info("크롤링 작업 완료. 실행 ID: {}", runId);
                    return true;
                } else if ("FAILED".equals(status) || "ABORTED".equals(status) || "TIMED_OUT".equals(status)) {
                    logger.error("크롤링 작업 실패. 상태: {}", status);
                    throw new BusinessException(ErrorCode.LINKEDIN_CRAWLING_FAILED, "크롤링 작업 실패. 상태: " + status);
                }

                logger.info("크롤링 작업 진행 중... 상태: {}", status);
            }

            TimeUnit.SECONDS.sleep(10); // 10초 대기 후 다시 확인
        }

        logger.warn("크롤링 작업 타임아웃. 최대 대기 시간 초과.");
        throw new BusinessException(ErrorCode.LINKEDIN_CRAWLING_FAILED, "크롤링 작업 타임아웃. 최대 대기 시간(" + timeoutSeconds + "초)을 초과했습니다.");
    }

    private List<LinkedInProfileDTO> fetchCrawledData(String runId) throws Exception {
        List<LinkedInProfileDTO> profiles = new ArrayList<>();
        String datasetUrl = "https://api.apify.com/v2/actor-runs/" + runId + "/dataset/items?token=" + apifyApiToken;

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();

        // HTTP 요청 설정
        HttpEntity<String> request = new HttpEntity<>(headers);

        // 데이터셋 요청
        ResponseEntity<String> response = restTemplate.exchange(
                datasetUrl,
                HttpMethod.GET,
                request,
                String.class
        );

        // 응답 본문 추출
        String responseBody = response.getBody();

        // JSON 파싱
        JsonNode itemsArray = objectMapper.readTree(responseBody);

        if (itemsArray.isArray()) {
            for (JsonNode item : itemsArray) {
                try {
                    LinkedInProfileDTO profile = parseProfileData(item);
                    if (profile != null) { // HTTP 오류 응답이 아닌 경우에만 추가
                        profiles.add(profile);
                        logger.info("크롤링된 LinkedIn 프로필: {}", profile.getName());
                    }
                } catch (Exception e) {
                    logger.error("프로필 데이터 파싱 중 오류 발생: {}", e.getMessage(), e);
                }
            }
        }

        if (profiles.isEmpty()) {
            logger.warn("크롤링된 LinkedIn 프로필이 없습니다.");
        }

        return profiles;
    }

    private LinkedInProfileDTO parseProfileData(JsonNode profileData) {
        // HTTP 오류 응답 확인
        if (profileData.has("error")) {
            logger.warn("프로필 크롤링 중 오류 발생: {} for URL: {}",
                    profileData.get("error").asText(),
                    profileData.has("inputUrl") ? profileData.get("inputUrl").asText() : "Unknown URL");
            return null; // 오류가 있는 경우 null 반환
        }

        String currentDatetime = DateUtil.getCurrentDatetime(); // YYYYMMDDHHMMSS 형식

        LinkedInProfileDTO profile = new LinkedInProfileDTO();
        profile.setIndate(currentDatetime);
        profile.setLdate(currentDatetime);
        profile.setDeleteYn("N");

        // LinkedIn URL 설정
        if (profileData.has("url")) {
            profile.setLinkedinUrl(profileData.get("url").asText());
        } else if (profileData.has("inputUrl")) {
            // supreme_coder가 url 대신 inputUrl을 반환하는 경우도 있음
            profile.setLinkedinUrl(profileData.get("inputUrl").asText());
        }

        // 기본 정보 설정
        if (profileData.has("general")) {
            JsonNode general = profileData.get("general");
            profile.setName(getTextValue(general, "fullName"));
            profile.setHeadline(getTextValue(general, "headline"));
            profile.setLocation(getTextValue(general, "location"));
            profile.setSummary(getTextValue(general, "summary"));

            // 프로필 이미지 URL
            if (general.has("profileImgUrl")) {
                profile.setProfileImageUrl(general.get("profileImgUrl").asText());
            }
        } else {
            // 새로운 포맷 (general 필드가 없는 경우) 처리
            profile.setName(profileData.has("firstName") && profileData.has("lastName") ?
                    profileData.get("firstName").asText() + " " + profileData.get("lastName").asText() :
                    getTextValue(profileData, "name"));
            profile.setHeadline(getTextValue(profileData, "headline"));
            profile.setLocation(getTextValue(profileData, "geoLocationName"));
            profile.setSummary(getTextValue(profileData, "summary"));

            // 프로필 이미지 URL
            if (profileData.has("pictureUrl")) {
                profile.setProfileImageUrl(profileData.get("pictureUrl").asText());
            }
        }

        // 회사 정보 (최신 경력에서 가져옴)
        if (profileData.has("experience") && profileData.get("experience").isArray() && profileData.get("experience").size() > 0) {
            JsonNode latestExperience = profileData.get("experience").get(0);
            if (latestExperience.has("company")) {
                profile.setCompany(latestExperience.get("company").asText());
            } else if (latestExperience.has("companyName")) {
                profile.setCompany(latestExperience.get("companyName").asText());
            }
        } else if (profileData.has("positions") && profileData.get("positions").isArray() && profileData.get("positions").size() > 0) {
            // 새로운 포맷 (positions 필드가 있는 경우) 처리
            JsonNode latestPosition = profileData.get("positions").get(0);
            if (latestPosition.has("companyName")) {
                profile.setCompany(latestPosition.get("companyName").asText());
            } else if (latestPosition.has("company") && latestPosition.get("company").has("name")) {
                profile.setCompany(latestPosition.get("company").get("name").asText());
            }
        } else if (profileData.has("companyName")) {
            profile.setCompany(profileData.get("companyName").asText());
        }

        // 경력 정보 파싱
        List<LinkedInExperienceDTO> experiences = new ArrayList<>();
        if (profileData.has("experience") && profileData.get("experience").isArray()) {
            for (JsonNode exp : profileData.get("experience")) {
                LinkedInExperienceDTO experience = LinkedInExperienceDTO.builder()
                        .title(getTextValue(exp, "title"))
                        .company(getTextValue(exp, "company"))
                        .duration(getTextValue(exp, "dateRange"))
                        .description(getTextValue(exp, "description"))
                        .indate(currentDatetime)
                        .build();
                experiences.add(experience);
            }
        } else if (profileData.has("positions") && profileData.get("positions").isArray()) {
            // 새로운 포맷 (positions 필드가 있는 경우) 처리
            for (JsonNode pos : profileData.get("positions")) {
                String title = getTextValue(pos, "title");
                String company = pos.has("companyName") ? pos.get("companyName").asText() :
                        (pos.has("company") && pos.get("company").has("name") ?
                                pos.get("company").get("name").asText() : null);

                String duration = null;
                if (pos.has("timePeriod")) {
                    JsonNode timePeriod = pos.get("timePeriod");
                    String startDate = formatDate(timePeriod, "startDate");
                    String endDate = formatDate(timePeriod, "endDate");

                    if (startDate != null) {
                        duration = startDate + (endDate != null ? " - " + endDate : " - 현재");
                    }
                }

                String description = getTextValue(pos, "description");

                LinkedInExperienceDTO experience = LinkedInExperienceDTO.builder()
                        .title(title)
                        .company(company)
                        .duration(duration)
                        .description(description)
                        .indate(currentDatetime)
                        .build();
                experiences.add(experience);
            }
        }
        profile.setExperiences(experiences);

        // 학력 정보 파싱
        List<LinkedInEducationDTO> educations = new ArrayList<>();
        if (profileData.has("education") && profileData.get("education").isArray()) {
            for (JsonNode edu : profileData.get("education")) {
                LinkedInEducationDTO education = LinkedInEducationDTO.builder()
                        .school(getTextValue(edu, "school"))
                        .degree(getTextValue(edu, "degree"))
                        .fieldOfStudy(getTextValue(edu, "fieldOfStudy"))
                        .duration(getTextValue(edu, "dateRange"))
                        .indate(currentDatetime)
                        .build();
                educations.add(education);
            }
        } else if (profileData.has("educations") && profileData.get("educations").isArray()) {
            // 새로운 포맷 (educations 필드가 있는 경우) 처리
            for (JsonNode edu : profileData.get("educations")) {
                String school = getTextValue(edu, "schoolName");
                String degree = getTextValue(edu, "degreeName");
                String fieldOfStudy = getTextValue(edu, "fieldOfStudy");

                String duration = null;
                if (edu.has("timePeriod")) {
                    JsonNode timePeriod = edu.get("timePeriod");
                    String startDate = formatYearOnly(timePeriod, "startDate");
                    String endDate = formatYearOnly(timePeriod, "endDate");

                    if (startDate != null || endDate != null) {
                        duration = (startDate != null ? startDate : "") +
                                (endDate != null ? " - " + endDate : "");
                    }
                }

                LinkedInEducationDTO education = LinkedInEducationDTO.builder()
                        .school(school)
                        .degree(degree)
                        .fieldOfStudy(fieldOfStudy)
                        .duration(duration)
                        .indate(currentDatetime)
                        .build();
                educations.add(education);
            }
        }
        profile.setEducations(educations);

        // 스킬 정보 파싱
        List<LinkedInSkillDTO> skills = new ArrayList<>();
        if (profileData.has("skills") && profileData.get("skills").isArray()) {
            for (JsonNode skill : profileData.get("skills")) {
                LinkedInSkillDTO skillDTO = new LinkedInSkillDTO();
                skillDTO.setIndate(currentDatetime);

                if (skill.isTextual()) {
                    skillDTO.setSkillName(skill.asText());
                } else if (skill.has("name")) {
                    skillDTO.setSkillName(skill.get("name").asText());
                }

                if (skillDTO.getSkillName() != null && !skillDTO.getSkillName().isEmpty()) {
                    skills.add(skillDTO);
                }
            }
        }
        profile.setSkills(skills);

        // 기본 관련성 점수 설정 (실제로는 다른 로직으로 계산 가능)
        profile.setRelevance(0.5f);

        return profile;
    }

    private String getTextValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return null;
    }

    private String formatDate(JsonNode timePeriod, String dateName) {
        if (timePeriod != null && timePeriod.has(dateName)) {
            JsonNode date = timePeriod.get(dateName);
            if (date.has("month") && date.has("year")) {
                return date.get("month").asText() + "/" + date.get("year").asText();
            } else if (date.has("year")) {
                return date.get("year").asText();
            }
        }
        return null;
    }

    private String formatYearOnly(JsonNode timePeriod, String dateName) {
        if (timePeriod != null && timePeriod.has(dateName)) {
            JsonNode date = timePeriod.get(dateName);
            if (date.has("year")) {
                return date.get("year").asText();
            }
        }
        return null;
    }
}