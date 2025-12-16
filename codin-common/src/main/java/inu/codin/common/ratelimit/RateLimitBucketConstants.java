package inu.codin.codin.common.ratelimit;

import java.time.Duration;

/**
 * @apiNote # BUCKET_CAPACITY : 토큰 버킷의 최대 용량
 *          # BUCKET_TOKEN : 토큰 버킷의 현재 토큰 개수
 *          # REFILL_DURATION : 토큰 버킷이 채워지는 주기
 *          # REQUEST_PER_COST : 요청당 필요한 토큰 개수
 */
public class RateLimitBucketConstants {

    public static final long BUCKET_CAPACITY = 10L;
    public static final long BUCKET_TOKEN = 10L;
    public static final Duration REFILL_DURATION = Duration.ofSeconds(1);
    public static final long REQUEST_PER_COST = 1L;

}
