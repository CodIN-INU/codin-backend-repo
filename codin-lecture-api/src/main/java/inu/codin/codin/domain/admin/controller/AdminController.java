package inu.codin.codin.domain.admin.controller;

import inu.codin.codin.domain.admin.service.AdminService;
import inu.codin.codin.global.common.response.SingleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "Lecture Admin API", description = "긴급 대비용 Lecture Admin API")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "모든 교과목 데이터 ES 재인덱싱")
    @PostMapping("/es-reindexing")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<SingleResponse<?>> esReindexing() {
        adminService.reindexAllLectures();
        return ResponseEntity.ok().body(new SingleResponse<>(200, "[비동기] 모든 교과목 데이터를 ElasticSearch에 재인덱싱합니다.", null));
    }

    @Operation(summary = "모든 교과목 데이터 AI 요약본 재생성")
    @PostMapping("/ai-re-summarize")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<SingleResponse<?>> reAiSummarize() {
        adminService.reSummarizeAllLectures();
        return ResponseEntity.ok().body(new SingleResponse<>(200, "[비동기] 모든 교과목 데이터의 AI 요약본을 재생성합니다.", null));
    }
}
