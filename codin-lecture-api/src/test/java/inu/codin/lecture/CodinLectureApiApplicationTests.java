package inu.codin.lecture;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest(classes = CodinLectureApiApplicationTests.TestApp.class)
class CodinLectureApiApplicationTests {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {
            "inu.codin.codin.domain.admin",
            "inu.codin.codin.domain.elasticsearch",
            "inu.codin.codin.domain.lecture",
            "inu.codin.codin.domain.like",
            "inu.codin.codin.domain.review",
            "inu.codin.codin.global",
            "inu.codin.codin.domain.user.service"
    })
    static class TestApp { }

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private RedisTemplate<String, String> stringRedisTemplate;

    @MockitoBean
    private ElasticsearchClient elasticsearchClient;

    @MockitoBean
    private ElasticsearchAsyncClient elasticsearchAsyncClient;

    @SuppressWarnings("rawtypes")
    @MockitoBean
    private BulkIngester bulkIngester;

    @Test
    void contextLoads() {
    }

}
