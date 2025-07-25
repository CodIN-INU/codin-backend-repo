package inu.codin.codin.global.infra.redis.exception;

public class RedisUnavailableException extends RuntimeException {
    public RedisUnavailableException(String message) {
        super(message);
    }
}
