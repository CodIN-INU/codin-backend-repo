package inu.codin.codin.common.security.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.mail.access")
public class AccessEmailProperties {
    private List<String> domain;
}
