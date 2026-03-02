package inu.codin.lecture.domain.lecture.controller;

import inu.codin.lecture.domain.lecture.dto.LectureRoomResponseDto;
import inu.codin.lecture.domain.lecture.service.LectureRoomService;
import inu.codin.common.response.SingleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/rooms")
@RestController
@RequiredArgsConstructor
@Tag(name = "Lecture Room API", description = "강의실 현황 API")
public class LectureRoomController {

    private final LectureRoomService lectureRoomService;

    @Operation(
            summary = "오늘의 강의 현황",
            description = "당일의 요일에 따라 층마다 호실에서의 수업 내용 반환 <br><br>" +
                    "유저의 단과대에 해당하는 강의실의 수업 내용만 반환 [유저의 단과대가 없을 경우 빈 리스트 반환]"
    )
    @GetMapping("/empty")
    public ResponseEntity<SingleResponse<List<Map<Integer, List<LectureRoomResponseDto>>>>> statusOfEmptyRoom(){
        return ResponseEntity.ok().body(new SingleResponse<>(200, "오늘의 강의실 현황 반환",
                lectureRoomService.statusOfEmptyRoom()));
    }

    @Operation(
            summary = "특정 건물 및 층의 오늘의 강의 현황",
            description = "당일의 요일에 따라 특정 건물과 층에 해당하는 수업 내용 반환 <br><br>" +
                    "전체 층은 0으로 입력 <br><br>" +
                    "건물은 호관 번호로 입력"
    )
    @GetMapping("/empty/detail")
    public ResponseEntity<SingleResponse<List<Map<Integer, List<LectureRoomResponseDto>>>>> statusOfDetailEmptyRoom(
            @RequestParam(value = "building") int building,
            @RequestParam(value = "floor") int floor
    ){
        return ResponseEntity.ok().body(new SingleResponse<>(200, "특정 건물 및 층의 오늘의 강의 현황",
                lectureRoomService.statusOfDetailEmptyRoom(building, floor)));
    }
}
