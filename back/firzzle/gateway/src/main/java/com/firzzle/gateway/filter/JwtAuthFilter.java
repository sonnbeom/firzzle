package com.firzzle.gateway.filter;

import com.firzzle.jwt.config.JwtConfig;
import com.firzzle.jwt.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Objects;

/**
 * @Class Name : JwtAuthFilter.java
 * @Description : JWT 인증 필터 - API 게이트웨이에서 JWT 토큰 검증
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    // 개발 모드 플래그 추가 - false로 설정
    private static final boolean DEV_MODE = false;

    // 개발 환경에서 사용할 고정 사용자 정보
    private static final String DEV_UUID = "07f670f0-2853-11f0-aeb6-c68431894852";
    private static final String DEV_ROLE = "user";
    private static final String DEV_SCOPE = "content:read content:write";

    private final JwtConfig jwtConfig;

    public JwtAuthFilter(JwtConfig jwtConfig) {
        super(Config.class);
        this.jwtConfig = jwtConfig;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 필터가 비활성화된 경우 바로 다음 필터로 전달
            if (!config.isEnabled()) {
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();

            // 개발 모드인 경우
            if (DEV_MODE) {
                logger.debug("개발 모드로 실행 중 - 고정 사용자 정보 사용: UUID={}, ROLE={}", DEV_UUID, DEV_ROLE);

                // 개발용 사용자 정보로 헤더 설정
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-UUID", DEV_UUID)
                        .header("X-User-Role", DEV_ROLE)
                        .header("X-User-Scope", DEV_SCOPE)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }

            // Authorization 헤더 확인
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Authorization 헤더가 없습니다.", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String token = JwtTokenUtil.extractTokenFromAuthHeader(authHeader);

            if (token == null) {
                return onError(exchange, "유효한 JWT 토큰 형식이 아닙니다.", HttpStatus.UNAUTHORIZED);
            }

            try {
                // 토큰 검증 및 클레임 추출
                Claims claims = validateToken(token);

                // 어드민 전용 경로인 경우 역할 확인
                if (config.isAdminOnly()) {
                    String role = JwtTokenUtil.extractRole(claims);
                    if (!"admin".equalsIgnoreCase(role)) {
                        return onError(exchange, "관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN);
                    }
                }

                // 요청 경로에 따른 접근 권한 확인
                String servicePath = extractServicePath(request.getPath().toString());
                if (!hasAccessToService(claims, servicePath)) {
                    return onError(exchange, "해당 서비스에 접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
                }

                // 헤더에 사용자 정보 추가
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-UUID", claims.getSubject())
                        .header("X-User-Role", JwtTokenUtil.extractRole(claims))
                        .header("X-User-Scope", JwtTokenUtil.extractScope(claims))
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (JwtException e) {
                logger.error("JWT 토큰 검증 실패: {}", e.getMessage());
                return onError(exchange, JwtTokenUtil.getReadableJwtError(e), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * 요청 경로에서 서비스 이름 추출
     * @param path 요청 경로
     * @return 서비스 이름
     */
    private String extractServicePath(String path) {
        // 경로에서 서비스 이름 추출 (예: /api/v1/auth/ -> auth)
        if (path.startsWith("/api/")) {
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                return parts[3]; // 예: api/v1/auth/... -> auth
            }
        }
        return "";
    }

    /**
     * 토큰에 포함된 권한으로 서비스 접근 가능 여부 확인
     * @param claims JWT 클레임
     * @param servicePath 요청 서비스 경로
     * @return 접근 가능 여부
     */
    private boolean hasAccessToService(Claims claims, String servicePath) {
        // 클레임에서 대상 서비스(audience) 목록 추출
        Object audObj = claims.get("aud");
        List<String> audiences = null;

        if (audObj instanceof List) {
            audiences = (List<String>) audObj;
        }

        // audiences가 null이거나 비어있으면 접근 불가
        if (audiences == null || audiences.isEmpty()) {
            return false;
        }

        // 사용자가 해당 서비스에 접근 가능한지 확인
        return audiences.contains(servicePath);
    }

    /**
     * JWT 토큰 검증
     * @param token JWT 토큰
     * @return 검증된 클레임
     */
    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecretKeyBytes());

        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(jwtConfig.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 오류 응답 처리
     * @param exchange 서버 교환 객체
     * @param message 오류 메시지
     * @param status HTTP 상태 코드
     * @return Mono<Void>
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        logger.error(message);
        return response.setComplete();
    }

    /**
     * 필터 설정 클래스
     */
    @Data
    public static class Config {
        // 필터 활성화 여부
        private boolean enabled = true;
        // 관리자 전용 경로 여부
        private boolean adminOnly = false;
    }
}