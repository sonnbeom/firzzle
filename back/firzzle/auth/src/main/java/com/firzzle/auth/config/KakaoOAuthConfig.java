package com.firzzle.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * @Class Name : KakaoOAuthConfig.java
 * @Description : Kakao OAuth 설정 속성 클래스
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
@Getter
@Setter
public class KakaoOAuthConfig {

    private Registration registration = new Registration();
    private Provider provider = new Provider();

    // 개발 모드 설정을 위한 추가 필드
    private String localRedirectUri;
    private String prodRedirectUri;

    @Getter
    @Setter
    public static class Registration {
        private Kakao kakao = new Kakao();

        @Getter
        @Setter
        public static class Kakao {
            private String clientId;
            private String clientSecret;
            private String redirectUri;
            private String authorizationGrantType;
            private String clientAuthenticationMethod;
            private String clientName;
            private String[] scope;
        }
    }

    @Getter
    @Setter
    public static class Provider {
        private Kakao kakao = new Kakao();

        @Getter
        @Setter
        public static class Kakao {
            private String authorizationUri;
            private String tokenUri;
            private String userInfoUri;
            private String userNameAttribute;
        }
    }
}