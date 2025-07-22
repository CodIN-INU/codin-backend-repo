package inu.codin.codinticketingsse.sse.config;

import inu.codin.codinticketingsse.sse.listener.EventStockStreamListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisStreamListenerConfig {

    private final RedisConnectionFactory connectionFactory;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EventStockStreamListener streamListener;

    private static final String STREAM_KEY    = "event-stock-stream";
    private static final String GROUP_NAME    = "event-stock-group";
    private static final String CONSUMER_NAME = "event-stock-consumer";

    @PostConstruct
    public void initializeRedisStreamListener() {
        // Consumer Group 생성
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
            log.info("Redis Stream 그룹 생성, key:{}, group:{}", STREAM_KEY, GROUP_NAME);
        } catch (Exception e) {
            log.info("Redis Stream 그룹이 이미 존재, group:{}, message:{}", GROUP_NAME, e.getMessage());
        }

        // ListenerContainer 옵션 설정
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainerOptions.builder()
                    .pollTimeout(Duration.ofMillis(200)) // 저지연성 우선
                    .build();

        // ListenerContainer 생성
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container
                = StreamMessageListenerContainer.create(connectionFactory, options);

        // Subscription 등록
        container.receive(
                Consumer.from(GROUP_NAME, CONSUMER_NAME),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
                streamListener
        );
        // 컨테이너 시작
        container.start();
        log.info("Redis Stream Listener 컨테이너 시작, key:{}", STREAM_KEY);
    }
}
