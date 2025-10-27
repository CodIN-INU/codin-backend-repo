package inu.codin.codin.domain.review.service;


import inu.codin.codin.domain.elasticsearch.service.LectureElasticService;
import inu.codin.codin.domain.lecture.entity.Emotion;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.Semester;
import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.lecture.service.EmotionService;
import inu.codin.codin.domain.lecture.service.SemesterService;
import inu.codin.codin.domain.like.dto.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.review.dto.CreateReviewRequestDto;
import inu.codin.codin.domain.review.dto.ReviewListResponseDto;
import inu.codin.codin.domain.review.dto.ReviewPageResponse;
import inu.codin.codin.domain.review.entity.Review;
import inu.codin.codin.domain.review.exception.ReviewErrorCode;
import inu.codin.codin.domain.review.exception.ReviewException;
import inu.codin.codin.domain.review.repository.ReviewRepository;
import inu.codin.codin.global.auth.util.SecurityUtils;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final LectureRepository lectureRepository;
    private final LectureElasticService lectureElasticService;

    private final EmotionService emotionService;
    private final LikeService likeService;
    private final UserReviewStatsService userReviewStatsService;
    private final SemesterService semesterService;


    /**
     * 새로운 강의 후기 작성
     * @param lectureId 후기를 작성하는 강의의 pk
     * @param createReviewRequestDto 후기 작성 시 포함되는 내용
     */
    @Transactional
    public void createReview(Long lectureId, CreateReviewRequestDto createReviewRequestDto) {
        validateRating(createReviewRequestDto);

        Lecture lecture = lectureRepository.findLectureWithSemesterAndReviewsById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorCode.LECTURE_NOT_FOUND));

        String userId = SecurityUtils.getUserId();
        checkReviewExisted(lectureId, userId, lecture); //리뷰를 작성한 상태인지 확인

        Semester semester = getSemester(createReviewRequestDto, lecture);
        Review newReview = Review.of(createReviewRequestDto, semester, lecture, userId);

        reviewRepository.save(newReview);
        updateRating(lecture, createReviewRequestDto.getStarRating()); //과목의 평점 업데이트
        userReviewStatsService.updateStats(userId); //유저의 리뷰 작성 현황 업데이트

        log.info("새로운 강의 후기 저장 - lectureId : {} userId : {}", lectureId, userId);
    }

    private Semester getSemester(CreateReviewRequestDto createReviewRequestDto, Lecture lecture) {
        Semester semester = semesterService.getSemester(createReviewRequestDto.getSemester())
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_WRONG_SEMESTER));
        isLectureInSemester(lecture, semester);
        return semester;
    }

    private void isLectureInSemester(Lecture lecture, Semester semester) {
        boolean isLectureInSemester = lecture.getSemester().stream().anyMatch(s -> s.getSemester().equals(semester));
        if (!isLectureInSemester) {
            throw new ReviewException(ReviewErrorCode.REVIEW_WRONG_SEMESTER);
        }
    }

    /**
     * 강의 후기 작성 시 해당 강의의 Rating 업데이트
     *
     * @param lecture 강의 엔티티
     * @param starRating 별점
     */
    private void updateRating(Lecture lecture, @NotNull @Digits(integer = 1, fraction = 2) double starRating){
        double avgOfStarRating = reviewRepository.getAvgOfStarRatingByLecture(lecture);
        Emotion emotion = getEmotion(lecture, starRating);
        lecture.updateReviewRating(avgOfStarRating, emotion);
        lectureElasticService.updateStarRating(lecture.getId(), avgOfStarRating);
    }

    private Emotion getEmotion(Lecture lecture, double starRating) {
        Emotion emotion = emotionService.getOrMakeEmotion(lecture);
        emotion.updateScore(starRating);
        return emotion;
    }


    /**
     * 해당 강의의 수강 후기 리스트 Page로 5개씩 가져오기
     * @param lectureId 강의 pk
     * @param page 페이지 번호
     * @return ReviewPageResponse 강의 후기 Page 반환
     */
    public ReviewPageResponse getListOfReviews(Long lectureId, int page) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorCode.LECTURE_NOT_FOUND));
        PageRequest pageRequest = PageRequest.of(page, 5, Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewRepository.findReviewsByLecture(lecture, pageRequest);

        List<ReviewListResponseDto> reviewDtos = reviewPage.stream()
                .map(this::toResponseDto).toList();

        return ReviewPageResponse.of(
                reviewDtos,
                reviewPage.getTotalPages() -1,
                reviewPage.hasNext()? reviewPage.getPageable().getPageNumber() + 1: -1);
    }

    private ReviewListResponseDto toResponseDto(Review review) {
        String reviewId = review.getId().toString();
        boolean liked = likeService.isLiked(LikeType.LECTURE, reviewId);
        int likeCount = likeService.getLikeCount(LikeType.REVIEW, reviewId);
        return ReviewListResponseDto.of(review, liked, likeCount);
    }

    private void validateRating(CreateReviewRequestDto createReviewRequestDto) {
        if (createReviewRequestDto.getStarRating() > 5.0 || createReviewRequestDto.getStarRating() < 0.25){
            throw new ReviewException(ReviewErrorCode.REVIEW_WRONG_RATING);
        }
    }

    private void checkReviewExisted(Long lectureId, String userId, Lecture lecture) {
        boolean isExisted = reviewRepository.existsByUserIdAndLectureAndDeletedAtIsNull(userId, lecture);
        if (isExisted) {
            log.error("이미 유저가 작성한 후기가 존재합니다. userId: {}, lectureId: {}", userId, lectureId);
            throw new ReviewException(ReviewErrorCode.REVIEW_ALREADY_EXISTED);
        }
    }
}
