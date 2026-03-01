package inu.codin.lecture;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import inu.codin.lecture.domain.user.feign.UserFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class CodinLectureApiApplicationTests {

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private RedisTemplate<String, String> stringRedisTemplate;

    @MockitoBean
    private ElasticsearchClient elasticsearchClient;

    @MockitoBean
    private ElasticsearchAsyncClient elasticsearchAsyncClient;

    @MockitoBean
    private UserFeignClient userFeignClient;

    @SuppressWarnings("rawtypes")
    @MockitoBean
    private BulkIngester bulkIngester;

    @Test
    void contextLoads() {
    }

}