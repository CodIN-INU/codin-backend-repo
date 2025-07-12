package inu.codin.codinticketingapi.domain.ticketing.controller;

import inu.codin.codinticketingapi.common.response.SingleResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.service.EventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticketing/event")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "티켓팅 이벤트 API")
public class EventController {

    private final EventService eventService;

    /** 티켓팅 이벤트 목록 조회 (송도캠, 미추홀캠) */
    @GetMapping
    public ResponseEntity<SingleResponse<?>> getEventList(
            @RequestParam @Valid Campus campus,
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 게시물 리스트 조회 성공",
                eventService.getEventList(campus, pageNumber)));
    }

    /** 티켓팅 이벤트 상세 정보 조회 */
    @GetMapping("/{eventId}")
    public ResponseEntity<SingleResponse<?>> getEventDetail(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 이벤트 상세 정보 조회 성공",
                eventService.getEventDetail(eventId)));
    }

    /** 유저 마이페이지 티켓팅 참여 전체 이력 조회 */
    @GetMapping("/user")
    public ResponseEntity<SingleResponse<?>> getUserEventList(
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "티켓팅 유저 티켓팅 참여 전체 이력 조회",
                eventService.getUserEventList(pageNumber)));
    }

    /** 유저 마이페이지 티켓팅 참여 완료, 취소 이력 조회 */
    @GetMapping("/user/canceled")
    public ResponseEntity<SingleResponse<?>> getUserEventListByCanceled(
            @RequestParam("page") @NotNull int pageNumber,
            @RequestParam("canceled") boolean canceled
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "유저 티켓팅 참여 (완료, 취소) 이력 조회",
                eventService.getUserEventListByCanceled(pageNumber, canceled)));
    }
}
