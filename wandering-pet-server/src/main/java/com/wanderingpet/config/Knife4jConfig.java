package com.wanderingpet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 3 配置：访问 http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    private static final String SECURITY_SCHEME_NAME = "Authorization";

    @Bean
    public OpenAPI wanderingPetOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("流浪宠物领养系统 API")
                        .description("连接救助机构与领养人的公益平台：宠物档案、领养审核、回访、资讯、捐赠、线索上报、在线咨询。")
                        .version("v1.0.0"))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Token，格式：Bearer {token}")));
    }
}
