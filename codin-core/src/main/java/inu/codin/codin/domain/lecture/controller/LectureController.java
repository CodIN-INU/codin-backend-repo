package inu.codin.codin.domain.lecture.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.lecture.service.LectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lectures")
@RequiredArgsConstructor
@Tag(name = "Lecture API", description = "강의실 정보 API")
public class LectureController {

    private final LectureService lectureService;

    @Operation(summary = "강의실 정보 반환")
    @GetMapping("/{lectureId}")
    public ResponseEntity<?> getLectureDetails(@PathVariable("lectureId") String lectureId){
        lectureService.getLectureDetails(lectureId);
        return null;
    }

    @Operation(
            summary = "오늘의 강의 현황",
            description = "당일의 요일에 따라 층마다 호실에서의 수업 내용 반환"
    )
    @GetMapping("/rooms/empty")
    public ResponseEntity<?> statusOfEmptyRoom(@RequestParam("floor") int floor){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, floor + "층의 강의실 현황 반환", lectureService.statusOfEmptyRoom(floor)));
    }


}
