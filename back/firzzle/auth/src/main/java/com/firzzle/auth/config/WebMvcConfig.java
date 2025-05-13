package com.firzzle.auth.config;

import com.firzzle.auth.interceptor.LoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Class Name : WebMvcConfig.java
 * @Description : MVC 설정 클래스 (인터셉터 등록)
 * @author Firzzle
 * @since 2025. 5. 13.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    public WebMvcConfig(LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**") // 모든 경로에 적용
                .excludePathPatterns(
                        "/actuator/**",    // 액추에이터 제외
                        "/swagger-ui/**",  // Swagger UI 제외
                        "/api-docs/**",    // API 문서 제외
                        "/v3/api-docs/**", // API 문서 제외
                        "/favicon.ico"     // 파비콘 제외
                );
    }
}