package com.apppang.apppang2.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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

        return new OpenAPI()
                .info(info);
    }
}