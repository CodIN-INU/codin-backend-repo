package inu.codin.lecture;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest(classes = CodinLectureApiApplicationTests.TestApp.class)
class CodinLectureApiApplicationTests {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableJpaAuditing
    @ComponentScan(
            basePackages = "inu.codin.codin",
            excludeFilters = {
                    @ComponentScan.Filter(
                            type = FilterType.ASSIGNABLE_TYPE,
                            classes = {
                                    inu.codin.codin.common.config.SwaggerConfig.class,
                                    inu.codin.codin.global.config.SwaggerConfig.class,
                                    inu.codin.codin.TestController.class,
                                    inu.codin.codin.global.LectureTestController.class
                            }
                    )
            }
    )
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
