package inu.codin.codinticketingapi.domain.ticketing.redis;

import inu.codin.codinticketingapi.domain.ticketing.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisHealthCheckService {
    private final EventService eventService;
    private final RedisTemplate<String, String> pingRedisTemplate;

    // 연속 실패 횟수를 저장 (동시성 문제를 위해 AtomicInteger 사용)
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    // 6번 연속 실패하면 조치 실행 (10초 간격이므로 약 1분간 장애 시)
    private static final int FAILURE_THRESHOLD = 6;

    // 10초마다 실행 (fixedDelay는 이전 작업이 끝나고 10초 후 실행)
    @Scheduled(fixedDelay = 10000)
    public void checkRedisHealth() {
        try {
            // 가장 간단한 PING 명령으로 연결 상태 확인
            pingRedisTemplate.getConnectionFactory().getConnection().ping();
            handleSuccess();
            log.info("redis 정상 연결 중");
        } catch (RedisConnectionFailureException e) {
            handleFailure();
        } catch (Exception e) {
            log.error("Redis 헬스 체크 중 예상치 못한 오류 발생", e);
            handleFailure();
        }
    }

    private void handleSuccess() {
        int currentFailures = consecutiveFailures.get();

        if (currentFailures >= FAILURE_THRESHOLD) {
            log.info("Redis 연결이 복구되었습니다. 이벤트 상태를 ACTIVE로 복원합니다.");
            eventService.restoreUpcomingEventsToActive();
        } else if (currentFailures > 0) {
            log.info("Redis 연결이 복구되었습니다.");
        }
        // 실패 카운트 리셋
        consecutiveFailures.set(0);
    }

    private void handleFailure() {
        int failures = consecutiveFailures.incrementAndGet();
        log.warn("Redis 연결 실패. 연속 실패 횟수: {}회", failures);

        // 정확히 임계값에 도달했을 때 딱 한 번만 실행
        if (failures == FAILURE_THRESHOLD) {
            log.error("Redis 장애가 {}초 이상 지속되어 모든 활성 이벤트를 UPCOMING 상태로 변경합니다.", 10 * FAILURE_THRESHOLD);
            eventService.changeAllActiveEventsToUpcoming();
        }
    }
}
