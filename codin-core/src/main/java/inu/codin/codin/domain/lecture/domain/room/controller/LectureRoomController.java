package inu.codin.codin.domain.lecture.domain.room.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.lecture.domain.room.service.LectureRoomService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/rooms")
@RestController
@RequiredArgsConstructor
public class LectureRoomController {

    private final LectureRoomService lectureRoomService;

    @Operation(
            summary = "오늘의 강의 현황",
            description = "당일의 요일에 따라 층마다 호실에서의 수업 내용 반환"
    )
    @GetMapping("/empty")
    public ResponseEntity<?> statusOfEmptyRoom(@RequestParam("floor") @Max(5) @Min(1) int floor){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, floor + "층의 강의실 현황 반환", lectureRoomService.statusOfEmptyRoom(floor)));
    }
}
