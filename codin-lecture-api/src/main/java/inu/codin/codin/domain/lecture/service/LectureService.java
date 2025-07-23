package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.domain.lecture.dto.LectureDetailResponseDto;
import inu.codin.codin.domain.lecture.dto.LecturePageResponse;
import inu.codin.codin.domain.lecture.dto.LecturePreviewResponseDto;
import inu.codin.codin.domain.lecture.dto.LectureSearchListResponseDto;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.entity.SortingOption;
import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.global.common.entity.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;


    /**
     * 강의의 상세 별점 반환
     * @param lectureId 강의 _id
     * @return LectureDetailResponseDto
     */
    public LectureDetailResponseDto getLectureDetails(String lectureId) {
        Lecture lecture = lectureRepository.findLectureAndScheduleByLectureCode(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorCode.LECTURE_NOT_FOUND));
        return LectureDetailResponseDto.of(lecture);
    }

    /**
     * 여러 옵션을 선택하여 강의 리스트 반환
     * @param department Department (COMPUTER_SCI, INFO_COMM, EMBEDDED)
     * @param sortingOption 평점 많은 순, 좋아요 많은 순, 조회수 순 중 내림차순 선택
     * @param page 페이지 번호
     * @return LecturePageResponse
     */
    public LecturePageResponse sortListOfLectures(Department department, SortingOption sortingOption, int page) {
        Page<Lecture> lecturePage = null;
        Sort sort = getSort(sortingOption);
        PageRequest pageRequest = (sort != null) ? PageRequest.of(page, 20, sort) : PageRequest.of(page, 20);

        if (department != null) {
            validDepartment(department);
            lecturePage = lectureRepository.findAllByDepartment(pageRequest, department);
        } else lecturePage = lectureRepository.findAll(pageRequest);

        return getLecturePageResponse(lecturePage);
    }

    /**
     * 강의 후기를 작성할 강의 목록 검색
     * @param department Department (COMPUTER_SCI, INFO_COMM, EMBEDDED, OTHERS)
     * @param grade 학년 (1,2,3,4)
     * @param semester 수강 학기 (23-1, 23-2,,, 현재 학기)
     * @return List<LectureSearchListResponseDto> 검색 결과 리스트 반환
     */
    public List<LectureSearchListResponseDto> searchLecturesToReview(Department department, Integer grade, String semester) {
        List<Lecture> lectures = findLectures(department, grade, semester);
//        if (semester != null) return lectures.stream()
//                                    .map(lecture -> LectureSearchListResponseDto.of(lecture, semester))
//                                    .toList();

        return lectures.stream()
                    .map(LectureSearchListResponseDto::of)
                    .flatMap(List::stream)
                    .toList();
    }

    /**
     * 페이지로 반환된 LectureEntity -> Dto 변환
     */
    private LecturePageResponse getLecturePageResponse(Page<Lecture> lecturePage) {
        //todo Like의 여부
        return LecturePageResponse.of(lecturePage.stream().map(LecturePreviewResponseDto::of).toList(),
                            lecturePage.getTotalPages() - 1,
                            lecturePage.hasNext() ? lecturePage.getPageable().getPageNumber() + 1 : -1);
    }

    /**
     * 강의 검색 시 선택된 옵션에 따라 검색 진행
     */
    public List<Lecture> findLectures(Department department, Integer grade, String semester) {
        return List.of();
    }

    private Sort getSort(SortingOption sortingOption) {
        if (sortingOption == null) return null;
        Sort sort = null;
        switch (sortingOption) {
            case HIT -> sort = Sort.by("hits");
            case LIKE -> sort = Sort.by("likes");
            case RATING -> sort = Sort.by("starRating");
        }
        return sort;
    }

    private void validDepartment(Department department) {
        if (! (department.equals(Department.EMBEDDED) || department.equals(Department.COMPUTER_SCI) || department.equals(Department.INFO_COMM)))
            throw new LectureException(LectureErrorCode.DEPARTMENT_WRONG_INPUT);
    }
}
