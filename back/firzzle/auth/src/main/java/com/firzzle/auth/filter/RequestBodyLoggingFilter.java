package com.firzzle.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Class Name : RequestBodyLoggingFilter.java
 * @Description : 요청/응답 본문 로깅을 위한 필터
 * @author Firzzle
 * @since 2025. 5. 13.
 */
@Component
public class RequestBodyLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestBodyLoggingFilter.class);

    // 개발 모드 설정 - true로 설정하면 요청/응답 본문 로깅
    private static final boolean DEV_MODE = true;

    // 본문 로깅 최대 길이 (너무 큰 본문은 잘라서 로깅)
    private static final int MAX_PAYLOAD_LENGTH = 10000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 개발 모드가 아니면 필터를 바로 통과
        if (!DEV_MODE) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청과 응답을 캐싱하여 로그를 남기기 위한 래퍼
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // 실제 필터 체인 실행 (여기서 실제 컨트롤러 메소드가 호출됨)
        filterChain.doFilter(requestWrapper, responseWrapper);

        // 요청 경로와 메소드
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();

        // Gateway에서 전달한 사용자 정보 확인
        String userUuid = request.getHeader("X-User-UUID");

        // 요청 본문 로깅 (POST, PUT 등의 요청만)
        if ("POST".equals(requestMethod) || "PUT".equals(requestMethod) || "PATCH".equals(requestMethod)) {
            byte[] content = requestWrapper.getContentAsByteArray();
            if (content.length > 0) {
                String contentAsString = new String(content, StandardCharsets.UTF_8);

                // 긴 본문은 자르기
                if (contentAsString.length() > MAX_PAYLOAD_LENGTH) {
                    contentAsString = contentAsString.substring(0, MAX_PAYLOAD_LENGTH) + "... (생략됨)";
                }

                if (userUuid != null && !userUuid.isEmpty()) {
                    logger.info("요청 본문 - [{}] {} - 사용자: {}: {}",
                            requestMethod, requestUri, userUuid, contentAsString);
                } else {
                    logger.info("요청 본문 - [{}] {} - 공개 요청: {}",
                            requestMethod, requestUri, contentAsString);
                }
            }
        }

        // 응답 본문 로깅
        byte[] responseContent = responseWrapper.getContentAsByteArray();
        if (responseContent.length > 0) {
            String responseContentAsString = new String(responseContent, StandardCharsets.UTF_8);

            // 긴 본문은 자르기
            if (responseContentAsString.length() > MAX_PAYLOAD_LENGTH) {
                responseContentAsString = responseContentAsString.substring(0, MAX_PAYLOAD_LENGTH) + "... (생략됨)";
            }

            if (userUuid != null && !userUuid.isEmpty()) {
                logger.info("응답 본문 - [{}] {} - 사용자: {}: {}",
                        requestMethod, requestUri, userUuid, responseContentAsString);
            } else {
                logger.info("응답 본문 - [{}] {} - 공개 요청: {}",
                        requestMethod, requestUri, responseContentAsString);
            }
        }

        // 중요: 응답을 다시 복사해서 클라이언트에게 전송
        responseWrapper.copyBodyToResponse();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 특정 URL 패턴에 대해 필터링 제외 (선택적)
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") || // 액추에이터 제외
                path.contains("/swagger-ui") ||  // Swagger UI 제외
                path.contains("/api-docs") ||    // API 문서 제외
                path.contains("/favicon.ico");   // 파비콘 제외
    }
}