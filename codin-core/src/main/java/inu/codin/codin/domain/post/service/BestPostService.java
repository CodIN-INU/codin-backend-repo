package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.post.domain.best.BestEntity;
import inu.codin.codin.domain.post.domain.best.BestRepository;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.infra.redis.service.RedisBestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BestPostService {
    private final RedisBestService redisBestService;
    private final BestRepository bestRepository;
    private final PostRepository postRepository;

    // [BestService] - Top 3 베스트 게시물 조회
    public List<PostEntity> getTop3BestPostsInternal() {
        Map<String, Double> posts = redisBestService.getBests();
        return posts.entrySet().stream()
                .map(post -> postRepository.findByIdAndNotDeleted(new ObjectId(post.getKey()))
                        .orElseGet(() -> {
                            redisBestService.deleteBest(post.getKey());
                            return null;
                        }))
                .filter(Objects::nonNull)
                .toList();
    }

    // [BestService] - 베스트 게시물 페이지 조회
    public Page<PostEntity> getBestPostsInternal(int pageNumber) {
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<BestEntity> bests = bestRepository.findAll(pageRequest);
        return bests.map(bestEntity -> postRepository.findByIdAndNotDeleted(bestEntity.getPostId())
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다.")));
    }

    // [BestService] - 베스트 점수 적용 처리
    public void applyBestScoreIfNeeded(PostEntity post) {
        redisBestService.applyBestScore(1, post.get_id());
        log.info("베스트 점수 적용. PostId: {}", post.get_id());
    }

}
