package com.firzzle.gateway.filter;

import com.firzzle.jwt.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
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

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;

    // 인증이 필요하지 않은 URL 패턴 목록
    private final List<String> allowedUrls = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/share/*"
    );

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // 인증이 필요하지 않은 URL은 통과
        if (isAllowedPath(path)) {
            return chain.filter(exchange);
        }

        // Authorization 헤더에서 토큰 추출
        String token = extractToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰에서 정보 추출 및 헤더 추가
            Claims claims = jwtTokenProvider.validateAndGetClaims(token);
            String uuid = claims.getSubject();
            String role = claims.get("role", String.class);
            String scope = claims.get("scope", String.class);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-UUID", uuid)
                    .header("X-User-Role", role)
                    .header("X-User-Scope", scope)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        // 인증 실패 시 401 Unauthorized 응답
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        return response.setComplete();
    }

    private boolean isAllowedPath(String path) {
        return allowedUrls.stream().anyMatch(path::startsWith);
    }

    private String extractToken(ServerHttpRequest request) {
        List<String> authHeader = request.getHeaders().get("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            String auth = authHeader.get(0);
            if (auth.startsWith("Bearer ")) {
                return auth.substring(7);
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return -100; // 높은 우선순위로 실행
    }
}