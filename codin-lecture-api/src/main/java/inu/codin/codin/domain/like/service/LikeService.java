package inu.codin.codin.domain.like.service;

import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.like.controller.LikeFeignClient;
import inu.codin.codin.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.like.dto.LikeResponseType;
import inu.codin.codin.domain.like.dto.LikeType;
import inu.codin.codin.domain.review.entity.Review;
import inu.codin.codin.domain.review.exception.ReviewErrorCode;
import inu.codin.codin.domain.review.exception.ReviewException;
import inu.codin.codin.domain.review.repository.ReviewRepository;
import inu.codin.codin.global.auth.util.SecurityUtils;
import inu.codin.codin.global.infra.redis.config.RedisHealthChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeService {

    private final LectureRepository lectureRepository;
    private final ReviewRepository reviewRepository;

    private final LikeFeignClient likeFeignClient;

    private final RedisLikeService redisLikeService;
    private final RedisHealthChecker redisHealthChecker;

    @Transactional
    public LikeResponseType toggleLike(LikeRequestDto likeRequestDto) {
        LikeResponseType message = null;
        try {
            message = LikeResponseType.valueOf((String) likeFeignClient.toggleLike(likeRequestDto).getBody().getData());
            boolean isLiked = parseLikeResponse(message);
            applyLikeChange(likeRequestDto, isLiked);
            return message;
        } catch (Exception e){ //만약 오류가 났다면 좋아요 추가/취소 (토글) 기능 진행
            log.error("[toggleLike] 예외 발생으로 보상 트랜잭션이 진행됩니다. {}", e.getMessage());
            if (message != null) message = LikeResponseType.valueOf((String) likeFeignClient.toggleLike(likeRequestDto).getBody().getData());
        }
        return message;
    }

    public int getLikeCount(LikeType likeType, String likeTypeId) {
        //Redis가 사용 가능하면 Redis 시도
        if (redisHealthChecker.isRedisAvailable()) {
            Object redisResult = redisLikeService.getLikeCount(likeType.name(), likeTypeId);
            if (redisResult != null)
                return Integer.parseInt(String.valueOf(redisResult));
        }
        //Redis가 꺼져 있거나 cache가 없을 경우 -> Main 서버 요청
        return likeFeignClient.getLikeCount(likeType, likeTypeId);
    }

    public Boolean isLiked(LikeType likeType, String id) {
        String userId = SecurityUtils.getUserId();
        return likeFeignClient.isUserLiked(likeType, id, userId);
    }

    private boolean parseLikeResponse(LikeResponseType message) {
        return switch (message) {
            case ADD, RECOVER -> true;
            case REMOVE -> false;
        };
    }

    private void applyLikeChange(LikeRequestDto dto, boolean isLiked) {
        Long targetId = Long.parseLong(dto.getId());
        switch (dto.getLikeType()) {
            case LECTURE -> applyLikeToLecture(targetId, isLiked);
            case REVIEW -> applyLikeToReview(targetId, isLiked);
        }
    }

    private void applyLikeToLecture(Long lectureId, boolean isLiked) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorCode.LECTURE_NOT_FOUND));
        if (isLiked) {
            lecture.increaseLikes();
        } else {
            lecture.decreaseLikes();
        }
    }

    private void applyLikeToReview(Long reviewId, boolean isLiked) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND));
        if (isLiked) {
            review.increaseLikes();
        } else {
            review.decreaseLikes();
        }
    }
}
