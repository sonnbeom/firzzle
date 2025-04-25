package com.firzzle.common.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * 국제화 설정 클래스
 * 기존의 I18nConfig를 확장하여 코드 메시지 리소스를 추가합니다.
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * 메시지 소스 빈 정의
     * 기존의 messages와 국제화된 코드 메시지를 통합합니다.
     * @return MessageSource 빈
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * 로케일 해석기 빈 정의
     * 쿠키 기반 Locale 리졸버 설정
     * 기존 설정을 재사용합니다.
     * @return LocaleResolver 빈
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("locale");
        resolver.setDefaultLocale(Locale.KOREAN); // 기본 언어를 한국어로 설정
        resolver.setCookieMaxAge(60 * 60 * 24 * 365); // 쿠키 유효기간 1년
        return resolver;
    }

    /**
     * 로케일 변경 인터셉터 빈 정의
     * URL 파라미터로 언어 변경 처리하는 인터셉터
     * 기존 설정을 재사용합니다.
     * @return LocaleChangeInterceptor 빈
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // 예: ?lang=ko
        return interceptor;
    }

    /**
     * 인터셉터 등록
     * 기존 설정을 재사용합니다.
     * @param registry 인터셉터 레지스트리
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}