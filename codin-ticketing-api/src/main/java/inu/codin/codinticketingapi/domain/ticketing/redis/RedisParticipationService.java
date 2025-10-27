package inu.codin.codinticketingapi.domain.ticketing.redis;

import inu.codin.codinticketingapi.domain.ticketing.dto.event.ParticipationStatusChangedEvent;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.dto.event.ParticipationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisParticipationService {

    private final RedisTemplate<String, ParticipationResponse> participationRedisTemplate;

    private static final String CACHE_KEY_PREFIX = "participation:";
    private static final Duration CACHE_TTL = Duration.ofHours(3);

    /**
     * 티켓팅 참여 정보 캐시 저장
     */
    public void cacheParticipation(String userId, Long eventId, Participation participation) {
        String cacheKey = generateCacheKey(userId, eventId);
        ParticipationResponse response = ParticipationResponse.of(participation);
        participationRedisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
        log.debug("participation Cached participation: userId={}, eventId={}", userId, eventId);
    }

    /**
     * 티켓팅 참여 정보 캐시 조회
     */
    public Optional<ParticipationResponse> getCachedParticipation(String userId, Long eventId) {
        String cacheKey = generateCacheKey(userId, eventId);
        try {
            ParticipationResponse cached = participationRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("participation Cache hit: userId={}, eventId={}", userId, eventId);
                return Optional.of(cached);
            }
        } catch (Exception e) {
            log.warn("participation Cache read error: userId={}, eventId={}, error={}", userId, eventId, e.getMessage());
        }
        log.debug("participation Cache miss: userId={}, eventId={}", userId, eventId);
        return Optional.empty();
    }

    /**
     * 티켓팅 참여 정보 캐시 삭제
     */
    public void evictParticipation(String userId, Long eventId) {
        String cacheKey = generateCacheKey(userId, eventId);
        participationRedisTemplate.delete(cacheKey);
        log.debug("participation Evicted cache: userId={}, eventId={}", userId, eventId);
    }

    /**
     * 티켓팅 참여 생성 이벤트 처리
     */
    @EventListener
    public void handleParticipationCreated(ParticipationCreatedEvent event) {
        Participation participation = event.getParticipation();
        cacheParticipation(participation.getUserId(), participation.getEvent().getId(), participation);
    }

    /**
     * 티켓팅 참여 상태 변경 이벤트 처리
     */
    @EventListener
    public void handleParticipationStatusChanged(ParticipationStatusChangedEvent event) {
        Participation participation = event.getParticipation();
        cacheParticipation(participation.getUserId(), participation.getEvent().getId(), participation);
    }

    private String generateCacheKey(String userId, Long eventId) {
        return CACHE_KEY_PREFIX + userId + ":" + eventId;
    }
}
