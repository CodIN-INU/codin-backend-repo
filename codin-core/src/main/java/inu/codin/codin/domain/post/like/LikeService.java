package inu.codin.codin.domain.post.like;

import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final RedisService redisService;

    public void addLike(String entityType, String entityId, String userId) {
        redisService.addLike(entityType, entityId, userId);
    }

    public void removeLike(String entityType, String entityId, String userId) {
        redisService.removeLike(entityType, entityId, userId);
    }
}