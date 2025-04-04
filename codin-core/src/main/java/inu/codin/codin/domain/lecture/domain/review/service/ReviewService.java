package inu.codin.codin.domain.lecture.domain.review.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.lecture.domain.review.dto.CreateReviewRequestDto;
import inu.codin.codin.domain.lecture.domain.review.dto.ReviewListResposneDto;
import inu.codin.codin.domain.lecture.domain.review.dto.ReviewPageResponse;
import inu.codin.codin.domain.lecture.domain.review.entity.ReviewEntity;
import inu.codin.codin.domain.lecture.domain.review.exception.ReviewExistenceException;
import inu.codin.codin.domain.lecture.domain.review.exception.WrongRatingException;
import inu.codin.codin.domain.lecture.domain.review.repository.ReviewRepository;
import inu.codin.codin.domain.lecture.dto.Emotion;
import inu.codin.codin.domain.lecture.entity.LectureEntity;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
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
    private final LectureRepository lectureRepository;

    private final LikeService likeService;

    /**
     * 새로운 강의 후기 작성
     * @param lectureId 후기를 작성하는 강의의 _id
     * @param createReviewRequestDto 후기 작성 시 포함되는 내용
     */
    public void createReview(ObjectId lectureId, CreateReviewRequestDto createReviewRequestDto) {
        if (createReviewRequestDto.getStarRating() > 5.0 || createReviewRequestDto.getStarRating() < 0.25){
            log.warn("잘못된 평점입니다. 0.25 ~ 5.0 사이의 점수를 입력해주세요");
            throw new WrongRatingException("잘못된 평점입니다. 0.25 ~ 5.0 사이의 점수를 입력해주세요.");
        }
        ObjectId userId = SecurityUtils.getCurrentUserId();
        Optional<ReviewEntity> review = reviewRepository.findByLectureIdAndUserIdAndDeletedAtIsNull(lectureId, userId);
        if (review.isPresent()) {
            log.error("이미 유저가 작성한 후기가 존재합니다. userId: {}, lectureId: {}", userId, lectureId);
            throw new ReviewExistenceException("이미 유저가 작성한 후기가 존재합니다.");
        }

        ReviewEntity newReview = ReviewEntity.of(createReviewRequestDto, lectureId, userId);
        reviewRepository.save(newReview);
        updateRating(lectureId);
        log.info("새로운 강의 후기 저장 - lectureId : {} userId : {}", lectureId, userId);
    }

    /**
     * 강의 후기 작성 시 해당 강의의 Rating 업데이트
     * @param lectureId 강의 _id
     */
    public void updateRating(ObjectId lectureId){
        LectureEntity lectureEntity = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new NotFoundException("강의 정보를 찾을 수 없습니다."));
        double starRating = reviewRepository.getAvgRatingByLectureId(lectureId);
        Emotion emotion = reviewRepository.getEmotionsCountByRanges(lectureId).changeToPercentage();
        int participants = reviewRepository.countByLectureId(lectureId);
        lectureEntity.updateReviewRating(starRating, participants, emotion);
        lectureRepository.save(lectureEntity);
    }


    /**
     * 해당 강의의 수강 후기 리스트 Page로 가져오기
     * @param lectureId 강의 _id
     * @param page 페이지 번호
     * @return ReviewPageResponse 강의 후기 Page 반환
     */
    public ReviewPageResponse getListOfReviews(String lectureId, int page) {
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("created_at").descending());
        Page<ReviewEntity> reviewPage = reviewRepository.getAvgRatingByLectureId(new ObjectId(lectureId), pageRequest);

        ObjectId userId = SecurityUtils.getCurrentUserId();
        return ReviewPageResponse.of(reviewPage.stream()
                        .map(review -> ReviewListResposneDto.of(review,
                                likeService.isLiked(LikeType.REVIEW, review.get_id(), userId),
                                likeService.getLikeCount(LikeType.REVIEW, review.get_id()))).toList(),
                reviewPage.getTotalPages() -1,
                reviewPage.hasNext()? reviewPage.getPageable().getPageNumber() + 1: -1);
    }
}
