package com.firzzle.llm;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

@EnableScheduling
//@EnableFeignClients
@EnableEncryptableProperties
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
@ComponentScan(basePackages = {
        "com.firzzle.llm",  // 현재 모듈의 패키지
        "com.firzzle.common",  // common 모듈의 설정 패키지
})
@MapperScan("com.firzzle.llm.mapper") // MyBatis 매퍼
@EnableDiscoveryClient
public class LlmApplication {

	public static void main(String[] args) {
		SpringApplication.run(LlmApplication.class, args);
	}

}
