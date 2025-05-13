package com.firzzle.gateway.config;

import com.firzzle.gateway.filter.JwtAuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * @Class Name : SecurityConfig.java
 * @Description : Spring Security 설정 클래스 (리액티브 환경용)
 * @author Firzzle
 * @since 2025. 5. 12.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // CSRF 비활성화 (API 게이트웨이는 상태를 저장하지 않음)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // CORS 설정은 application.yml에서 관리, 여기서는 기본 설정만 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 경로별 인증 설정
                .authorizeExchange(exchanges -> exchanges
                        // Swagger/OpenAPI 문서
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/api-docs/**").permitAll()

                        // 정적 리소스
                        .pathMatchers("/static/**", "/favicon.ico").permitAll()

                        // 헬스체크 및 모니터링
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()

                        // OPTIONS 요청
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .anyExchange().permitAll()
                )

                // 세션 관리 비활성화
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // 예외 처리
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                            logger.info("HttpStatus.UNAUTHORIZED : {}", ex.getMessage());
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
                            logger.info("HttpStatus.FORBIDDEN : {}", ex.getMessage());
                            return exchange.getResponse().setComplete();
                        })
                )

                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 최소한의 설정만 포함하고 나머지는 application.yml에 위임
        CorsConfiguration configuration = new CorsConfiguration();

        // CORS를 위한 기본 설정만 유지, 세부사항은 application.yml에서 관리
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}