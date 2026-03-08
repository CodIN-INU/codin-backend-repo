package inu.codin.codin.domain.calendar.controller;

import inu.codin.common.entity.Department;
import inu.codin.common.response.SingleResponse;
import inu.codin.codin.domain.calendar.controller.swagger.CalendarController;
import inu.codin.codin.domain.calendar.dto.CalendarCreateRequest;
import inu.codin.codin.domain.calendar.dto.CalendarCreateResponse;
import inu.codin.codin.domain.calendar.dto.CalendarMonthResponse;
import inu.codin.codin.domain.calendar.service.CalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarControllerImpl implements CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/month")
    public ResponseEntity<SingleResponse<CalendarMonthResponse>> getMonth(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Department department
    ) {


        return ResponseEntity.ok().body(
                new SingleResponse<>(200, "캘린더 반환 완료",
                        calendarService.getMonth(year, month, department))
        );
    }

    @PostMapping("/events")
    //@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<SingleResponse<CalendarCreateResponse>> create(@Valid @RequestBody CalendarCreateRequest request) {
        return ResponseEntity.status(201).body(new SingleResponse<>(201, "켈린더 이벤트 생성 완료",
                calendarService.create(request)));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<SingleResponse<?>> delete(@PathVariable String eventId) {
        calendarService.delete(eventId);
        return ResponseEntity.accepted().body(new SingleResponse<>(202, "캘린더 이벤트 삭제 완료", null));
    }
}
