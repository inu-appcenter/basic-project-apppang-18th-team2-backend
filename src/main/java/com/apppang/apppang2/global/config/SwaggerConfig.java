package com.apppang.apppang2.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        Info info = new Info()
                .title("AppPang API")
                .description("AppPang Backend API")
                .version("v1.0");

        String jwtSchemeName = "bearerAuth";

        return new OpenAPI()
                .info(info)

                // Swagger 전체에 JWT 인증 적용
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))

                // JWT 인증 방식 정의
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName,
                                new SecurityScheme()
                                        .name(jwtSchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}