package inu.codin.codin.domain.report.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.post.domain.comment.dto.response.ReportedCommentDetailResponseDTO;
import inu.codin.codin.domain.post.domain.comment.service.CommentService;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.dto.response.ReportedPostDetailResponseDTO;
import inu.codin.codin.domain.report.dto.request.ReportCreateRequestDto;
import inu.codin.codin.domain.report.dto.request.ReportExecuteRequestDto;
import inu.codin.codin.domain.report.dto.response.ReportCountResponseDto;
import inu.codin.codin.domain.report.dto.response.ReportResponseDto;
import inu.codin.codin.domain.report.dto.response.ReportSummaryResponseDTO;
import inu.codin.codin.domain.report.entity.ReportTargetType;
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

//    @Operation(
//            summary = "삭제되지 않은 신고된 모든 게시물 조회"
//    )
//    @GetMapping("/post")
//    public ResponseEntity<SingleResponse<PostPageResponse>> getAllPosts(@RequestParam("page") @NotNull int pageNumber) {
//        PostPageResponse postpages= reportService.getReportedPosts(pageNumber);
//        return ResponseEntity.ok()
//                .body(new SingleResponse<>(200, "삭제되지 않은 신고된 모든 게시물 조회 성공", postpages));
//    }

    //(Admin) 특정 대상 신고 정보 조회 API
    @Operation(summary = "특정 게시물의 신고 내역 조회(관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/summary/{reportedEntityId}")
    public ResponseEntity<?> getReportSummary(@PathVariable String reportedEntityId) {
        ReportSummaryResponseDTO reportSummary = reportService.getReportSummary(reportedEntityId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "특정 대상 신고 내역 조회 완료", reportSummary));
    }


    // 특정 신고 타입 목록 조회 (관리자)
    @Operation(summary = "Pending(신고 처리 대기) 신고 내역 오름차순 정렬 조회 (관리자)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getReports(){

        List<ReportCountResponseDto> reports = reportService.getAllReports();
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
        PostPageResponse postpages = reportService.getAllReportedPosts(pageNumber);
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
