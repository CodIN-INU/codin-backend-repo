package inu.codin.codin.domain.board.notice.controller;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.board.notice.dto.request.NoticeCreateUpdateRequestDTO;
import inu.codin.codin.domain.board.notice.dto.response.NoticeDetailResponseDto;
import inu.codin.codin.domain.board.notice.dto.response.NoticePageResponse;
import inu.codin.codin.domain.board.notice.service.NoticeService;
import inu.codin.codin.domain.post.service.PostCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/notice")
@Tag(name = "Notice API", description = "[리디자인] 게시판 공지사항 API")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final PostCommandService postCommandService;

    /*
    ===================
    ROLE_USER 사용한 API (조회 기능)
    ===================
     */

    /*
     * 학과별 공지사항 조회,
     * Post 기능과 동일하지만 EXTRACURRICULAR_INNER, DEPARTMENT_NOTICE 카테고리만 조회
     */
    @Operation(
            summary = "학과별 공지사항 조회",
            description = "공지사항의 postCategory 중 EXTRACURRICULAR_INNER는 크롤링된 학과 홈페이지 공지사항 <br>" +
                    "DEPARTMENT_NOTICE는 직접 수기로 작성한 공지사항 <br>"
    )
    @GetMapping("/category")
    public ResponseEntity<SingleResponse<NoticePageResponse>> getAllNotices(@RequestParam("department") @NotNull Department department,
                                                                            @RequestParam("page") @NotNull int pageNumber) {
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "학과별 공지사항 조회 성공", noticeService.getAllNotices(department, pageNumber)));
    }


    @Operation(
            summary = "해당 공지사항 상세 조회"
    )
    @GetMapping("/{postId}")
    public ResponseEntity<SingleResponse<NoticeDetailResponseDto>> getNoticesWithDetail(@PathVariable String postId) {
        NoticeDetailResponseDto notice = noticeService.getNoticesWithDetail(postId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "공지사항 상세 조회 성공", notice));
    }
    

    /*
    ===================
    ROLE_MANAGER, ROLE_ADMIN 사용한 API (작성, 수정, 삭제 기능)
    ===================
     */

    @Operation(
            summary = "공지사항 작성",
            description = "작성하는 글쓴이의 Department에 맞춰 title에 prefix가 자동으로 삽입 <br>" +
                    "COMPUTER_SCI -> [컴공] <br>" +
                    "COMPUTER_SCI_NIGHT -> [컴공] <br>" +
                    "IT_COLLEGE -> [정보대] <br>" +
                    "INFO_COMM -> [정통] <br>" +
                    "EMBEDDED -> [임베] <br>" +
                    "공지사항 작성 시 이미지 첨부 가능, 이미지가 없으면 빈 리스트로 처리"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>> createNotice(
            @RequestPart("noticeContent") @Valid NoticeCreateUpdateRequestDTO noticeCreateUpdateRequestDTO,
            @RequestPart(value = "noticeImages", required = false) List<MultipartFile> noticeImages) {

        // postImages가 null이면 빈 리스트로 처리
        if (noticeImages == null || noticeImages.isEmpty()) noticeImages = List.of();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "공지사항이 작성되었습니다.", noticeService.createNotice(noticeCreateUpdateRequestDTO, noticeImages)));
    }

    @Operation(
            summary = "공지사항 내용 수정 및 이미지 수정&추가",
            description = "공지사항의 내용 수정, 이미지 추가 가능. <br>" +
                    "새로 추가된 이미지만 넣어서 API 요청 필수 (없으면 빈 리스트), 삭제의 경우 별도의 API로 처리"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PutMapping(value = "/{postId}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>>  updatePostContent(
            @PathVariable String postId,
            @RequestPart("noticeContent") @Valid NoticeCreateUpdateRequestDTO noticeCreateUpdateRequestDTO,
            @RequestPart(value = "noticeImages", required = false) List<MultipartFile> noticeImages) {

        if (noticeImages == null || noticeImages.isEmpty()) noticeImages = List.of();
        noticeService.updateNoticeContent(postId, noticeCreateUpdateRequestDTO, noticeImages);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SingleResponse<>(200, "게시물 내용이 수정되었습니다.", null));
    }

    @Operation(
            summary = "공지사항 이미지 삭제"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("/{postId}/images")
    public ResponseEntity<SingleResponse<?>> deleteNoticeImage(
            @PathVariable String postId,
            @RequestParam String imageUrl) {

        postCommandService.deletePostImage(postId, imageUrl);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "공지사항 이미지가 삭제되었습니다.", null));
    }

    @Operation(
            summary = "공지사항 삭제 (Soft Delete)"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("/{postId}")
    public ResponseEntity<SingleResponse<?>> softDeleteNotice(@PathVariable String postId) {
        postCommandService.softDeletePost(postId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "공지사항이 삭제되었습니다.", null));
    }
}
