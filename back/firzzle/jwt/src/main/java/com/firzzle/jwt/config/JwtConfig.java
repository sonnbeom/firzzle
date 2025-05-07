package com.firzzle.jwt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.nio.charset.StandardCharsets;

/**
 * @Class Name : JwtConfig.java
 * @Description : JWT 관련 설정을 관리하는 클래스
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity-seconds:3600}") // 기본값 1시간
    private long accessTokenValiditySeconds;

    @Value("${jwt.refresh-token-validity-seconds:604800}") // 기본값 7일
    private long refreshTokenValiditySeconds;

    @Value("${jwt.issuer:firzzle.site}")
    private String issuer;

    // 고정 상수값
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";

    /**
     * JWT 시크릿 키 반환
     */
    public String getSecret() {
        return secret;
    }

    /**
     * JWT 시크릿 키를 바이트 배열로 반환
     */
    public byte[] getSecretKeyBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 액세스 토큰 유효 기간(초) 반환
     */
    public long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    /**
     * 리프레시 토큰 유효 기간(초) 반환
     */
    public long getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    /**
     * JWT 발급자(issuer) 반환
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * 토큰 접두사(예: "Bearer ") 반환
     */
    public String getTokenPrefix() {
        return TOKEN_PREFIX;
    }

    /**
     * 인증 헤더 이름(예: "Authorization") 반환
     */
    public String getHeaderName() {
        return HEADER_NAME;
    }
}