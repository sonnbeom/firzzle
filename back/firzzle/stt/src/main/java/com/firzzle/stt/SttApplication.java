package com.firzzle.stt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.context.WebApplicationContext;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@EnableScheduling
//@EnableFeignClients
@EnableEncryptableProperties
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
@ComponentScan(basePackages = {
        "com.firzzle.stt",  // 현재 모듈의 패키지
        "com.firzzle.common",  // common 모듈의 설정 패키지
})
@MapperScan("com.firzzle.stt.mapper")
@EnableDiscoveryClient
public class SttApplication {

    public static void main(String[] args) {
    	ConfigurableApplicationContext context = SpringApplication.run(SttApplication.class, args);
    	System.out.println(">>> WebApplicationContext loaded? " +
    	    (context instanceof WebApplicationContext));
    }

}
