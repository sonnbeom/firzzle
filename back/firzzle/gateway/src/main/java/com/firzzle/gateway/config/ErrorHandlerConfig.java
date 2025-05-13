package com.firzzle.gateway.config;

import com.firzzle.gateway.exception.GatewayExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.stream.Collectors;

/**
 * @Class Name : ErrorHandlerConfig.java
 * @Description : 게이트웨이 예외 처리 구성 클래스
 * @author Firzzle
 * @since 2025. 5. 12.
 */
@Configuration
public class ErrorHandlerConfig {

    private final ServerProperties serverProperties;
    private final ApplicationContext applicationContext;
    private final WebProperties webProperties;
    private final ServerCodecConfigurer serverCodecConfigurer;
    private final ObjectProvider<ViewResolver> viewResolvers;
    private final ErrorAttributes errorAttributes;

    public ErrorHandlerConfig(
            ServerProperties serverProperties,
            ApplicationContext applicationContext,
            WebProperties webProperties,
            ServerCodecConfigurer serverCodecConfigurer,
            ObjectProvider<ViewResolver> viewResolvers,
            ErrorAttributes errorAttributes) {
        this.serverProperties = serverProperties;
        this.applicationContext = applicationContext;
        this.webProperties = webProperties;
        this.serverCodecConfigurer = serverCodecConfigurer;
        this.viewResolvers = viewResolvers;
        this.errorAttributes = errorAttributes;
    }

    @Bean
    @Primary
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorWebExceptionHandler errorWebExceptionHandler() {
        GatewayExceptionHandler exceptionHandler = new GatewayExceptionHandler();
        return exceptionHandler;
    }
}