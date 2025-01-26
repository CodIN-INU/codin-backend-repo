package inu.codin.codin.domain.lecture.domain.review.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.lecture.domain.review.dto.CreateReviewRequestDto;
import inu.codin.codin.domain.lecture.domain.review.dto.ReviewListResposneDto;
import inu.codin.codin.domain.lecture.domain.review.dto.ReviewPageResponse;
import inu.codin.codin.domain.lecture.domain.review.entity.ReviewEntity;
import inu.codin.codin.domain.lecture.domain.review.exception.ReviewExistenceException;
import inu.codin.codin.domain.lecture.domain.review.exception.WrongRatingException;
import inu.codin.codin.domain.lecture.domain.review.repository.ReviewRepository;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.infra.redis.config.RedisHealthChecker;
import inu.codin.codin.infra.redis.service.RedisReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final LikeService likeService;
    private final RedisReviewService redisReviewService;
    private final RedisHealthChecker redisHealthChecker;

    public void createReview(String lectureId, CreateReviewRequestDto createReviewRequestDto) {
        if (createReviewRequestDto.getStarRating() > 5.0 || createReviewRequestDto.getStarRating() < 0.25){
            log.warn("잘못된 평점입니다. 0.25 ~ 5.0 사이의 점수를 입력해주세요");
            throw new WrongRatingException("잘못된 평점입니다. 0.25 ~ 5.0 사이의 점수를 입력해주세요.");
        }
        ObjectId userId = SecurityUtils.getCurrentUserId();
        Optional<ReviewEntity> review = reviewRepository.findByLectureIdAndUserId(new ObjectId(lectureId), userId);
        if (review.isPresent()) {
            log.error("이미 유저가 작성한 후기가 존재합니다. userId: {}, lectureId: {}", userId, lectureId);
            throw new ReviewExistenceException("이미 유저가 작성한 후기가 존재합니다.");
        }

        ReviewEntity newReview = ReviewEntity.of(createReviewRequestDto, new ObjectId(lectureId), userId);
        reviewRepository.save(newReview);
        if (redisHealthChecker.isRedisAvailable()) {
            redisReviewService.addReview(lectureId, createReviewRequestDto.getStarRating(), userId);
        }
    }

    public Object getListOfReviews(String lectureId, int page) {
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("created_at").descending());
        Page<ReviewEntity> reviewPage = reviewRepository.findAllByLectureId(new ObjectId(lectureId), pageRequest);

        ObjectId userId = SecurityUtils.getCurrentUserId();
        return ReviewPageResponse.of(reviewPage.stream()
                        .map(review -> ReviewListResposneDto.of(review,
                                likeService.isReviewLiked(review.get_id(), userId),
                                likeService.getLikeCount(LikeType.REVIEW, review.get_id()))).toList(),
                reviewPage.getTotalPages() -1,
                reviewPage.hasNext()? reviewPage.getPageable().getPageNumber() + 1: -1);
    }
}
