package com.firzzle.gateway.filter;

import com.firzzle.jwt.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * @Class Name : JwtAuthenticationFilter.java
 * @Description : JWT 인증 필터 클래스 - 모든 요청에 대해 JWT 토큰을 검증하고 사용자 정보를 헤더에 추가
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // 개발 모드 플래그 - 필요에 따라 변경 가능
    private static final boolean DEV_MODE = true;

    // 개발 환경에서 사용할 고정 사용자 정보
    private static final String DEV_UUID = "07f670f0-2853-11f0-aeb6-c68431894852";
    private static final String DEV_ROLE = "user";
    private static final String DEV_SCOPE = "content:read content:write";

    private final JwtTokenProvider jwtTokenProvider;

    // 인증이 필요하지 않은 URL 패턴 목록
    private final List<String> allowedUrls = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/**",
            "/api/v1/share/*"
    );

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        logger.debug("요청 경로: {}", path);

        // 인증이 필요하지 않은 URL은 통과
        if (isAllowedPath(path)) {
            logger.debug("인증이 필요 없는 경로입니다: {}", path);
            return chain.filter(exchange);
        }

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

        // 프로덕션 모드: Authorization 헤더에서 토큰 추출
        String token = extractToken(request);

        if (token == null) {
            logger.warn("토큰이 존재하지 않음: {}", path);
            return unauthorizedResponse(exchange);
        }

        try {
            if (jwtTokenProvider.validateToken(token)) {
                // 토큰에서 정보 추출 및 헤더 추가
                Claims claims = jwtTokenProvider.validateAndGetClaims(token);
                String uuid = claims.getSubject();
                String role = claims.get("role", String.class);
                String scope = claims.get("scope", String.class);

                logger.debug("인증 성공 - 사용자: {}, 역할: {}, 범위: {}", uuid, role, scope);

                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-UUID", uuid)
                        .header("X-User-Role", role)
                        .header("X-User-Scope", scope)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } else {
                logger.warn("토큰 검증 실패: {}", path);
                return unauthorizedResponse(exchange);
            }
        } catch (Exception e) {
            logger.error("토큰 처리 중 오류 발생: {}", e.getMessage(), e);
            return unauthorizedResponse(exchange);
        }
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        logger.debug("401 Unauthorized 응답 반환");
        return response.setComplete();
    }

    private boolean isAllowedPath(String path) {
        boolean allowed = allowedUrls.stream().anyMatch(path::startsWith);
        if (allowed) {
            logger.debug("허용된 경로: {}", path);
        } else {
            logger.debug("인증이 필요한 경로: {}", path);
        }
        return allowed;
    }

    private String extractToken(ServerHttpRequest request) {
        List<String> authHeader = request.getHeaders().get("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            String auth = authHeader.get(0);
            if (auth.startsWith("Bearer ")) {
                logger.debug("Bearer 토큰 추출 성공");
                return auth.substring(7);
            } else {
                logger.warn("Authorization 헤더 형식이 올바르지 않음: {}", auth);
            }
        } else {
            logger.debug("Authorization 헤더가 없음");
        }
        return null;
    }

    @Override
    public int getOrder() {
        return -100; // 높은 우선순위로 실행
    }
}