package com.firzzle.learning.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HeaderAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(HeaderAuthenticationFilter.class);

    // 개발 모드 플래그 - 필요에 따라 변경 가능
    private static final boolean DEV_MODE = false;

    // 개발 환경에서 사용할 고정 사용자 정보
    private static final String DEV_UUID = "07f670f0-2853-11f0-aeb6-c68431894852";
    private static final String DEV_ROLE = "user";
    private static final String DEV_USERNAME = "user2";
    private static final String DEV_NAME = "OAuth 사용자2";
    private static final String DEV_EMAIL = "oauth_user@example.com";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.info("HeaderAuthenticationFilter 시작 - URI: {}, 메소드: {}",
                request.getRequestURI(), request.getMethod());

        if (DEV_MODE) {
            // 개발 모드일 때 고정 사용자로 인증
            logger.info("개발 모드로 실행 중 - 고정 사용자 정보 사용: UUID={}", DEV_UUID);
            setupDevAuthentication(request);
        } else {
            // 프로덕션 모드일 때 헤더에서 인증 정보 가져오기
            logger.info("프로덕션 모드로 실행 중 - 헤더에서 인증 정보 가져오기");
            setupProductionAuthentication(request);
        }

        filterChain.doFilter(request, response);
    }

    private void setupDevAuthentication(HttpServletRequest request) {
        // 권한 목록 생성
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + DEV_ROLE.toUpperCase()));
        authorities.add(new SimpleGrantedAuthority("content:read"));
        authorities.add(new SimpleGrantedAuthority("content:write"));

        // 인증 객체 생성 및 SecurityContext에 설정
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(DEV_UUID, null, authorities);

        // 추가 정보 설정 (필요시 사용)
        DevUserDetails userDetails = new DevUserDetails(
                DEV_UUID,
                DEV_USERNAME,
                DEV_NAME,
                DEV_EMAIL
        );
        authentication.setDetails(userDetails);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // RequestBox에서 사용할 수 있도록 속성 설정
        request.setAttribute("uuid", DEV_UUID);
        request.setAttribute("username", DEV_USERNAME);
        request.setAttribute("role", DEV_ROLE);
        request.setAttribute("name", DEV_NAME);
        request.setAttribute("email", DEV_EMAIL);

        logger.info("개발 모드 인증 완료 - 사용자: {}, 역할: {}, 권한: {}",
                DEV_UUID, DEV_ROLE, authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
    }

    private void setupProductionAuthentication(HttpServletRequest request) {
        // Gateway에서 추가한 헤더에서 인증 정보 가져오기
        String uuid = request.getHeader("X-User-UUID");
        String role = request.getHeader("X-User-Role");
        String scope = request.getHeader("X-User-Scope");

        logger.info("HeaderAuthFilter - 수신된 헤더 정보: UUID={}, Role={}, Scope={}", uuid, role, scope);

        if (uuid != null) {
            // 권한 목록 생성
            List<GrantedAuthority> authorities = new ArrayList<>();

            // 역할 추가 (ROLE_ 접두사를 붙여야 hasRole()에서 인식됨)
            if (role != null && !role.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                logger.info("HeaderAuthFilter - 추가된 역할: ROLE_{}", role.toUpperCase());
            }

            // 스코프 추가
            if (scope != null && !scope.isEmpty()) {
                logger.info("HeaderAuthFilter - 수신된 스코프: {}", scope);

                List<String> addedScopes = new ArrayList<>();
                Arrays.stream(scope.split("[ ,]"))
                        .filter(s -> !s.isEmpty())
                        .peek(addedScopes::add)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);

                logger.info("HeaderAuthFilter - 추가된 스코프들: {}", String.join(", ", addedScopes));
            }

            // 인증 객체 생성 및 SecurityContext에 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(uuid, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("HeaderAuthFilter - 인증 객체 생성 완료: principal={}, authorities={}",
                    authentication.getPrincipal(),
                    authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));

            // 추가: 현재 요청 정보와 함께 인증 정보 로깅
            logger.info("HeaderAuthFilter - 요청 정보: 메소드={}, 경로={}, 인증됨={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    SecurityContextHolder.getContext().getAuthentication() != null);
        } else {
            logger.warn("HeaderAuthFilter - UUID 헤더 없음, 인증 객체 생성 실패");
        }
    }

    // 개발용 사용자 상세 정보 클래스
    public static class DevUserDetails {
        private final String uuid;
        private final String username;
        private final String name;
        private final String email;

        public DevUserDetails(String uuid, String username, String name, String email) {
            this.uuid = uuid;
            this.username = username;
            this.name = name;
            this.email = email;
        }

        public String getUuid() {
            return uuid;
        }

        public String getUsername() {
            return username;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        String method = request.getMethod();

        // 기본적으로 필터가 적용되지 않는 경로들
        boolean shouldNotFilter = path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/api-docs/") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/swagger-resources/") ||
                path.startsWith("/api/v1/logging/visit");

        // /api/v1/learning/share/ 경로에 대한 GET 요청만 필터에서 제외
        if (path.startsWith("/api/v1/share/") && "GET".equalsIgnoreCase(method)) {
            shouldNotFilter = true;
            logger.info("HeaderAuthFilter - GET 요청에 대해 필터 제외 경로: {}", path);
        }

        if (shouldNotFilter) {
            logger.info("HeaderAuthFilter - 필터 제외 경로: {}, 메소드: {}", path, method);
        }

        return shouldNotFilter;
    }
}