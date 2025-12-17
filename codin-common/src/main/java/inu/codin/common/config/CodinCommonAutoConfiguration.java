package inu.codin.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import inu.codin.common.util.MultipartJackson2HttpMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Codin Common 모듈의 자동 구성 클래스
 *
 * 목적:
 * - codin-common 모듈을 의존성으로 추가하기만 해도
 *   MultipartJackson2HttpMessageConverter 빈이 자동으로 등록되도록 함
 *
 * 특징:
 * - Spring Boot 3 방식의 AutoConfiguration
 * - 애플리케이션(@SpringBootApplication)에서
 *   scanBasePackages를 수정하지 않아도 됨
 */
@AutoConfiguration
public class CodinCommonAutoConfiguration {

    /**
     * Multipart 요청(JSON + File)을 처리하기 위한
     * 커스텀 HttpMessageConverter 빈 등록
     *
     * ObjectMapper는 Spring Boot가 기본으로 제공하는
     * 공용 Jackson ObjectMapper 빈을 주입받아 사용
     *
     * → 이렇게 하면:
     *   - Jackson 설정(LocalDateTime, snake_case 등)
     *   - 기존 API JSON 직렬화 규칙
     *   을 그대로 재사용 가능
     */
    @Bean
    public MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter(
            ObjectMapper objectMapper
    ) {
        return new MultipartJackson2HttpMessageConverter(objectMapper);
    }
}