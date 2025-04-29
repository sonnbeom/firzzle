package com.firzzle.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@EnableEncryptableProperties
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableWebSecurity
@EnableSchedulerLock(defaultLockAtMostFor = "60m")
@EnableCaching
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

}
