package inu.codin.codin.infra.redis;

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

}