package inu.codin.codin.domain.post.domain.best;

import inu.codin.codin.infra.redis.service.RedisBestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BestService {
    private final RedisBestService redisBestService;
    private final BestRepository bestRepository;

    // [BestService] - Top 3 베스트 postId 목록 반환
    public List<String> getTop3BestPostIds() {
        Map<String, Double> bestPosts = redisBestService.getBests();
        return new ArrayList<>(bestPosts.keySet()); // 빈 리스트 반환 가능
    }

    // [BestService] - BestEntity 페이지 반환
    public Page<BestEntity> getBestEntities(int pageNumber) {
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        return bestRepository.findAll(pageRequest);
    }

    // Redis에서 특정 베스트 게시물 삭제
    public void deleteBestPost(String postId) {
        redisBestService.deleteBest(postId);
    }

    // [BestService] - 베스트 점수 적용 처리 래핑
    public void applyBestScore(ObjectId postId) {
        redisBestService.applyBestScore(1, postId);
        log.info("베스트 점수 적용. PostId: {}", postId);
    }

}
