package inu.codin.codinticketingapi.common.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties("spring.data.redis")
public class RedisProperties {
    private String host;
    private int port;
    private String password;
    private int database;
}