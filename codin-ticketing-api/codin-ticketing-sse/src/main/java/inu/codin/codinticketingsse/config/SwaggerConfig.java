package inu.codin.codinticketingsse.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    @Value("${server.domain}")
    private String BASE_DOMAIN_URL;
    @Value("${server.port}")
    private String SERVER_PORT;

    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
                .title("CODIN Ticketing SSE Module Documentation")
                .description("CODIN Ticketing SSE Module 명세서")
                .version("v1.0.0");

        // Bearer Token Auth
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Authorization 헤더를 통한 Bearer Token 인증");

        // Security Requirements 설정
        SecurityRequirement bearerRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .security(List.of(bearerRequirement))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerAuth)
                )
                .servers(List.of(
                        new Server().url(BASE_DOMAIN_URL + "/api/ticketing/sse").description("운영 서버"),
                        new Server().url("http://localhost:" + SERVER_PORT).description("로컬 서버")
                ));
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        // ForwardedHeaderFilter Bean 등록 Nginx 프록시 서버 사용 시 필요
        return new ForwardedHeaderFilter();
    }
}