package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.domain.elasticsearch.document.LectureDocument;
import inu.codin.codin.domain.elasticsearch.service.LectureElasticService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureSearchRepositoryCustom lectureSearchRepository;

    private final LikeService likeService;
    private final EmotionService emotionService;
    private final SemesterService semesterService;
    private final LectureElasticService lectureElasticService;
    private final UserReviewStatsService userReviewStatsService;

    /**
     * 여러 옵션을 선택하여 강의 리스트 반환
     *
     * @param keyword       검색 키워드
     * @param department    Department(COMPUTER_SCI, INFO_COMM, EMBEDDED), null 시에 전체 검색
     * @param sortingOption 정확도 순, 평점 많은 순, 좋아요 많은 순, 조회수 순 중 내림차순 선택
     * @param like          좋아요 목록 토글
     * @param page          페이지 번호
     * @return LecturePageResponse
     */
    public LecturePageResponse sortListOfLectures(String keyword, Department department, SortingOption sortingOption, Boolean like, int page) {
        // 조회 유저의 좋아요 목록을 조회해 반환
        List<Long> likeList = null;

        if (like != null && like) {
            likeList = likeService.getLiked(LikeType.LECTURE).stream()
                    .map(likedResponseDto -> Long.valueOf(likedResponseDto.getLikeTypeId()))
                    .toList();
        }

        Page<LectureDocument> lecturePage = lectureSearchRepository.searchLecturesAtPreview(keyword, department, sortingOption, likeList, PageRequest.of(page, 10), like);

        return getLecturePageResponse(lecturePage);
    }

    /**
     * 강의의 상세 별점 반환
     *
     * @param lectureId 강의 id
     * @return LectureDetailResponseDto
     */
    @Transactional
    public LectureDetailResponseDto getLectureDetails(Long lectureId) {
        Lecture lecture = lectureRepository.findLectureWithScheduleAndTagsById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorCode.LECTURE_NOT_FOUND));
        lecture.increaseHits();
        Emotion emotion = emotionService.getOrMakeEmotion(lecture);

        lectureElasticService.incrementHits(lectureId);

        return LectureDetailResponseDto.of(lecture, emotion, userReviewStatsService.isOpenKeyword());
    }

    /**
     * 페이지로 반환된 LectureEntity -> Dto 변환
     */
    private LecturePageResponse getLecturePageResponse(Page<LectureDocument> lecturePage) {

        return LecturePageResponse.of(lecturePage.stream()
                        .map(lectureDocument -> {
                                    int likeCount = likeService.getLikeCount(LikeType.LECTURE, String.valueOf(lectureDocument.getId()));

                                    return LecturePreviewResponseDto.of(lectureDocument, likeCount);
                                }
                        )
                        .toList(),
                lecturePage.getTotalPages() - 1,
                lecturePage.hasNext() ? lecturePage.getPageable().getPageNumber() + 1 : -1);
    }

    /**
     * 강의 후기를 작성할 강의 목록 검색
     *
     * @param department Department (COMPUTER_SCI, INFO_COMM, EMBEDDED)
     * @param grade      학년 (1,2,3,4)
     * @param semester   수강 학기 (23-1, 23-2,,, 현재 학기)
     * @return List<LectureSearchListResponseDto> 검색 결과 리스트 반환
     */
    public List<LectureSearchListResponseDto> searchLecturesToReview(Department department, Integer grade, String semester) {
        Semester semesterEntity = (semester != null) ? semesterService.getSemester(semester).orElseThrow(() -> new SemesterException(SemesterErrorCode.SEMESTER_NOT_FOUND)) : null;
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
