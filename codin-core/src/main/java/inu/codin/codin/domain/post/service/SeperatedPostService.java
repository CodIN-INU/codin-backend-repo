package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.best.BestEntity;
import inu.codin.codin.domain.post.domain.best.BestRepository;
import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollVoteRepository;
import inu.codin.codin.domain.post.dto.response.PollInfoResponseDTO;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.scrap.service.ScrapService;
import inu.codin.codin.infra.redis.service.RedisBestService;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.infra.s3.exception.ImageRemoveException;
import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeperatedPostService {
    private final S3Service s3Service;
    private final PostRepository postRepository;
    private final HitsService hitsService;
    private final RedisBestService redisBestService;
    private final BestRepository bestRepository;
    private final ScrapService scrapService;
    private final LikeService likeService;
    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;

    // [ImageService] - 이미지 업로드 처리
    public List<String> handleImageUpload(List<MultipartFile> postImages) {
        return s3Service.handleImageUpload(postImages);
    }

    // [ImageService] - 게시글 이미지 삭제 처리
    public void deletePostImageInternal(PostEntity post, String imageUrl) {
        if (!post.getPostImageUrls().contains(imageUrl)) {
            log.error("게시물에 이미지 없음. PostId: {}, ImageUrl: {}", post.get_id(), imageUrl);
            throw new NotFoundException("이미지가 게시물에 존재하지 않습니다.");
        }
        try {
            s3Service.deleteFile(imageUrl);
            post.removePostImage(imageUrl);
            postRepository.save(post);
            log.info("이미지 삭제 성공. PostId: {}, ImageUrl: {}", post.get_id(), imageUrl);
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생. PostId: {}, ImageUrl: {}", post.get_id(), imageUrl, e);
            throw new ImageRemoveException("이미지 삭제 중 오류 발생: " + imageUrl);
        }
    }


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



    // [likeService] - 게시글 좋아요 수 조회
    public int getLikeCount(PostEntity post) {
        return likeService.getLikeCount(LikeType.POST, post.get_id());
    }

    // [ScrapService] - 게시글 스크랩 수 조회
    public int getScrapCount(PostEntity post) {
        return scrapService.getScrapCount(post.get_id());
    }

    // [HitsService] - 게시글 조회수 조회
    public int getHitsCount(PostEntity post) {
        return hitsService.getHitsCount(post.get_id());
    }


    // [HitsService] - 조회수 증가 처리
    public void increaseHitsIfNeeded(PostEntity post, ObjectId userId) {
        if (!hitsService.validateHits(post.get_id(), userId)) {
            hitsService.addHits(post.get_id(), userId);
            log.info("조회수 업데이트. PostId: {}, UserId: {}", post.get_id(), userId);
        }
    }

}
