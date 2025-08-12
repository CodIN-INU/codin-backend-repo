package inu.codin.codinticketingapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // LocalDateTime을 타임스탬프(숫자)가 아닌 ISO-8601 형식의 문자열로 직렬화
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2. ObjectMapper를 사용하는 Serializer 생성
        return getStringObjectRedisTemplate(redisConnectionFactory, objectMapper);
    }

    @Bean
    public RedisTemplate<String, String> eventRedisTemplate (RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, String> pingRedisTemplate (RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }

    private RedisTemplate<String, Object> getStringObjectRedisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setDefaultSerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(serializer);
        return redisTemplate;
    }
}
