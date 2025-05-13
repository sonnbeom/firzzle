package com.firzzle.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firzzle.gateway.response.Response;
import com.firzzle.gateway.response.Status;
import com.firzzle.jwt.config.JwtConfig;
import com.firzzle.jwt.provider.JwtTokenProvider;
import com.firzzle.jwt.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @Class Name : JwtAuthFilter.java
 * @Description : JWT 인증 필터 - API 게이트웨이에서 JWT 토큰 검증
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    // 개발 모드 플래그 - true로 설정하면 JWT 검증 없이 개발용 사용자 정보 사용
    private static final boolean DEV_MODE = true;

    // 개발 환경에서 사용할 고정 사용자 정보
    private static final String DEV_UUID = "07f670f0-2853-11f0-aeb6-c68431894852";
    private static final String DEV_ROLE = "user";
    private static final String DEV_SCOPE = "content:read content:write";

    private final JwtConfig jwtConfig;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthFilter(JwtConfig jwtConfig, JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtConfig = jwtConfig;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 필터가 비활성화된 경우 바로 다음 필터로 전달
            if (!config.isEnabled()) {
                logger.info("JwtAuthFilter 비활성화됨 - 인증 검사 없이 요청 통과");
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getPath().toString();
            String requestMethod = request.getMethod().toString();
            String clientIp = getClientIp(request);

            logger.info("JWT 인증 시작 - 경로: [{}] {}, IP: {}", requestMethod, requestPath, clientIp);

            // 개발 모드인 경우
            if (DEV_MODE) {
                logger.info("개발 모드로 실행 중 - 고정 사용자 정보 사용: UUID={}, 역할={}, IP={}",
                        DEV_UUID, DEV_ROLE, clientIp);

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
                logger.info("인증 실패 - Authorization 헤더 없음: [{}] {}, IP: {}",
                        requestMethod, requestPath, clientIp);
                return onError(exchange, "Authorization 헤더가 없습니다.", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String token = JwtTokenUtil.extractTokenFromAuthHeader(authHeader);

            if (token == null) {
                logger.info("인증 실패 - 유효하지 않은 JWT 토큰 형식: [{}] {}, IP: {}",
                        requestMethod, requestPath, clientIp);
                return onError(exchange, "유효한 JWT 토큰 형식이 아닙니다.", HttpStatus.UNAUTHORIZED);
            }

            try {
                // 토큰의 첫 10자리만 로깅 (보안상의 이유로 전체 토큰은 로깅하지 않음)
                String tokenPrefix = token.length() > 10 ? token.substring(0, 10) + "..." : token;
                logger.info("JWT 토큰 검증 중 - 토큰: {}, 경로: [{}] {}",
                        tokenPrefix, requestMethod, requestPath);

                // JwtTokenProvider를 사용하여 토큰 검증 및 클레임 추출
                Claims claims = jwtTokenProvider.validateAndGetClaims(token);
                String uuid = claims.getSubject();
                String role = JwtTokenUtil.extractRole(claims);
                String scope = JwtTokenUtil.extractScope(claims);

                logger.info("JWT 토큰 검증 성공 - 사용자: {}, 역할: {}, 스코프: {}", uuid, role, scope);

                // 어드민 전용 경로인 경우 역할 확인
                if (config.isAdminOnly()) {
                    logger.info("관리자 전용 경로 접근 확인 중 - 경로: {}, 역할: {}", requestPath, role);

                    if (!"admin".equalsIgnoreCase(role)) {
                        logger.info("관리자 권한 접근 거부 - 사용자: {}, 역할: {}, 경로: {}, IP: {}",
                                uuid, role, requestPath, clientIp);
                        return onError(exchange, "관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN);
                    }

                    logger.info("관리자 권한 접근 허용 - 사용자: {}, 역할: {}, 경로: {}",
                            uuid, role, requestPath);
                }

                // 요청 경로에 따른 접근 권한 확인
                String servicePath = extractServicePath(requestPath);
                logger.info("서비스 경로 접근 확인 중 - 경로: {}, 서비스: {}", requestPath, servicePath);

                if (!hasAccessToService(claims, servicePath)) {
                    logger.info("서비스 접근 권한 없음 - 사용자: {}, 역할: {}, 서비스: {}, IP: {}",
                            uuid, role, servicePath, clientIp);
                    return onError(exchange, "해당 서비스에 접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
                }

                // 헤더에 사용자 정보 추가
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-UUID", uuid)
                        .header("X-User-Role", role)
                        .header("X-User-Scope", scope)
                        .build();

                logger.info("인증 성공 - 사용자: {}, 역할: {}, 경로: [{}] {}",
                        uuid, role, requestMethod, requestPath);

                // 토큰 만료 임박 확인 (선택적 기능)
                if (JwtTokenUtil.isTokenExpiringSoon(claims, 300)) { // 5분(300초) 이내
                    logger.info("토큰 만료 임박 - 사용자: {}, 남은 시간: {}초",
                            uuid, getTokenRemainingTime(claims));
                }

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (JwtException e) {
                logger.info("JWT 토큰 검증 실패 - 오류: {}, 경로: [{}] {}, IP: {}",
                        e.getMessage(), requestMethod, requestPath, clientIp);
                return onError(exchange, JwtTokenUtil.getReadableJwtError(e), HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                logger.info("JWT 처리 중 예외 발생 - 오류: {}, 경로: [{}] {}, IP: {}",
                        e.getMessage(), requestMethod, requestPath, clientIp);
                return onError(exchange, "인증 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
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
        // JwtTokenProvider에서 audience 리스트 가져오기
        List<String> audiences = JwtTokenProvider.getAudienceList(claims);

        // audiences가 null이거나 비어있으면 접근 불가
        if (audiences == null || audiences.isEmpty()) {
            return false;
        }

        // 사용자가 해당 서비스에 접근 가능한지 확인
        return audiences.contains(servicePath);
    }

    /**
     * 토큰 남은 유효 시간 계산 (초 단위)
     */
    private long getTokenRemainingTime(Claims claims) {
        long expirationTime = claims.getExpiration().getTime();
        long currentTime = System.currentTimeMillis();
        return Math.max(0, (expirationTime - currentTime) / 1000);
    }

    /**
     * 클라이언트 IP 주소 가져오기
     */
    private String getClientIp(ServerHttpRequest request) {
        String clientIp = request.getHeaders().getFirst("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeaders().getFirst("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeaders().getFirst("HTTP_CLIENT_IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeaders().getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddress() != null ? request.getRemoteAddress().toString() : "unknown";
        }
        // X-Forwarded-For 헤더에 여러 IP가 있는 경우 첫 번째 IP만 사용
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }
        return clientIp;
    }

    /**
     * 오류 응답 처리 - 로깅 강화
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 로깅 레벨 상황에 맞게 조정 - 모두 INFO로 변경
        if (status == HttpStatus.UNAUTHORIZED) {
            logger.info("인증 오류 응답: {}, 상태: {}", message, status);
        } else if (status == HttpStatus.FORBIDDEN) {
            logger.info("접근 거부 응답: {}, 상태: {}", message, status);
        } else {
            logger.info("서버 오류 응답: {}, 상태: {}", message, status);
        }

        // Response 객체 생성 (GlobalExceptionHandler와 동일한 형식)
        Response<Void> responseBody = Response.<Void>builder()
                .status(Status.FAIL)
                .message(message)
                .build();

        // JSON으로 변환 (Jackson 사용)
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] responseBytes = objectMapper.writeValueAsBytes(responseBody);
            DataBuffer buffer = response.bufferFactory().wrap(responseBytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            logger.info("JSON 변환 중 오류 발생: {}", e.getMessage());
            return response.setComplete();
        }
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