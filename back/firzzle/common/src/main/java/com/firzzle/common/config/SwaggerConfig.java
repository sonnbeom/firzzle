package com.firzzle.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicV1Api() {
        return GroupedOpenApi.builder()
                .group("v1")
                .displayName("API v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }

//    @Bean
//    public GroupedOpenApi publicV2Api() {
//        return GroupedOpenApi.builder()
//                .group("v2")
//                .displayName("API v2")
//                .pathsToMatch("/api/v2/**")
//                .build();
//    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rabbit API Documentation")
                        .version("1.0.0")
                        .description("API Documentation")
                        .termsOfService("http://swagger.io/terms/")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
