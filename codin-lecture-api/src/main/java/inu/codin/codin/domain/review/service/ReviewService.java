package inu.codin.codin.domain.review.service;


import inu.codin.codin.domain.lecture.entity.Emotion;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.Semester;
import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.lecture.repository.SemesterRepository;
import inu.codin.codin.domain.review.dto.CreateReviewRequestDto;
import inu.codin.codin.domain.review.entity.Review;
import inu.codin.codin.domain.review.exception.ReviewErrorCode;
import inu.codin.codin.domain.review.exception.ReviewException;
import inu.codin.codin.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final LectureRepository lectureRepository;
    private final SemesterRepository semesterRepository;

//    private final LikeService likeService;

    /**
     * 새로운 강의 후기 작성
     * @param lectureId 후기를 작성하는 강의의 _id
     * @param createReviewRequestDto 후기 작성 시 포함되는 내용
     */
    @Transactional
    public void createReview(Long lectureId, CreateReviewRequestDto createReviewRequestDto) {
        if (createReviewRequestDto.getStarRating() > 5.0 || createReviewRequestDto.getStarRating() < 0.25){
            throw new ReviewException(ReviewErrorCode.REVIEW_WRONG_RATING);
        }
        Lecture lecture = lectureRepository.findLectureWithSemesterAndReviewsById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorCode.LECTURE_NOT_FOUND));
//        Long userId = SecurityUtils.getCurrentUserId();
        Long userId = 1L;
        checkReviewExisted(lectureId, userId, lecture);

        Semester semester = getSemester(createReviewRequestDto, lecture);
        Review newReview = Review.of(createReviewRequestDto, semester, lecture, userId);
        reviewRepository.save(newReview);
        updateRating(lecture);
        log.info("새로운 강의 후기 저장 - lectureId : {} userId : {}", lectureId, userId);
    }

    private void checkReviewExisted(Long lectureId, Long userId, Lecture lecture) {
        boolean isExisted = reviewRepository.existsByUserIdAndLectureAndDeletedAtIsNull(userId, lecture);
        if (isExisted) {
            log.error("이미 유저가 작성한 후기가 존재합니다. userId: {}, lectureId: {}", userId, lectureId);
            throw new ReviewException(ReviewErrorCode.REVIEW_ALREADY_EXISTED);
        }
    }

    private Semester getSemester(CreateReviewRequestDto createReviewRequestDto, Lecture lecture) {
        Integer year = Integer.parseInt(createReviewRequestDto.getSemester().split("-")[0]);
        Integer quarter = Integer.parseInt(createReviewRequestDto.getSemester().split("-")[1]);
        Semester semester = semesterRepository.findSemesterByYearAndQuarter(year, quarter)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.REVIEW_WRONG_SEMESTER));
        if (lecture.getSemester().stream().noneMatch(lectureSemester -> lectureSemester.getSemester().equals(semester)))
            throw new ReviewException(ReviewErrorCode.REVIEW_WRONG_SEMESTER);
        return semester;
    }

    /**
     * 강의 후기 작성 시 해당 강의의 Rating 업데이트
     * @param lecture
     */
    public void updateRating(Lecture lecture){
        double starRating = reviewRepository.getAvgOfStarRatingByLecture(lecture);
        Emotion emotion = reviewRepository.getEmotionsCountByRange(lecture.getId()).changeToPercentage();
        lecture.updateReviewRating(starRating, emotion);
    }


    /**
     * 해당 강의의 수강 후기 리스트 Page로 가져오기
     * @param lectureId 강의 _id
     * @param page 페이지 번호
     * @return ReviewPageResponse 강의 후기 Page 반환
     */
//    public ReviewPageResponse getListOfReviews(Long lectureId, int page) {
//        Lecture lecture = lectureRepository.findById(lectureId)
//                .orElseThrow(() -> new LectureException(LectureErrorCode.LECTURE_NOT_FOUND));
//        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("created_at").descending());
//        Page<Review> reviewPage = reviewRepository.findReviewsByLecture(lecture, pageRequest);
//
//        Long userId = SecurityUtils.getCurrentUserId();
//        return ReviewPageResponse.of(reviewPage.stream()
//                        .map(review -> ReviewListResposneDto.of(review,
//                                likeService.isLiked(LikeType.REVIEW, review.get_id(), userId),
//                                likeService.getLikeCount(LikeType.REVIEW, review.get_id()))).toList(),
//                reviewPage.getTotalPages() -1,
//                reviewPage.hasNext()? reviewPage.getPageable().getPageNumber() + 1: -1);
//    }
}
