package inu.codin.codinticketingapi;

import inu.codin.codinticketingapi.config.RedisConfig;
import inu.codin.codinticketingapi.config.S3Config;
import inu.codin.codinticketingapi.domain.user.fegin.UserFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@ActiveProfiles("test")
@SpringBootTest
class CodinTicketingApiApplicationTests {

    @MockitoBean
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void contextLoads() {
    }

}
