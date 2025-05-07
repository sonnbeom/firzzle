package com.firzzle.jwt.provider;

import com.firzzle.jwt.config.JwtConfig;
import com.firzzle.jwt.model.JwtToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

/**
 * @Class Name : JwtTokenProvider.java
 * @Description : JWT 토큰 생성, 검증, 파싱 등의 기능을 제공하는 클래스
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtConfig jwtConfig;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * JwtConfig 객체 반환
     * @return JWT 설정 객체
     */
    public JwtConfig getJwtConfig() {
        return this.jwtConfig;
    }

    /**
     * 서명에 사용할 시크릿 키 생성
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecretKeyBytes());
    }

    /**
     * 역할에 따른 접근 가능 모듈 목록 반환
     * @param role 사용자 역할
     * @return 접근 가능한 모듈 목록
     */
    public List<String> getAudiencesByRole(String role) {
        // 유저별로 다른 audience도 가능
        switch (role.toLowerCase()) {
            case "admin":
                // 관리자는 모든 모듈에 접근 가능
                return Arrays.asList("auth", "gateway", "discovery", "learning", "llm", "admin", "main", "stt");
            case "business":
                // 비즈니스 사용자는 특정 비즈니스 관련 모듈에 접근 가능
                return Arrays.asList("auth", "gateway", "discovery", "learning", "llm", "main", "stt");
            case "user":
            default:
                // 일반 사용자는 기본 모듈에만 접근 가능
                return Arrays.asList("auth", "gateway", "discovery", "learning", "llm", "main", "stt");
        }
    }

    /**
     * 액세스 토큰 생성
     * @param uuid 사용자 UUID
     * @param role 사용자 역할
     * @param scopes 사용자 권한 범위 목록
     * @param audiences 접근 가능한 서비스 모듈 목록
     * @return JWT 액세스 토큰
     */
    public String generateAccessToken(String uuid, String role, List<String> scopes, List<String> audiences) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getAccessTokenValiditySeconds() * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("scope", String.join(" ", scopes));
        claims.put("aud", audiences);

        return Jwts.builder()
                .subject(uuid)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtConfig.getIssuer())
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 액세스 토큰 생성 (역할에 따른 기본 audiences 사용)
     * @param uuid 사용자 UUID
     * @param role 사용자 역할
     * @param scopes 사용자 권한 범위 목록
     * @return JWT 액세스 토큰
     */
    public String generateAccessToken(String uuid, String role, List<String> scopes) {
        List<String> audiences = getAudiencesByRole(role);
        return generateAccessToken(uuid, role, scopes, audiences);
    }

    /**
     * 리프레시 토큰 생성
     * @param uuid 사용자 UUID
     * @return JWT 리프레시 토큰
     */
    public String generateRefreshToken(String uuid) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getRefreshTokenValiditySeconds() * 1000);

        // 토큰 고유 식별자 생성
        String tokenId = UUID.randomUUID().toString();

        return Jwts.builder()
                .subject(uuid)
                .id(tokenId) // jti 클레임 추가
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtConfig.getIssuer())
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();

        // 참고: 실제 구현에서는 이 tokenId를 데이터베이스에 저장하여
        // 토큰 무효화 등에 활용할 수 있습니다.
    }

    /**
     * Claims에서 대상 서비스(audience) 정보를 Set으로 추출
     */
    public static Set<String> getAudienceSet(Claims claims) {
        Set<String> result = new HashSet<>();
        Object audObj = claims.get("aud");

        if (audObj == null) {
            return result;
        }

        if (audObj instanceof String) {
            result.add((String) audObj);
        } else if (audObj instanceof List) {
            for (Object item : (List<?>) audObj) {
                if (item instanceof String) {
                    result.add((String) item);
                }
            }
        }

        return result;
    }

    /**
     * Claims에서 대상 서비스(audience) 정보를 List로 변환하여 추출
     */
    public static List<String> getAudienceList(Claims claims) {
        return new ArrayList<>(getAudienceSet(claims));
    }

    /**
     * Claims에서 토큰 ID(jti) 추출
     */
    public static String extractTokenId(Claims claims) {
        return claims.getId();
    }

    /**
     * 액세스 토큰과 리프레시 토큰을 함께 생성
     * @param uuid 사용자 UUID
     * @param role 사용자 역할
     * @param scopes 사용자 권한 범위 목록
     * @return JWT 토큰 객체(액세스 토큰 + 리프레시 토큰)
     */
    public JwtToken createTokenPair(String uuid, String role, List<String> scopes) {
        List<String> audiences = getAudiencesByRole(role);
        String accessToken = generateAccessToken(uuid, role, scopes, audiences);
        String refreshToken = generateRefreshToken(uuid);
        return new JwtToken(accessToken, refreshToken, jwtConfig.getAccessTokenValiditySeconds());
    }

    /**
     * 토큰 검증 및 클레임 추출
     * @param token JWT 토큰
     * @return 토큰의 클레임
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(jwtConfig.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            logger.error("JWT 토큰 검증 실패: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 토큰에서 사용자 UUID 추출
     * @param token JWT 토큰
     * @return 사용자 UUID
     */
    public String getUuidFromToken(String token) {
        Claims claims = validateAndGetClaims(token);
        return claims.getSubject();
    }

    /**
     * 토큰 유효성 검증
     * @param token JWT 토큰
     * @return 토큰 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(jwtConfig.getIssuer())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            logger.warn("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰 생성
     * @param refreshToken 리프레시 토큰
     * @param role 사용자 역할
     * @param scopes 사용자 권한 범위 목록
     * @return 새로운 액세스 토큰
     * @throws JwtException 리프레시 토큰이 유효하지 않은 경우
     */
    public String refreshAccessToken(String refreshToken, String role, List<String> scopes) {
        try {
            String uuid = getUuidFromToken(refreshToken);
            List<String> audiences = getAudiencesByRole(role);
            return generateAccessToken(uuid, role, scopes, audiences);
        } catch (JwtException e) {
            logger.error("리프레시 토큰으로 액세스 토큰 갱신 실패: {}", e.getMessage());
            throw e;
        }
    }
}