package inu.codin.codinticketingapi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String REDIS_HOST;

    @Value("${spring.data.redis.port}")
    private int REDIS_PORT;

    @Value("${spring.data.redis.database}")
    private int REDIS_DATABASE;

    @Value("${spring.data.redis.password}")
    private String REDIS_PASSWORD;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandAloneConfiguration = new RedisStandaloneConfiguration();
        redisStandAloneConfiguration.setPort(REDIS_PORT);
        redisStandAloneConfiguration.setHostName(REDIS_HOST);
        redisStandAloneConfiguration.setPassword(REDIS_PASSWORD);
        redisStandAloneConfiguration.setDatabase(REDIS_DATABASE);

        return new LettuceConnectionFactory(redisStandAloneConfiguration, LettuceClientConfiguration.builder()
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
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }
}
