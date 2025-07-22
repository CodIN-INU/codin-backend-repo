package inu.codin.codinticketingapi.domain.user.config;

import feign.RequestInterceptor;
import inu.codin.codinticketingapi.security.util.SecurityUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

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
        return SecurityUtil.getUserToken();
    }
}
