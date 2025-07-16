package inu.codin.codin.domain.report.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.post.domain.comment.service.CommentService;
import inu.codin.codin.domain.report.dto.request.ReportCreateRequestDto;
import inu.codin.codin.domain.report.dto.request.ReportExecuteRequestDto;
import inu.codin.codin.domain.report.dto.response.ReportPageResponse;
import inu.codin.codin.domain.report.dto.response.ReportSummaryResponseDTO;
import inu.codin.codin.domain.report.dto.response.ReportedCommentDetailResponseDTO;
import inu.codin.codin.domain.report.dto.response.ReportedPostDetailResponseDTO;
import inu.codin.codin.domain.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
@Tag(name = "Reoprt API", description = "사용자 신고 기능")
public class ReportController {
    private final ReportService reportService;
    private final CommentService commentService;

    //(User)신고 작성
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


    //(Admin) 특정 대상 신고 정보 조회 API
    @Operation(summary = "특정 게시글,댓글 신고 내역 상세조회(관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/summary/{reportedEntityId}")
    public ResponseEntity<?> getReportSummary(@PathVariable String reportedEntityId) {
        ReportSummaryResponseDTO reportSummary = reportService.getReportSummary(reportedEntityId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "특정 대상 신고 내역 조회 완료", reportSummary));
    }


    // 신고 처리 (관리자)
    @Operation(summary = "신고 처리 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{reportTargetId}")
    public ResponseEntity<?> handleReport(
            @RequestBody ReportExecuteRequestDto reportExecuteRequestDto) {

        reportService.handleReport(reportExecuteRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(200, "(관리자) 신고 처리 - 정지하기",null));
    }

    // 신고 처리 (관리자)
    @Operation(summary = "신고 처리-유지하기 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/resolveReport/{reportTargetId}")
    public ResponseEntity<?> resolveReport(
            @PathVariable String reportTargetId
    ) {

        reportService.resolveReport(reportTargetId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(200, "(관리자) 신고 처리 - 유지하기",null));
    }



    @Operation(summary = "모든 신고 조회하기 - 오름차순 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> getAllReportedPosts(@RequestParam("page") @NotNull int pageNumber){
        ReportPageResponse postpages = reportService.getAllReportedPosts(pageNumber);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "(관리자) 신고 전체 조회",postpages));
    }


    @Operation(summary = "게시글 신고 상세조회  (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/post/{postId}/{reportedEntityId}")
    public ResponseEntity<?> getReportedPost (@PathVariable String postId,
                                              @PathVariable String reportedEntityId) {
        ReportedPostDetailResponseDTO responseDTO = reportService.getReportedPostWithDetail(postId, reportedEntityId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "(관리자) 신고 게시글 상세 조회",responseDTO));
    }

    @Operation(summary = "댓글,대댓글 신고 상세조회 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/comments/{postId}/{reportedEntityId}")
    public ResponseEntity<?> getReportedComments(@PathVariable String postId,
                                                 @PathVariable String reportedEntityId){
        List<ReportedCommentDetailResponseDTO> responseDTOS =  reportService.getReportedCommentsByPostId(postId, reportedEntityId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "(관리자) 신고 댓글/대댓글 상세 조회",responseDTOS));
    }



}
