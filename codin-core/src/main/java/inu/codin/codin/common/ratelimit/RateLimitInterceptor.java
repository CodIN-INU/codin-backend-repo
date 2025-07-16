package inu.codin.codin.common.ratelimit;

import inu.codin.codin.common.response.RateLimitResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static inu.codin.codin.common.ratelimit.RateLimitBucketConstants.*;

/**
 * Reference : https://velog.io/@whcksdud8/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-Rate-limit-%ED%95%B8%EB%93%A4%EB%A7%81-%EB%B0%8F-%EB%AA%A8%EB%8B%88%ED%84%B0%EB%A7%81
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    // todo : Redis로 변경
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = ClientIpUtil.getClientIp(request);
        Bucket bucket = cache.computeIfAbsent(clientIp, key -> createNewBucket());
        ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(REQUEST_PER_COST);

        if (isRateLimitExceeded(request, response, clientIp, consumptionProbe)) {
            return false;
        }
        return true;
    }

    private boolean isRateLimitExceeded(HttpServletRequest request, HttpServletResponse response, String clientIp, ConsumptionProbe consumptionProbe) {

        if (consumptionProbe.isConsumed()) {
            log.info("IP: {}, remaining tokens: {}", clientIp, consumptionProbe.getRemainingTokens());
            RateLimitResponse.successResponse(response, consumptionProbe.getRemainingTokens(), BUCKET_CAPACITY, REFILL_DURATION);
            return false;
        } else {
            log.warn("IP: {}, rate limit exceeded", clientIp);
            RateLimitResponse.errorResponse(response, BUCKET_CAPACITY, REFILL_DURATION, 1);
            rateLimitService.isLimitReachedThreshold(clientIp);
            return true;
        }
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        BUCKET_CAPACITY,
                        Refill.intervally(BUCKET_TOKEN, REFILL_DURATION)
                ))
                .build();
    }
}
