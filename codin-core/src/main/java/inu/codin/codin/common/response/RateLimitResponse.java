package inu.codin.codin.common.response;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.time.Duration;

public class RateLimitResponse {

    public static void successResponse(HttpServletResponse response, long remainToken, Long bucketCapacity, Duration callsInSeconds) {
        response.setHeader("X-RateLimit-Remaining", Long.toString(remainToken));
        response.setHeader("X-RateLimit-Limit", bucketCapacity + ";w=" + callsInSeconds.getSeconds());
    }

    public static void errorResponse(HttpServletResponse response, Long bucketCapacity, Duration callsInSeconds, float waitForRefill) {
//        response.setHeader("X-RateLimit-Retry-After", Float.toString(waitForRefill));
        response.setHeader("X-RateLimit-Limit", bucketCapacity + ";w=" + callsInSeconds.getSeconds());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }
}
