package inu.codin.codin.common.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisStorageService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * RefreshToken 저장
     * @param username
     * @param refreshToken
     * @param expiration
     */
    public void saveRefreshToken(String username, String refreshToken, long expiration) {
        redisTemplate.opsForValue().set("RT:" + username, refreshToken, expiration);
    }

    /**
     * RefreshToken 조회
     * @param username
     * @return refreshToken
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
