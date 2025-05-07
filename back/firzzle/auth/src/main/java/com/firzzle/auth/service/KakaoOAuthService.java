package com.firzzle.auth.service;

import com.firzzle.auth.config.KakaoOAuthConfig;
import com.firzzle.auth.dao.UserDAO;
import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Class Name : KakaoOAuthService.java
 * @Description : Kakao OAuth 관련 서비스
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private static final Logger logger = LoggerFactory.getLogger(KakaoOAuthService.class);

    private final UserDAO userDAO;
    private final RestTemplate restTemplate;
    private final KakaoOAuthConfig kakaoOAuthConfig;

    // OAuthRedirectService 주입 추가
    private final OAuthRedirectService oAuthRedirectService;

    /**
     * 사용자 역할에 따른 기본 범위 설정
     * 개인 별로 지정 가능
     */
    public List<String> getDefaultScopes(String role) {
        // 기존 코드 유지
        if ("admin".equalsIgnoreCase(role)) {
            return List.of("content:read", "content:write", "user:read", "user:write", "admin:read", "admin:write");
        } else if ("business".equalsIgnoreCase(role)) {
            return List.of("content:read", "content:write", "user:read", "user:write");
        } else {
            return List.of("content:read", "content:write", "user:read", "user:write");
        }
    }

    /**
     * 인증 코드로 카카오 액세스 토큰 요청
     * 고정 URI
     */
    /*
    public String getKakaoAccessToken(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoOAuthConfig.getRegistration().getKakao().getClientId());
            params.add("client_secret", kakaoOAuthConfig.getRegistration().getKakao().getClientSecret());
            params.add("redirect_uri", kakaoOAuthConfig.getRegistration().getKakao().getRedirectUri());
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    kakaoOAuthConfig.getProvider().getKakao().getTokenUri(),
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            return (String) response.getBody().get("access_token");

        } catch (Exception e) {
            logger.error("카카오 액세스 토큰 요청 중 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "카카오 인증 서버 요청 중 오류가 발생했습니다.");
        }
    }
    */

    /**
     * 인증 코드로 카카오 액세스 토큰 요청
     * 동적 리다이렉트 URI
     */
    public String getKakaoAccessToken(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 동적으로 리다이렉트 URI 결정
            String redirectUri = oAuthRedirectService.determineClientRedirectUri();
            logger.info("카카오 액세스 토큰 요청 - 동적 리다이렉트 URI: {}", redirectUri);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoOAuthConfig.getRegistration().getKakao().getClientId());
//            params.add("client_secret", kakaoOAuthConfig.getRegistration().getKakao().getClientSecret());
            params.add("redirect_uri", "https://firzzle.site/api/v1/auth/kakao/callback"); // 백엔드 URI 설정
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    kakaoOAuthConfig.getProvider().getKakao().getTokenUri(),
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            String accessToken = response.getBody().get("access_token").toString();
            logger.debug("accessToken : {}", accessToken);
            return accessToken;

        } catch (Exception e) {
            logger.error("카카오 액세스 토큰 요청 중 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "카카오 인증 서버 요청 중 오류가 발생했습니다.");
        }
    }

    /**
     * 카카오 API로 사용자 정보 요청
     */
    public RequestBox getKakaoUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // 요청 전 로깅 추가
            logger.debug("카카오 사용자 정보 요청 - URI: {}, 토큰: {}",
                    kakaoOAuthConfig.getProvider().getKakao().getUserInfoUri(),
                    accessToken.substring(0, 10) + "...");

            ResponseEntity<Map> response = restTemplate.exchange(
                    kakaoOAuthConfig.getProvider().getKakao().getUserInfoUri(),
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            // 응답 디버깅
            logger.debug("카카오 사용자 정보 응답: {}", response.getBody());

            Map<String, Object> body = response.getBody();
            // null 체크 추가
            if (body == null) {
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "카카오 API에서 빈 응답을 반환했습니다.");
            }

            Long id = Long.valueOf(body.get("id").toString());

            // null 체크 추가
            Map<String, Object> properties = (body.get("properties") != null) ?
                    (Map<String, Object>) body.get("properties") : new HashMap<>();

            Map<String, Object> kakaoAccount = (body.get("kakao_account") != null) ?
                    (Map<String, Object>) body.get("kakao_account") : new HashMap<>();

            String nickname = properties.get("nickname") != null ? (String) properties.get("nickname") : "사용자";
            String email = kakaoAccount.get("email") != null ? (String) kakaoAccount.get("email") : null;
            String profileImageUrl = properties.get("profile_image") != null ?
                    (String) properties.get("profile_image") : null;

            if (email == null) {
//                throw new BusinessException(ErrorCode.MISSING_OAUTH_INFO, "카카오 계정에 이메일 정보가 없습니다.");
                email = "test@test.com";
            }

            if (profileImageUrl == null) {
//                throw new BusinessException(ErrorCode.MISSING_OAUTH_INFO, "카카오 계정에 프로필 정보가 없습니다.");
                profileImageUrl = "https://picsum.photos/250/250";
            }

            // RequestBox 대신 HashMap 사용하여 NPE 회피
            RequestBox userInfo = new RequestBox("requestbox");
            // 여기서 NPE 발생 가능성 있음 - 모든 값 null 체크 후 넣기
            userInfo.put("id", id != null ? id.toString() : "");
            userInfo.put("nickname", nickname != null ? nickname : "사용자");
            userInfo.put("email", email);
            userInfo.put("profile_image_url", profileImageUrl);

            return userInfo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("카카오 사용자 정보 요청 중 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "카카오 API 호출 중 오류가 발생했습니다.");
        }
    }

    /**
     * 새 사용자 등록
     */
    public DataBox registerNewUser(RequestBox kakaoUserInfo) throws Exception {
        // 기존 코드 유지
        String uuid = UUID.randomUUID().toString();
        String username = "kakao_" + kakaoUserInfo.getString("id"); // 카카오 ID로 고유한 사용자명 생성
        String now = FormatDate.getDate("yyyyMMddHHmmss");
        String randomPassword = UUID.randomUUID().toString();


        RequestBox newUser = new RequestBox("requestbox");
        newUser.put("uuid", uuid);
        newUser.put("username", username);
        newUser.put("password", randomPassword);
        newUser.put("email", kakaoUserInfo.getString("email"));
        newUser.put("name", kakaoUserInfo.getString("nickname"));
        newUser.put("profile_image_url", kakaoUserInfo.getString("profile_image_url"));
        newUser.put("role", "user"); // 기본 역할
        newUser.put("provider_code", "KAKAO");
        newUser.put("signup_type", "O"); // OAuth 회원가입
        newUser.put("indate", now);
        newUser.put("ldate", now);
        newUser.put("last_login", now);

        userDAO.insertUser(newUser);

        RequestBox emailBox = new RequestBox("requestbox");
        emailBox.put("email", kakaoUserInfo.getString("email"));
        return userDAO.selectUserByEmail(emailBox);
    }
}