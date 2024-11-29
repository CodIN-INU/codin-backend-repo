package inu.codin.codin.infra.redis;

import inu.codin.codin.domain.post.like.LikeEntity;
import inu.codin.codin.domain.post.like.LikeRepository;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.scrap.ScrapEntity;
import inu.codin.codin.domain.post.scrap.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class SyncScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final ScrapRepository scrapRepository;

    @Scheduled(fixedRate = 60000) // 매 1분마다 실행
    public void syncLikesAndScraps() {
        syncLikes();
        syncScraps();
    }

    private void syncLikes() {
        Set<String> keys = redisTemplate.keys("post:likes:*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            String postId = key.replace("post:likes:", "");
            Set<String> users = redisTemplate.opsForSet().members(key);

            if (users != null && !users.isEmpty()) {
                // LikeEntity 동기화
                users.forEach(userId -> {
                    if (!likeRepository.findByPostIdAndUserId(postId, userId)) {
                        likeRepository.save(new LikeEntity(postId, userId));
                    }
                });

                // PostEntity의 likeCount 업데이트
                PostEntity post = postRepository.findById(postId)
                        .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
                int newLikeCount = users.size();
                if (post.getLikeCount() != newLikeCount) { // 기존 카운트와 다를 경우만 업데이트
                    post.updateLikeCount(newLikeCount);
                    postRepository.save(post);
                }
            }
        }
    }

    private void syncScraps() {
        Set<String> keys = redisTemplate.keys("user:scraps:*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            String userId = key.replace("user:scraps:", "");
            Set<String> posts = redisTemplate.opsForSet().members(key);

            if (posts != null && !posts.isEmpty()) {
                // ScrapEntity 동기화
                posts.forEach(postId -> {
                    if (!scrapRepository.findByPostIdAndUserId(postId, userId)) {
                        scrapRepository.save(new ScrapEntity(postId, userId));
                    }
                });

                // PostEntity의 scrapCount 업데이트
                posts.forEach(postId -> {
                    PostEntity post = postRepository.findById(postId)
                            .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
                    int newScrapCount = redisTemplate.opsForSet().size("post:scraps:" + postId).intValue();
                    if (post.getScrapCount() != newScrapCount) { // 기존 카운트와 다를 경우만 업데이트
                        post.updateScrapCount(newScrapCount);
                        postRepository.save(post);
                    }
                });
            }
        }
    }

}
