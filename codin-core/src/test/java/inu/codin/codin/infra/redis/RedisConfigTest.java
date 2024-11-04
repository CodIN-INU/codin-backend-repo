package inu.codin.codin.infra.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RedisConfigTest {

    @Autowired
    private RedisProperties redisProperties;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void Redis연결설정테스트() {
        assertEquals(redisProperties.getHost(), "localhost");
        assertEquals(redisProperties.getPort(), 6379);
    }

    @Test
    void redisTemplate작동확인테스트() {
        redisTemplate.opsForValue().set("key", "value");
        assertEquals(redisTemplate.opsForValue().get("key"), "value");
        redisTemplate.delete("key");
    }
}