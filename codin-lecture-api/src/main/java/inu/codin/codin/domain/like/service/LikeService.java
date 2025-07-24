package inu.codin.codin.domain.like.service;

import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.like.controller.LikeFeignClient;
import inu.codin.codin.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.like.dto.LikeType;
import inu.codin.codin.domain.like.exception.LikeErrorCode;
import inu.codin.codin.domain.like.exception.LikeException;
import inu.codin.codin.domain.review.entity.Review;
import inu.codin.codin.domain.review.exception.ReviewErrorCode;
import inu.codin.codin.domain.review.exception.ReviewException;
import inu.codin.codin.domain.review.repository.ReviewRepository;
import inu.codin.codin.global.auth.util.SecurityUtils;
import inu.codin.codin.global.infra.redis.config.RedisHealthChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LectureRepository lectureRepository;
    private final ReviewRepository reviewRepository;

    private final LikeFeignClient likeFeignClient;

    private final RedisLikeService redisLikeService;
    private final RedisHealthChecker redisHealthChecker;
    private final String LIKE_ADD_MESSAGE = "좋아요가 추가되었습니다.";
    private final String LIKE_RECOVER_MESSAGE = "좋아요가 복구되었습니다.";
    private final String LIKE_REMOVE_MESSAGE = "좋아요가 삭제되었습니다.";

    @Transactional
    public String toggleLike(LikeRequestDto likeRequestDto) {
        String message = likeFeignClient.toggleLike(likeRequestDto).getBody().getMessage();
        boolean isLiked = parseLikeResponse(message);
        applyLikeChange(likeRequestDto, isLiked);
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
        return (int) likeFeignClient.getLikeCount(likeType, likeTypeId).getBody().getData();
    }

    public Boolean isLiked(LikeType likeType, String id) {
        String userId = SecurityUtils.getUserId();
        return (Boolean) likeFeignClient.isUserLiked(likeType, id, userId).getBody().getData();
    }

    private boolean parseLikeResponse(String message) {
        return switch (message) {
            case LIKE_ADD_MESSAGE, LIKE_RECOVER_MESSAGE -> true;
            case LIKE_REMOVE_MESSAGE -> false;
            default -> throw new LikeException(LikeErrorCode.LIKE_UNEXPECTED_MESSAGE, message);
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
