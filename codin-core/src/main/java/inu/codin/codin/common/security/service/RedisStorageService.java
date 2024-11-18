package inu.codin.codin.common.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RedisStorageService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * RefreshToken 저장
     * @param username
     * @param refreshToken
     * @param expiration
     */
    public void saveRefreshToken(String username, String refreshToken, long expiration) {
        log.debug("[RedisStorageService] saveRefreshToken : username = {}, refreshToken = {}, expiration = {}", username, refreshToken, expiration);

        // null check validation
        if (refreshToken != null && refreshToken.contains("\u0000")) {
            log.warn("[RedisStorageService] refreshToken contains null character!");
            refreshToken = refreshToken.replace("\u0000", "");
        }

        redisTemplate.opsForValue().set("RT:" + username, Objects.requireNonNull(refreshToken), expiration, TimeUnit.SECONDS);
    }

    /**
     * RefreshToken 조회
     * @param username
     * @return refreshToken or null
     */
    public String getStoredRefreshToken(String username) {
        return redisTemplate.opsForValue().get("RT:" + username);
    }

    /**
     * RefreshToken 삭제
     * @param username
     */
    public void deleteRefreshToken(String username) {
        redisTemplate.delete("RT:" + username);
    }
}
