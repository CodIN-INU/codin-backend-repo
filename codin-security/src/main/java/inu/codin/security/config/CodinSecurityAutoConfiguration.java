package inu.codin.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Codin Security 자동 구성 클래스
 * 
 * Spring Boot 3 방식의 AutoConfiguration으로 구현
 * 의존성만 추가하면 자동으로 보안 설정이 활성화됩니다.
 * 
 * 조건:
 * - Web 애플리케이션인 경우
 * - Spring Security가 클래스패스에 있는 경우
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({HttpSecurity.class})
@ComponentScan(basePackages = "inu.codin.security")
@EnableConfigurationProperties({PermitAllProperties.class, PublicApiProperties.class})
public class CodinSecurityAutoConfiguration {
    
    // 필요한 경우 추가 빈 정의 가능
    // 현재는 @ComponentScan으로 자동 스캔되므로 별도 빈 정의 불필요
}