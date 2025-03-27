package inu.codin.codin.infra.redis.scheduler;

import inu.codin.codin.infra.redis.config.RedisHealthChecker;
import inu.codin.codin.infra.redis.exception.RedisUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisRecoverSyncScheduler {

    private final RedisHealthChecker redisHealthChecker;

    private Instant lastRecoveryTime = Instant.MIN; // 마지막 복구 시간

    /**
     * Redis 상태를 주기적으로 확인하고, 필요한 경우 복구 작업을 수행합니다.
     */
    @Scheduled(fixedRate = 21600000) // 6시간마다 실행
    public void monitorRedisAndRecover() {
        try {
            redisHealthChecker.checkRedisStatus(); // Redis 상태 확인
        } catch (RedisUnavailableException e) {
            log.warn("Redis 장애 감지: {}. MongoDB 우회 처리 중...", e.getMessage());
        }
    }

    /**
     * Redis 복구가 필요한지 확인합니다.
     *
     * @return Redis가 복구된 상태라면 true
     */
    private boolean shouldRecoverRedis() {
        return redisHealthChecker.isRedisAvailable() && canPerformRecovery();
    }

    /**
     * Redis 복구 작업을 실행합니다.
     */
    private void recoverRedisData() {
        if (!canPerformRecovery()) {
            log.info("최근 복구 이후 간격이 짧아 복구 작업을 건너뜁니다.");
            return;
        }
        try {
            log.info("[Redis 복구 작업] MongoDB 데이터를 Redis 복구 작업 시작...");
            lastRecoveryTime = Instant.now();
            log.info("[Redis 복구 작업] MongoDB 데이터를 Redis 데이터 복구 완료.");
        } catch (Exception e) {
            log.error("[Redis 복구 작업] Redis 복구 작업 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 마지막 복구 이후 충분한 간격이 지났는지 확인합니다.
     *
     * @return 복구 작업 실행 가능 여부
     */
    private boolean canPerformRecovery() {
        Instant now = Instant.now();
        return Duration.between(lastRecoveryTime, now).toMinutes() >= 5; // 최소 5분 간격
    }
}