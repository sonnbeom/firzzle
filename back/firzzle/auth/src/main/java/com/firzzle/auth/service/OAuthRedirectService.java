package com.firzzle.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Class Name : OAuthRedirectService.java
 * @Description : OAuth 클라이언트 리다이렉트 URI 처리 서비스
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Service
@RequiredArgsConstructor
public class OAuthRedirectService {

    private static final Logger logger = LoggerFactory.getLogger(OAuthRedirectService.class);

    private final HttpServletRequest request;

    @Value("${oauth2.client-local-url:https://localhost:3000}")
    private String clientLocalUrl;

    @Value("${oauth2.client-api-url:https://firzzle.site}")
    private String clientApiUrl;

    /**
     * 클라이언트 리다이렉트 URI 결정
     * 클라이언트 요청 Origin 헤더를 기반으로 판단
     */
    public String determineClientRedirectUri() {
        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        String clientIp = getClientIp(request);

        logger.info("Origin: {}, Referer: {}, Client IP: {}", origin, referer, clientIp);

        // Origin 또는 Referer에서 localhost 확인
        boolean isLocalRequest = (origin != null && origin.contains("localhost")) ||
                (referer != null && referer.contains("localhost")) ||
                "127.0.0.1".equals(clientIp) ||
                "0:0:0:0:0:0:0:1".equals(clientIp);

        if (isLocalRequest) {
            String redirectUri = clientLocalUrl + "/auth/callback";
            logger.info("로컬 환경 감지 - 클라이언트 리다이렉트 URI: {}", redirectUri);
            return redirectUri;
        } else {
            String redirectUri = clientApiUrl + "/auth/callback";
            logger.info("배포 환경 감지 - 클라이언트 리다이렉트 URI: {}", redirectUri);
            return redirectUri;
        }
    }

    /**
     * 클라이언트 IP 주소 가져오기
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_CLIENT_IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}