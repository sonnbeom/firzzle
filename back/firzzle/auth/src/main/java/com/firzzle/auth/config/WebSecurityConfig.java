package com.firzzle.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

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
                        .requestMatchers("/api/v1/auth/kakao/callback").permitAll()  // 카카오 콜백 URL
                        .requestMatchers("/api/v1/auth/refresh").permitAll()         // 토큰 갱신 엔드포인트
                        .requestMatchers("/api/v1/auth/logout").permitAll()          // 로그아웃 엔드포인트

                        // Actuator 엔드포인트 허용
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 세션 관리 (JWT를 사용하므로 STATELESS로 설정)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}