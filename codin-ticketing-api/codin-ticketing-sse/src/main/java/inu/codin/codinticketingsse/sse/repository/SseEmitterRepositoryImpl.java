package inu.codin.codinticketingsse.sse.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class SseEmitterRepositoryImpl implements SseEmitterRepository {

    // 사용자별 + 이벤트별 Emitter 관리 <Key, SseEmitter>
    private final ConcurrentHashMap<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    // 이벤트별 구독자 키 관리 <eventId, Set<Key>>
    private final ConcurrentHashMap<Long, Set<String>> eventSubscribers = new ConcurrentHashMap<>();

    @Override
    public SseEmitter saveEmitter(Long eventId, String userId, SseEmitter emitter) {
        String key = generateKey(eventId, userId);
        emitterMap.put(key, emitter);
        eventSubscribers.computeIfAbsent(eventId, k -> ConcurrentHashMap.newKeySet()).add(key);
        return emitter;
    }

    @Override
    public SseEmitter findEmitter(Long eventId, String userId) {
        String key = generateKey(eventId, userId);
        return emitterMap.get(key);
    }

    @Override
    public List<SseEmitter> findAll(Long eventId) {
        Set<String> keys = eventSubscribers.get(eventId);
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }

        return keys.stream()
                .map(emitterMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByEventIdAndEmitter(Long eventId, SseEmitter emitter) {
        Set<String> keys = eventSubscribers.get(eventId);
        if (keys != null) {
            keys.removeIf(key -> {
                SseEmitter stored = emitterMap.get(key);
                if (stored == emitter) {
                    emitterMap.remove(key);
                    return true;
                }
                return false;
            });

            if (keys.isEmpty()) {
                eventSubscribers.remove(eventId);
            }
        }
    }

    @Override
    public void removeEmitter(Long eventId, String userId) {
        String key = generateKey(eventId, userId);
        emitterMap.remove(key);

        Set<String> keys = eventSubscribers.get(eventId);
        if (keys != null) {
            keys.remove(key);
            if (keys.isEmpty()) {
                eventSubscribers.remove(eventId);
            }
        }
    }

    @Override
    public ConcurrentHashMap<String, SseEmitter> getAllEmitters() {
        return new ConcurrentHashMap<>(emitterMap);
    }

    @Override
    public int getActiveConnectionCount() {
        return emitterMap.size();
    }

    private String generateKey(Long eventId, String userId) {
        return eventId + ":" + userId;
    }
}