package com.firzzle.common.config;

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

public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    // 개발 모드 플래그 - 필요에 따라 변경 가능
    private static final boolean DEV_MODE = true;

    // 개발 환경에서 사용할 고정 사용자 정보
    private static final String DEV_UUID = "07f670f0-2853-11f0-aeb6-c68431894852";
    private static final String DEV_ROLE = "user";
    private static final String DEV_USERNAME = "user2";
    private static final String DEV_NAME = "OAuth 사용자2";
    private static final String DEV_EMAIL = "oauth_user@example.com";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (DEV_MODE) {
            // 개발 모드일 때 고정 사용자로 인증
            setupDevAuthentication(request);
        } else {
            // 프로덕션 모드일 때 헤더에서 인증 정보 가져오기
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
    }

    private void setupProductionAuthentication(HttpServletRequest request) {
        // Gateway에서 추가한 헤더에서 인증 정보 가져오기
        String uuid = request.getHeader("X-User-UUID");
        String role = request.getHeader("X-User-Role");
        String scope = request.getHeader("X-User-Scope");

        if (uuid != null) {
            // 권한 목록 생성
            List<GrantedAuthority> authorities = new ArrayList<>();

            // 역할 추가 (ROLE_ 접두사를 붙여야 hasRole()에서 인식됨)
            if (role != null && !role.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            }

            // 스코프 추가
            if (scope != null && !scope.isEmpty()) {
                Arrays.stream(scope.split("[ ,]"))
                        .filter(s -> !s.isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }

            // 인증 객체 생성 및 SecurityContext에 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(uuid, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
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
}