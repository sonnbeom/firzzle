package com.firzzle.jwt.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity-seconds:3600}") // 기본값 1시간
    private long accessTokenValiditySeconds;

    @Value("${jwt.refresh-token-validity-seconds:604800}") // 기본값 7일
    private long refreshTokenValiditySeconds;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes); // 최소 256비트 이상이어야 함
    }

    public String generateAccessToken(String uuid, String role, List<String> scopes) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValiditySeconds * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("scope", String.join(" ", scopes));

        return Jwts.builder()
                .subject(uuid)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String uuid) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValiditySeconds * 1000);

        return Jwts.builder()
                .subject(uuid)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer("firzzle.site")
                .keyLocator(header -> getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUuidFromToken(String token) {
        Claims claims = validateAndGetClaims(token);
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .keyLocator(header -> getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}