package inu.codin.codin.global.infra.redis.config;

import inu.codin.codin.global.infra.redis.dto.RedisProperties;
import lombok.RequiredArgsConstructor;
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

// Redis configuration
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration redisStandAloneConfiguration = new RedisStandaloneConfiguration();
        redisStandAloneConfiguration.setPort(redisProperties.getPort());
        redisStandAloneConfiguration.setHostName(redisProperties.getHost());
        redisStandAloneConfiguration.setPassword(redisProperties.getPassword());
        redisStandAloneConfiguration.setDatabase(0);

        // Lettuce는 비동기 방식을 지원하는 Redis 클라이언트
        // 성능상 이점이 있어 기본적으로 사용
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(500)) // 명령 타임아웃
                .shutdownTimeout(Duration.ofMillis(100)) // 셧다운 타임아웃
                .build();

        return new LettuceConnectionFactory(redisStandAloneConfiguration, clientConfig);
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

