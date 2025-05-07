package com.firzzle.jwt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Class Name : JwtTokenUtil.java
 * @Description : JWT 토큰 관련 유틸리티 메서드를 제공하는 클래스
 * @author Firzzle
 * @since 2025. 5. 6.
 */
public class JwtTokenUtil {

    // 고정 상수값
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    public static String extractTokenFromAuthHeader(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 스코프 문자열을 List<String>으로 변환
     */
    public static List<String> convertScopeToList(String scope) {
        if (!StringUtils.hasText(scope)) {
            return Collections.emptyList();
        }
        return Arrays.stream(scope.split("[ ,]"))
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    /**
     * Claims에서 역할(role) 정보 추출
     */
    public static String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }

    /**
     * Claims에서 스코프(scope) 정보 추출
     */
    public static String extractScope(Claims claims) {
        return claims.get("scope", String.class);
    }

    /**
     * 토큰의 만료 시간이 특정 시간(초) 이내인지 확인
     */
    public static boolean isTokenExpiringSoon(Claims claims, long secondsThreshold) {
        long expirationTime = claims.getExpiration().getTime();
        long currentTime = System.currentTimeMillis();
        return (expirationTime - currentTime) < (secondsThreshold * 1000);
    }

    /**
     * 예외 메시지로부터 유용한 오류 정보 추출
     */
    public static String getReadableJwtError(JwtException e) {
        String message = e.getMessage();
        if (message.contains("expired")) {
            return "토큰이 만료되었습니다.";
        } else if (message.contains("signature")) {
            return "토큰 서명이 유효하지 않습니다.";
        } else if (message.contains("malformed")) {
            return "잘못된 형식의 토큰입니다.";
        }
        return "토큰 검증 실패: " + message;
    }

    /**
     * 토큰이 특정 역할을 가지고 있는지 확인
     */
    public static boolean hasRole(Claims claims, String role) {
        String userRole = extractRole(claims);
        return role.equals(userRole);
    }

    /**
     * 토큰이 특정 스코프를 가지고 있는지 확인
     */
    public static boolean hasScope(Claims claims, String requiredScope) {
        String scope = extractScope(claims);
        if (scope == null) {
            return false;
        }
        List<String> scopes = convertScopeToList(scope);
        return scopes.contains(requiredScope);
    }
}