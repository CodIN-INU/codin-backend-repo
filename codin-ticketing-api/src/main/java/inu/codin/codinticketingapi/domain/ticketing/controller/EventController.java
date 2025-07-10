package inu.codin.codinticketingapi.domain.ticketing.controller;

import inu.codin.codinticketingapi.common.response.SingleResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Campus;
import inu.codin.codinticketingapi.domain.ticketing.service.EventReadService;
import inu.codin.codinticketingapi.domain.ticketing.service.EventWriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticketing/events")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "티켓팅 이벤트 API")
public class EventController {

    private final EventReadService eventReadService;
    private final EventWriteService eventWriteService;

    // 티켓팅 이벤트 목록 조회 (송도캠, 미추홀캠)
    @GetMapping
    public ResponseEntity<SingleResponse<?>> getEventList(
            @RequestParam @Valid Campus campus,
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "이벤트 게시물 리스트 반환 성공",
                eventReadService.getEventList(campus, pageNumber)));
    }

    // 티켓팅 이벤트 상세 사항
    @GetMapping("/{eventId}")
    public ResponseEntity<SingleResponse<?>> getEventDetail(
            @PathVariable String eventId
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "이벤트 상세정보 반환 성공",
                eventReadService.getEventDetail(eventId)));
    }

    // todo: 마이페이지 티켓팅 참여 전체 이력
    // todo: 마이페이지 티켓팅 참여 완료 이력
    // todo: 마이페이지 티켓팅 참여 취소 이력

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/management")
    public ResponseEntity<SingleResponse<?>> getEventListByManager(
            @RequestParam("page") @NotNull int pageNumber
    ) {
        return ResponseEntity.ok(new SingleResponse<>(200, "이벤트 게시물 리스트 반환 성공",
                eventReadService.getEventListByManager(pageNumber)));
    }

    // todo: 티켓팅 이벤트 비밀번호 탐색
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/{eventId}/password")
    public ResponseEntity<SingleResponse<?>> getEventPassword(
            @PathVariable String eventId
    ) {
        // 실제 구현 필요
        String password = "dummyPassword";
        return ResponseEntity.ok(new SingleResponse<>(200, "이벤트 비밀번호 반환 성공", password));
    }

    // todo: 티켓팅 이벤트 마감 - 마감시 더이상 유저(트래픽)를 받지 않음
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/{eventId}/close")
    public ResponseEntity<SingleResponse<?>> closeEvent(
            @PathVariable String eventId
    ) {
        // eventWriteService.closeEvent(eventId);
        return ResponseEntity.ok(new SingleResponse<>(200, "이벤트 마감 성공", null));
    }

    // todo: 티켓팅 이벤트 삭제
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<SingleResponse<?>> deleteEvent(
            @PathVariable String eventId
    ) {
        //eventWriteService.deleteEvent(eventId);
        return ResponseEntity.ok(new SingleResponse<>(200, "이벤트 삭제 성공", null));
    }

    // todo: 티켓팅 이벤트 수정
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PutMapping("/{eventId}")
    public ResponseEntity<SingleResponse<?>> updateEvent(
            @PathVariable String eventId
            /*, @RequestBody EventUpdateRequestDto dto */
    ) {
        // eventWriteService.updateEvent(eventId, dto);
        return ResponseEntity.ok(new SingleResponse<>(200, "이벤트 수정 성공", null));
    }
}
