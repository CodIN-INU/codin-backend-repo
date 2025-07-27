package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.domain.lecture.dto.LectureDetailResponseDto;
import inu.codin.codin.domain.lecture.dto.LecturePageResponse;
import inu.codin.codin.domain.lecture.dto.LecturePreviewResponseDto;
import inu.codin.codin.domain.lecture.dto.LectureSearchListResponseDto;
import inu.codin.codin.domain.lecture.entity.Emotion;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.Semester;
import inu.codin.codin.domain.lecture.entity.SortingOption;
import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import inu.codin.codin.domain.lecture.exception.SemesterErrorCode;
import inu.codin.codin.domain.lecture.exception.SemesterException;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.lecture.repository.LectureSearchRepositoryCustom;
import inu.codin.codin.domain.like.dto.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.review.service.UserReviewStatsService;
import inu.codin.codin.global.common.entity.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureSearchRepositoryCustom lectureSearchRepository;

    private final EmotionService emotionService;
    private final SemesterService semesterService;
    private final UserReviewStatsService userReviewStatsService;
    private final LikeService likeService;

    /**
     * 여러 옵션을 선택하여 강의 리스트 반환
     *
     * @param keyword
     * @param department    Department (COMPUTER_SCI, INFO_COMM, EMBEDDED)
     * @param sortingOption 평점 많은 순, 좋아요 많은 순, 조회수 순 중 내림차순 선택
     * @param like
     * @param page          페이지 번호
     * @return LecturePageResponse
     */
    public LecturePageResponse sortListOfLectures(String keyword, Department department, SortingOption sortingOption, Boolean like, int page) {
        Page<Lecture> lecturePage = lectureSearchRepository.searchLecturesAtPreview(keyword, department, sortingOption, like, PageRequest.of(page, 10));
        return getLecturePageResponse(lecturePage);
    }

    /**
     * 강의의 상세 별점 반환
     * @param lectureId 강의 id
     * @return LectureDetailResponseDto
     */
    @Transactional
    public LectureDetailResponseDto getLectureDetails(Long lectureId) {
        Lecture lecture = lectureRepository.findLectureWithScheduleAndTagsById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorCode.LECTURE_NOT_FOUND));
        lecture.increaseHits();
        Emotion emotion = emotionService.getOrMakeEmotion(lecture);
        return LectureDetailResponseDto.of(lecture, emotion, userReviewStatsService.isOpenKeyword());
    }

    /**
     * 페이지로 반환된 LectureEntity -> Dto 변환
     */
    private LecturePageResponse getLecturePageResponse(Page<Lecture> lecturePage) {
        return LecturePageResponse.of(lecturePage.stream()
                        .map(lecture -> {
                                    boolean liked = likeService.isLiked(LikeType.LECTURE, lecture.getId().toString());
                                    return LecturePreviewResponseDto.of(lecture, liked);
                        }).toList(),
                lecturePage.getTotalPages() - 1,
                lecturePage.hasNext() ? lecturePage.getPageable().getPageNumber() + 1 : -1);
    }

    /**
     * 강의 후기를 작성할 강의 목록 검색
     * @param department Department (COMPUTER_SCI, INFO_COMM, EMBEDDED)
     * @param grade 학년 (1,2,3,4)
     * @param semester 수강 학기 (23-1, 23-2,,, 현재 학기)
     * @return List<LectureSearchListResponseDto> 검색 결과 리스트 반환
     */
    public List<LectureSearchListResponseDto> searchLecturesToReview(Department department, Integer grade, String semester) {
        Semester semesterEntity = (semester != null)
                        ? semesterService.getSemester(semester)
                                .orElseThrow(() -> new SemesterException(SemesterErrorCode.SEMESTER_NOT_FOUND))
                        : null;

        List<Lecture> lectures = lectureSearchRepository.searchLecturesAtReview(department, grade, semesterEntity);

        return (semesterEntity != null)
                ? lectures.stream()
                        .map(lecture -> LectureSearchListResponseDto.of(lecture, semester))
                        .toList()
                : lectures.stream()
                        .map(LectureSearchListResponseDto::of)
                        .flatMap(List::stream)
                        .toList();
    }

}
