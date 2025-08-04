package inu.codin.codin.domain.lecture.controller;

import inu.codin.codin.domain.lecture.dto.LectureDetailResponseDto;
import inu.codin.codin.domain.lecture.dto.LecturePageResponse;
import inu.codin.codin.domain.lecture.dto.LectureSearchListResponseDto;
import inu.codin.codin.domain.lecture.entity.SortingOption;
import inu.codin.codin.domain.lecture.service.LectureService;
import inu.codin.codin.global.common.entity.Department;
import inu.codin.codin.global.common.response.ListResponse;
import inu.codin.codin.global.common.response.SingleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Lecture API", description = "강의실 정보 API")
public class LectureController {

    private final LectureService lectureService;

    @Operation(
            summary = "학과명 및 과목/교수 정렬 페이지",
            description = "학과명과 검색 키워드(optional), 과목/교수 라디오 토클을 통해 정렬한 리스트 10개씩 반환<br>"+
                    "department : COMPUTER_SCI, INFO_COMM, EMBEDDED <br>"+
                    "keyword : 검색 키워드 (Optional)"+
                    "sort : RATING(평점 높은 순) , LIKE(좋아요 많은 순), HIT(조회수 많은 순) <br>"+
                    "like : 좋아요한 과목 모아보기 true"
    )
    @GetMapping("/courses")
    public ResponseEntity<SingleResponse<LecturePageResponse>> sortListOfLectures(
            @RequestParam(value = "department", required = false) Department department,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", required = false) SortingOption sort,
            @RequestParam(value = "like", required = false) Boolean like,
            @RequestParam("page") int page)
    {
        return ResponseEntity.ok().body(new SingleResponse<>(200, "과목 리스트 반환 완료",
                        lectureService.sortListOfLectures(keyword, department, sort, like, page)));
    }

    @Operation(
            summary = "과목 상세 정보 반환",
            description = "Preview를 눌렀을 때 뜨는 과목 정보 반환"
    )
    @GetMapping("/{lectureId}")
    public ResponseEntity<SingleResponse<LectureDetailResponseDto>> getLectureDetails(@PathVariable("lectureId") Long lectureId) {
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "강의 별점 정보 반환", lectureService.getLectureDetails(lectureId)));
    }

    @Operation(
            summary = "학과, 학년, 수강학기 로 강의 검색",
            description = "수강 후기 작성 시 필요한 검색엔진<br>" +
                    "학과(COMPUTER_SCI, INFO_COMM, EMBEDDED) <br>" +
                    "학년(1,2,3,4) <br>" +
                    "수강학기(25-1 ~ )중 하나만으로도 검색 가능"
    )
    @GetMapping("/search-review")
    public ResponseEntity<ListResponse<LectureSearchListResponseDto>> searchLecturesToReview(
            @RequestParam(required = false) Department department,
            @RequestParam(required = false) @Min(1) @Max(4) Integer grade,
            @RequestParam(required = false) String semester)
    {
        return ResponseEntity.ok().body(new ListResponse<>(200, "필터링 된 강의들 반환 완료",
                        lectureService.searchLecturesToReview(department, grade, semester)));
    }

}
