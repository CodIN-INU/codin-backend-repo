package inu.codin.codin.domain.scrap;

import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final RedisTemplate<String, String> redisTemplate;

    public void addScrap(String userId, String postId) {
        String redisKey = "user:scraps:" + userId;

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(redisKey, postId))) {
            throw new IllegalStateException("이미 해당 게시물을 스크랩 했습니다.");
        }

        // Redis에 스크랩 추가
        redisTemplate.opsForSet().add(redisKey, postId);

    }

    public void removeScrap(String userId, String postId) {
        String redisKey = "user:scraps:" + userId;

        if (!Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(redisKey, postId))) {
            throw new IllegalStateException("해당 게시물을 스크랩 하지 않았습니다.");
        }

        // Redis에서 스크랩 제거
        redisTemplate.opsForSet().remove(redisKey, postId);

    }

    public long getScrapCount(String postId) {
        return redisTemplate.opsForSet().size("scraps:" + postId);
    }

    public Set<String> getScrappedPosts(String userId) {
        String redisKey = "user:scraps:" + userId;
        return redisTemplate.opsForSet().members(redisKey);
    }
}