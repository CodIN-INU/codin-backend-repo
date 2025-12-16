package inu.codin.codin.common.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitService {

    // IP 별로 요청 횟수를 저장하는 Map
    private Map<String, RequestInfo> rateLimitErrorCounter = new ConcurrentHashMap<>();

    public boolean isLimitReachedThreshold(String clientIp) {
        RequestInfo requestInfo = rateLimitErrorCounter.computeIfAbsent(clientIp, key -> new RequestInfo());
        if (!isApplyHandling(clientIp, requestInfo)) {
            requestInfo.saveCount();
            return false;
        }
        return true;
    }

    private boolean isApplyHandling(String clientIp, RequestInfo requestInfo) {
        if (requestInfo.isWithinTimeWindow()) {
            int count = requestInfo.incrementCount();
            log.info("IP: {}, remaining tokens: {}", clientIp, count);
            checkAndResetIfLimitExceeded(clientIp, requestInfo, count);
            return true;
        }
        return false;
    }

    private void checkAndResetIfLimitExceeded(String clientIp, RequestInfo requestInfo, int count) {
        if (count >= 10) {
            log.warn("IP: {}, rate limit exceeded, Count : {}", clientIp, count);
            requestInfo.resetCount();
            // todo : 지속적으로 rate limit을 넘는 IP Ban 처리 또는 계정 Ban 처리 필요.
        }
    }

    private static class RequestInfo {
        private AtomicInteger count = new AtomicInteger(0);
        private LocalDateTime lastRequestTime;

        // 요청 횟수 증가
        public int incrementCount() {
            return this.count.incrementAndGet();
        }

        // 1시간 이내에 요청이 있는지 확인
        public boolean isWithinTimeWindow() {
            LocalDateTime now = LocalDateTime.now();
            if (this.lastRequestTime == null || ChronoUnit.HOURS.between(this.lastRequestTime, now) >= 1) {
                this.lastRequestTime = now;
                return false;
            }
            return true;
        }

        // 요청 횟수 초기화
        public void saveCount() {
            this.count.set(1);
            lastRequestTime = LocalDateTime.now();
        }

        // 요청 횟수 초기화
        public void resetCount() {
            this.count.set(0);
            lastRequestTime = LocalDateTime.now();
        }
    }

}
