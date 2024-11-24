package inu.codin.codin.domain.like;

import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostRepository postRepository; // MongoDB의 게시글 저장소

    public void addLike(String postId, String userId) {
        String redisKey = "post:likes:" + postId;

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(redisKey, userId))) {
            throw new IllegalStateException("이미 해당 게시물에 좋아요를 눌렀습니다.");
        }

        // Redis에 좋아요 추가
        redisTemplate.opsForSet().add(redisKey, userId);

    }

    public void removeLike(String postId, String userId) {
        String redisKey = "post:likes:" + postId;

        if (!Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(redisKey, userId))) {
            throw new IllegalStateException("해당 게시물에 좋아요를 누르지 않았습니다.");
        }

        // Redis에서 좋아요 제거
        redisTemplate.opsForSet().remove(redisKey, userId);

    }

    public long getLikeCount(String postId) {
        String redisKey = "post:likes:" + postId;
        return redisTemplate.opsForSet().size(redisKey);
    }

    public boolean isPostLikedByUser(String postId, String userId) {
        String redisKey = "post:likes:" + postId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(redisKey, userId));
    }
}