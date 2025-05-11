package com.firzzle.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * @Class Name : SecurityConfig.java
 * @Description : Spring Security 설정 클래스 (리액티브 환경용)
 * @author Firzzle
 * @since 2025. 5. 12.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // CSRF 비활성화 (API 게이트웨이는 상태를 저장하지 않음)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 경로별 인증 설정
                .authorizeExchange(exchanges -> exchanges
                        // 공개 API 경로 설정
                        .pathMatchers(
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/kakao/callback",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/me").permitAll()

                        // Swagger/OpenAPI 문서 허용
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/api-docs/**").permitAll()

                        // 정적 리소스 허용
                        .pathMatchers("/static/**", "/favicon.ico").permitAll()

                        // 헬스체크 및 모니터링 허용
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()

                        // OPTIONS 요청은 모두 허용 (CORS preflight 요청)
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 관리자 전용 경로는 추가 보호 (JwtAuthFilter와 함께 이중 보호)
                        // 여기서는 JwtAuthFilter보다 먼저 실행되어 관리자 패턴 경로를 검사
                        .pathMatchers("/api/v*/admin/**").hasRole("ADMIN")

                        .anyExchange().permitAll()
                )

                // 세션 관리 비활성화 (JWT 사용으로 세션 불필요)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // 예외 처리 설정
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        })
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 origin 설정 (프로덕션에서는 구체적인 출처로 변경)
        configuration.setAllowedOrigins(Arrays.asList("*"));

        // 허용할 HTTP 메서드 설정
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 허용할 헤더 설정
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // 노출할 헤더 명시적 설정
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Access-Control-Expose-Headers",
                "X-User-UUID",
                "X-User-Role",
                "X-User-Scope"
        ));

        // 인증 정보 포함 설정
        configuration.setAllowCredentials(true);

        // preflight 요청의 캐시 시간 설정 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}