package com.firzzle.stt.config;

//@Configuration 클래스에 등록
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

 @Bean
 public WebClient webClient() {
     return WebClient.builder()
         .baseUrl("http://localhost:8000")
         .defaultHeader("Content-Type", "application/json")
         .build();
 }
}
