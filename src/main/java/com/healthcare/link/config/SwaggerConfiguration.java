package com.healthcare.link.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {
    @Bean
    public OpenAPI openApiV1() {
        Info info = new Info()
                .title("Healthcare Link API Document")
                .version("v1")
                .description("헬스케어 데이터 연동 API 명세");
        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}
