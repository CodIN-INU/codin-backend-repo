package inu.codin.codin.common.config;

import inu.codin.codin.common.ratelimit.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Lecture API - 좋아요 개수 Feign 요청으로 인한 RateLimiting 에러로 주석화
//@Configuration
//@RequiredArgsConstructor
//public class Bucket4jRateLimitApp implements WebMvcConfigurer {
//
//    private final RateLimitInterceptor interceptor;
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.
//                addInterceptor(interceptor).
//                addPathPatterns("/**")
//                .excludePathPatterns("/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs", "/webjars/**");
//    }
//}
