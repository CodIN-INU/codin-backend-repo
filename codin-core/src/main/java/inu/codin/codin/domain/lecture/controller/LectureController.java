package inu.codin.codin.domain.lecture.controller;

import inu.codin.codin.common.Department;
import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.lecture.dto.Option;
import inu.codin.codin.domain.lecture.service.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lectures")
@RequiredArgsConstructor
@Tag(name = "Lecture API", description = "강의실 정보 API")
public class LectureController {

    private final LectureService lectureService;

    @Operation(
            summary = "학과명 및 과목/교수 정렬 페이지",
            description = "학과명과 검색 키워드(optional), 과목/교수 라디오 토클을 통해 정렬한 리스트 반환<br>"+
                    "department : COMPUTER_SCI, INFO_COMM, EMBEDDED, OTHER(공통) <br>"+
                    "keyword : 검색 키워드 (Optional)"+
                    "option : LEC(과목명) , PROF(교수명)"
    )
    @GetMapping("/list")
    public ResponseEntity<SingleResponse<?>> sortListOfLectures(@RequestParam("department") Department department,
                                                           @RequestParam(value = "keyword", required = false) String keyword,
                                                           @RequestParam("option") Option option,
                                                              @RequestParam("page") int page){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200,
                        department.getDescription()+" 강의들 "+option.getDescription()+"순으로 정렬 반환",
                        lectureService.sortListOfLectures(department, keyword, option, page)));
    }

    @Operation(
            summary = "강의 별점 정보 반환",
            description = "강의후기 > 상세보기 눌렀을 때 뜨는 강의 정보 반환"
    )
    @GetMapping("/{lectureId}")
    public ResponseEntity<SingleResponse<?>> getLectureDetails(@PathVariable("lectureId") String lectureId){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "강의 별점 정보 반환", lectureService.getLectureDetails(lectureId)));
    }

    @Operation(
            summary = "학과, 학년, 수강학기 로 강의 검색",
            description = "수강 후기 작성 시 필요한 검색엔진<br>" +
                    "학과, 학년, 수강학기 중 하나만으로도 검색 가능"
    )
    @GetMapping("/search-review")
    public ResponseEntity<ListResponse<?>> searchLecturesToReview(@RequestParam(required = false) Department department,
                                                                    @RequestParam(required = false) @Min(1) @Max(4) Integer grade,
                                                                    @RequestParam(required = false) String semester){
        return ResponseEntity.ok()
                .body(new ListResponse<>(200, "필터링 된 강의들 반환 완료",
                        lectureService.searchLecturesToReview(department, grade, semester)));
    }

}
