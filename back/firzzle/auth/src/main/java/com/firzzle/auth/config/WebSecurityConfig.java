package com.firzzle.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (REST API는 CSRF에 덜 취약함)
                .csrf(csrf -> csrf.disable())

                // 요청 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // Swagger UI 관련 모든 경로 허용
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**", "/swagger-resources/**").permitAll()

                        // API 엔드포인트 권한 설정 - AuthController 경로 기반
                        .requestMatchers("/api/v1/kakao/callback").permitAll()  // 카카오 콜백 URL
                        .requestMatchers("/api/v1/refresh").permitAll()         // 토큰 갱신 엔드포인트
                        .requestMatchers("/api/v1/logout").permitAll()          // 로그아웃 엔드포인트
                        .requestMatchers("/api/v1/admin/login").permitAll()          // 로그아웃 엔드포인트
                        .requestMatchers("/api/v1/auth/kakao/callback").permitAll()  // 카카오 콜백 URL
                        .requestMatchers("/api/v1/auth/refresh").permitAll()         // 토큰 갱신 엔드포인트
                        .requestMatchers("/api/v1/auth/logout").permitAll()          // 로그아웃 엔드포인트
                        .requestMatchers("/api/v1/auth/admin/login").permitAll()          // 로그아웃 엔드포인트

                        // Actuator 엔드포인트 허용
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 세션 관리 (JWT를 사용하므로 STATELESS로 설정)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 게이트웨이 헤더를 처리하는 커스텀 필터 추가
                .addFilterBefore(new GatewayAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 게이트웨이에서 전달된 헤더를 처리하는 커스텀 필터
    public class GatewayAuthenticationFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            String userUuid = request.getHeader("X-User-UUID");
            String userRole = request.getHeader("X-User-Role");
            String userScope = request.getHeader("X-User-Scope");

            // 헤더에 사용자 정보가 있으면 인증 처리
            if (userUuid != null && !userUuid.isEmpty()) {
                List<SimpleGrantedAuthority> authorities = Collections.emptyList();

                // userRole이 있으면 권한 설정
                if (userRole != null && !userRole.isEmpty()) {
                    authorities = Arrays.stream(userRole.split(","))
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                            .collect(Collectors.toList());
                }

                // 사용자 인증 정보 생성 및 SecurityContext에 설정
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userUuid, null, authorities);

                // 추가 정보가 필요하면 authentication.setDetails()에 설정 가능

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }
    }
}