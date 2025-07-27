package inu.codin.codin.global.infra.redis.config;

import inu.codin.codin.global.infra.redis.exception.RedisUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthChecker {

    private final RedisTemplate<String, String> redisTemplate;
    private volatile boolean redisAvailable = true; // Redis의 현재 상태

    /**
     * Redis 상태를 확인하고 사용 가능 여부를 갱신합니다.
     * 장애 발생 시 예외를 던져 호출자가 처리하도록 합니다.
     *
     * @throws RedisUnavailableException Redis 장애 발생 시 예외
     */
    public void checkRedisStatus() {
        try {
            boolean status = "PONG".equals(redisTemplate.getConnectionFactory().getConnection().ping());
            if (status && !redisAvailable) {
                handleRedisRecovery(); // 복구 처리
            } else if (!status && redisAvailable) {
                handleRedisFailure(new RuntimeException("Redis가 응답하지 않습니다."));
            }
            redisAvailable = status;
        } catch (Exception e) {
            handleRedisFailure(e);
        }
    }

    /**
     * Redis의 현재 사용 가능 여부를 반환합니다.
     *
     * @return Redis가 활성화 상태라면 true
     */
    public boolean isRedisAvailable() {
        return redisAvailable;
    }

    /**
     * Redis 복구 처리 로직.
     */
    private void handleRedisRecovery() {
        redisAvailable = true;
        log.info("[Redis 상태 변경] Redis가 활성화되었습니다. 정상 상태로 전환.");
    }

    /**
     * Redis 장애 처리 로직.
     * 상태를 비활성화로 설정하고, 장애 내용을 로깅합니다.
     *
     * @param e Redis 장애 원인
     */
    private void handleRedisFailure(Exception e) {
        if (redisAvailable) {
            redisAvailable = false;
            log.warn("[Redis 상태 변경] Redis가 비활성화되었습니다. 원인: {}", e.getMessage(), e);
        }
    }
}