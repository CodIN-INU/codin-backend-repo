package inu.codin.codin.infra.redis.service;

import inu.codin.codin.common.security.dto.PortalLoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisAuthService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final long EXPIRE_TIME = 5;

    // 데이터 저장
    public void saveUserData(String studentId, PortalLoginResponseDto userData) {
        redisTemplate.opsForValue().set(studentId, userData, EXPIRE_TIME, TimeUnit.MINUTES); // 5분 후 자동 삭제
    }

    // 데이터 조회
    public PortalLoginResponseDto getUserData(String studentId) {
        return (PortalLoginResponseDto) redisTemplate.opsForValue().get(studentId);
    }

}
