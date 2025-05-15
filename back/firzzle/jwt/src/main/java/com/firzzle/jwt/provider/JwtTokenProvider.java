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

        // 디버깅: 객체 타입 확인
        logger.info("Audience object type: {}", (audObj != null ? audObj.getClass().getName() : "null"));

        if (audObj == null) {
            return result;
        }

        // 문자열인 경우
        if (audObj instanceof String) {
            result.add((String) audObj);
        }
        // 컬렉션 타입인 경우 (ArrayList, LinkedList 등의 부모 클래스)
        else if (audObj instanceof Collection) {
            for (Object item : (Collection<?>) audObj) {
                if (item instanceof String) {
                    result.add((String) item);
                } else {
                    // 타입이 String이 아닌 경우 toString()을 시도
                    result.add(String.valueOf(item));
                }
            }
        }
        // 배열인 경우
        else if (audObj.getClass().isArray()) {
            Object[] array = (Object[]) audObj;
            for (Object item : array) {
                if (item instanceof String) {
                    result.add((String) item);
                } else {
                    // 타입이 String이 아닌 경우 toString()을 시도
                    result.add(String.valueOf(item));
                }
            }
        }
        // 기타 단일 객체인 경우 toString으로 추가
        else {
            result.add(String.valueOf(audObj));
        }

        // 디버깅: 결과 확인
        logger.info("Extracted audience set: {}", result);

        return result;
    }

    /**
     * 사용자 역할에 따른 기본 권한 범위 설정
     * @param role 사용자 역할
     * @return 권한 범위 목록
     */
    public List<String> getDefaultScopes(String role) {
        if ("admin".equalsIgnoreCase(role)) {
            return List.of(
                    // 기본 권한
                    "content:read", "content:write",
                    "user:read", "user:write",
                    "admin:read", "admin:write",

                    // Auth 서비스 권한
                    "auth:read", "auth:write", "auth:admin",

                    // Learning 서비스 권한
                    "learning:read", "learning:write", "learning:admin",
                    "learning:create", "learning:delete", "learning:update",

                    // Main 서비스 권한
                    "main:read", "main:write", "main:admin",
                    "main:create", "main:delete", "main:update",

                    // LLM 서비스 권한
                    "llm:read", "llm:write", "llm:admin",
                    "llm:create", "llm:generate", "llm:configure",

                    // Admin 서비스 권한
                    "admin:users", "admin:reports", "admin:configuration",
                    "admin:system", "admin:billing", "admin:analytics",

                    // STT 서비스 권한
                    "stt:read", "stt:write", "stt:admin",
                    "stt:transcribe", "stt:configure"
            );
        } else if ("business".equalsIgnoreCase(role)) {
            return List.of(
                    // 기본 권한
                    "content:read", "content:write",
                    "user:read", "user:write",

                    // Auth 서비스 권한
                    "auth:read",

                    // Learning 서비스 권한
                    "learning:read", "learning:write",
                    "learning:create", "learning:update",

                    // Main 서비스 권한
                    "main:read", "main:write",
                    "main:create", "main:update",

                    // LLM 서비스 권한
                    "llm:read", "llm:write",
                    "llm:generate", "llm:configure",

                    // STT 서비스 권한
                    "stt:read", "stt:transcribe"
            );
        } else {
            return List.of(
                    // 기본 권한
                    "content:read", "content:write",
                    "user:read",

                    // Auth 서비스 권한
                    "auth:read",

                    // Learning 서비스 권한
                    "learning:read",
                    "learning:create",

                    // Main 서비스 권한
                    "main:read",

                    // LLM 서비스 권한
                    "llm:read",
                    "llm:generate",

                    // STT 서비스 권한
                    "stt:read", "stt:transcribe"
            );
        }
    }

    /**
     * Claims에서 대상 서비스(audience) 정보를 List로 변환하여 추출
     */
    public static List<String> getAudienceList(Claims claims) {
        List<String> result = new ArrayList<>(getAudienceSet(claims));
        logger.info("Audience list size: {}", result.size());
        return result;
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