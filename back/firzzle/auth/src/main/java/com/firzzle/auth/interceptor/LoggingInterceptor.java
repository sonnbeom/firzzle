package com.firzzle.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @Class Name : LoggingInterceptor.java
 * @Description : 요청/응답 로깅을 위한 인터셉터
 * @author Firzzle
 * @since 2025. 5. 13.
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    // 개발 모드 설정 - true로 설정하면 더 상세한 로그 출력
    private static final boolean DEV_MODE = true;

    private static final String REQUEST_START_TIME = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute(REQUEST_START_TIME, startTime);

        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();
        String clientIp = getClientIp(request);

        // Gateway에서 전달한 사용자 정보 확인
        String userUuid = request.getHeader("X-User-UUID");
        String userRole = request.getHeader("X-User-Role");

        if (userUuid != null && !userUuid.isEmpty()) {
            // 사용자 정보가 있을 경우 로깅에 포함
            logger.info("요청 시작 - [{}] {} - 사용자: {}, 역할: {}, IP: {}",
                    requestMethod, requestUri, userUuid, userRole, clientIp);
        } else {
            // 사용자 정보가 없을 경우 (공개 API 등)
            logger.info("요청 시작 - [{}] {} - 공개 요청, IP: {}",
                    requestMethod, requestUri, clientIp);
        }

        if (DEV_MODE) {
            // 개발 모드일 때만 더 상세한 정보 로깅
            Map<String, String> headers = getRequestHeaders(request);
            Map<String, String[]> parameters = request.getParameterMap();

            logger.info("요청 헤더: {}", headers);

            if (!parameters.isEmpty()) {
                logger.info("요청 파라미터: {}", parameters);
            }
        }

        // 인터셉터 계속 진행
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 요청 처리 완전히 끝난 후 호출
        long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();
        int statusCode = response.getStatus();
        String clientIp = getClientIp(request);

        // Gateway에서 전달한 사용자 정보 확인
        String userUuid = request.getHeader("X-User-UUID");

        if (userUuid != null && !userUuid.isEmpty()) {
            // 사용자 정보가 있을 경우 로깅에 포함
            logger.info("응답 완료 - [{}] {} - 사용자: {}, 상태 코드: {}, 처리 시간: {}ms, IP: {}",
                    requestMethod, requestUri, userUuid, statusCode, executionTime, clientIp);
        } else {
            // 사용자 정보가 없을 경우 (공개 API 등)
            logger.info("응답 완료 - [{}] {} - 공개 요청, 상태 코드: {}, 처리 시간: {}ms, IP: {}",
                    requestMethod, requestUri, statusCode, executionTime, clientIp);
        }

        if (DEV_MODE) {
            // 개발 모드일 때만 더 상세한 정보 로깅
            Map<String, String> responseHeaders = getResponseHeaders(response);
            logger.info("응답 헤더: {}", responseHeaders);
        }

        if (ex != null) {
            // 예외 발생 시 로깅
            logger.info("요청 처리 중 예외 발생 - [{}] {}, 오류: {}",
                    requestMethod, requestUri, ex.getMessage());
        }
    }

    /**
     * 클라이언트 IP 주소 가져오기
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_CLIENT_IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        // X-Forwarded-For 헤더에 여러 IP가 있는 경우 첫 번째 IP만 사용
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }
        return clientIp;
    }

    /**
     * 요청 헤더 가져오기
     */
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);

                // 보안 관련 헤더 값은 마스킹 처리
                if (headerName.toLowerCase().contains("authorization")) {
                    headerValue = headerValue.length() > 10 ?
                            headerValue.substring(0, 10) + "..." : headerValue;
                }

                headerMap.put(headerName, headerValue);
            }
        }

        return headerMap;
    }

    /**
     * 응답 헤더 가져오기
     */
    private Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headerMap = new HashMap<>();
        Collection<String> headerNames = response.getHeaderNames();

        for (String headerName : headerNames) {
            String headerValue = response.getHeader(headerName);
            headerMap.put(headerName, headerValue);
        }

        return headerMap;
    }
}