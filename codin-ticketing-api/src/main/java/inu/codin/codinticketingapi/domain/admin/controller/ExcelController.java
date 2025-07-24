package inu.codin.codinticketingapi.domain.admin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticketing/excel")
@RequiredArgsConstructor
@Tag(name = "Event 엑셀 API", description = "티켓팅 이벤트 엑셀 정보 API")
public class ExcelController {

    // todo: 1. EventId를 통해서 이벤트에 참여한 TicketingInfo, TicketingProfile 정보를 엑셀 파일로 다운로드
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/{eventId}")
    public void downloadEventExcel(@PathVariable Long eventId) {
        // 실제 구현 필요
    }
}
