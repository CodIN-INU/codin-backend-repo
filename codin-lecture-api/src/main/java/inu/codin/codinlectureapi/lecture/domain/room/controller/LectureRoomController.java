package inu.codin.codinlectureapi.lecture.domain.room.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.lecture.domain.room.service.LectureRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/rooms")
@RestController
@RequiredArgsConstructor
@Tag(name = "Lecture Room API", description = "강의실 현황 API")
public class LectureRoomController {

    private final LectureRoomService lectureRoomService;

    @Operation(
            summary = "오늘의 강의 현황",
            description = "당일의 요일에 따라 층마다 호실에서의 수업 내용 반환"
    )
    @GetMapping("/empty")
    public ResponseEntity<?> statusOfEmptyRoom(){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "오늘의 강의실 현황 반환", lectureRoomService.statusOfEmptyRoom()));
    }
}
