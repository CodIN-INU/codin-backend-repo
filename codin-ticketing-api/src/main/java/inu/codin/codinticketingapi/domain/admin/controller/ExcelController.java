package inu.codin.codinticketingapi.domain.admin.controller;

import inu.codin.codinticketingapi.domain.admin.dto.response.ExcelResponse;
import inu.codin.codinticketingapi.domain.admin.service.ExcelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final ExcelService excelService;

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/{eventId}")
    public ResponseEntity<ByteArrayResource> downloadEventExcel(@PathVariable Long eventId) {
        ExcelResponse response = excelService.getExcel(eventId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFileName() + "\"")
                .body(new ByteArrayResource(response.getExcel()));
    }
}
