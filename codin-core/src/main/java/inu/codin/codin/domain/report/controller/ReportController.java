package inu.codin.codin.domain.report.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.report.dto.request.ReportCreateRequestDto;
import inu.codin.codin.domain.report.dto.request.ReportExecuteRequestDto;
import inu.codin.codin.domain.report.dto.response.ReportResponseDto;
import inu.codin.codin.domain.report.entity.ReportEntity;
import inu.codin.codin.domain.report.entity.ReportStatus;
import inu.codin.codin.domain.report.entity.ReportTargetType;
import inu.codin.codin.domain.report.entity.ReportType;
import inu.codin.codin.domain.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    //신고 작성
    /**
      User -> User , Post, Comment, Reply
     **/
    @Operation(summary = "신고 하기 - 게시물, 댓글, 대댓글")
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody @Valid ReportCreateRequestDto reportCreateRequestDto) {
        reportService.createReport(reportCreateRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "신고 생성 완료", null));
    }

    //신고 목록 조회 (관리자)
    /***
     전체조회
     ReportType 별 조회
     + 특정 신고횟수 이상 조회
     */

    //특정 유저 신고 상세 조회 (관리자)

    //신고 처리 (관리자)

    // 특정 신고 타입 목록 조회 (관리자)
    @Operation(summary = "특정 신고 타입 목록 조회 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getReports(
            @RequestParam(required = false) ReportTargetType reportTargetType,
            @RequestParam(required = false) Integer minReportCount) {

        List<ReportResponseDto> reports = reportService.getAllReports(reportTargetType, minReportCount);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "특정 신고 타입 목록 조회", reports));
    }


    // 특정 유저 신고 내역 조회 (관리자)
    @Operation(summary = "특정 유저 신고 내역 조회 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user")
    public ResponseEntity<?> getReportsByUserId(
            @RequestParam("userId") @NotNull String userId) {

        List<ReportResponseDto> userReports = reportService.getReportsByUserId(userId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "특정 유저가 신고한 내역 조회", userReports));
    }


    // 신고 처리 (관리자)
    @Operation(summary = "신고 처리 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{reportId}")
    public ResponseEntity<?> handleReport(
            @RequestBody ReportExecuteRequestDto reportExecuteRequestDto) {

        reportService.executeReport(reportExecuteRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(200, "(관리자) 신고 처리",null));
    }

}
