package com.firzzle.auth;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
//@EnableFeignClients
@EnableEncryptableProperties
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
@ComponentScan(basePackages = {
		"com.firzzle.auth",  // 현재 모듈의 패키지
		"com.firzzle.common",  // common 모듈의 설정 패키지
		"com.firzzle.jwt",  // jwt 모듈의 설정 패키지
})
@EnableDiscoveryClient
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

}
