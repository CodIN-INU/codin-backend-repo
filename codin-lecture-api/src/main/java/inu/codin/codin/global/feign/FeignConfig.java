package inu.codin.codin.global.feign;

import feign.RequestInterceptor;
import inu.codin.codin.global.auth.util.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String jwtToken = getJwtToken();
            if (jwtToken != null && !jwtToken.isEmpty()) {
                requestTemplate.header("Authorization", "Bearer " + jwtToken);
            }
        };
    }

    private String getJwtToken() {
        return SecurityUtils.getUserToken();
    }
}