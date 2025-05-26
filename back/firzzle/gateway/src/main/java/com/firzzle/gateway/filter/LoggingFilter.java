package com.firzzle.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @Class Name : LoggingFilter.java
 * @Description : 요청/응답 로깅을 위한 글로벌 필터
 * @author Firzzle
 * @since 2025. 5. 13.
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    // 개발 모드 설정 - true로 설정하면 더 상세한 로그 출력
    private static final boolean DEV_MODE = true;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getPath().toString();
        String requestMethod = request.getMethod().toString();
        String clientIp = getClientIp(request);

        // 요청 시작 시간 기록
        exchange.getAttributes().put("startTime", Instant.now());

        // 요청 로깅
        logger.info("요청 시작 - [{}] {}, IP: {}", requestMethod, requestPath, clientIp);

        if (DEV_MODE) {
            // 개발 모드일 때만 더 상세한 정보 로깅
            logger.info("요청 헤더: {}", request.getHeaders());
            logger.info("쿠키: {}", request.getCookies());  // 쿠키 로깅 추가
            logger.info("쿼리 파라미터: {}", request.getQueryParams());
        }

        // 필터 체인 실행 후 응답 로깅
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Instant startTime = exchange.getAttribute("startTime");
            long executionTime = 0;
            if (startTime != null) {
                executionTime = ChronoUnit.MILLIS.between(startTime, Instant.now());
            }

            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 0;

            // 응답 로깅
            logger.info("응답 완료 - [{}] {} - 상태 코드: {}, 처리 시간: {}ms, IP: {}",
                    requestMethod, requestPath, statusCode, executionTime, clientIp);

            if (DEV_MODE) {
                // 개발 모드일 때만 더 상세한 정보 로깅
                logger.info("응답 헤더: {}", exchange.getResponse().getHeaders());
                logger.info("응답 쿠키: {}", exchange.getResponse().getCookies());  // 응답 쿠키 로깅 추가
            }
        }));
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

    @Override
    public int getOrder() {
        // 가장 먼저 실행되는 필터로 설정 (음수값이 작을수록 먼저 실행)
        return -999;
    }
}