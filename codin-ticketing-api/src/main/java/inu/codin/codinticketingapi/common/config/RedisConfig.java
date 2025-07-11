package inu.codin.codinticketingapi.common.config;

import inu.codin.codinticketingapi.common.redis.RedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Slf4j
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("Creating RedisConnectionFactory, port: {}, host: {}, password: {}, database: {}", redisProperties.getPort(), redisProperties.getHost(), redisProperties.getPassword(), redisProperties.getDatabase());

        RedisStandaloneConfiguration redisStandAloneConfiguration = new RedisStandaloneConfiguration();
        redisStandAloneConfiguration.setPort(redisProperties.getPort());
        redisStandAloneConfiguration.setHostName(redisProperties.getHost());
        redisStandAloneConfiguration.setPassword(redisProperties.getPassword());
        redisStandAloneConfiguration.setDatabase(redisProperties.getDatabase());

        return new LettuceConnectionFactory(
                redisStandAloneConfiguration,
                LettuceClientConfiguration.builder()
                    .commandTimeout(Duration.ofMillis(500)) // 명령 타임아웃
                    .shutdownTimeout(Duration.ofMillis(100)) // 셧다운 타임아웃
                    .build());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setDefaultSerializer(RedisSerializer.string());
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 객체를 JSON으로 직렬화
        return redisTemplate;
    }
}
