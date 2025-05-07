package com.firzzle.jwt.model;

/**
 * @Class Name : JwtToken.java
 * @Description : JWT 토큰 정보를 담는 모델 클래스
 * @author Firzzle
 * @since 2025. 5. 6.
 */
public class JwtToken {
    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;

    public JwtToken(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}