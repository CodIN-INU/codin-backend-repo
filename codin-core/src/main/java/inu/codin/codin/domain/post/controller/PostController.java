package inu.codin.codin.domain.post.controller;

import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.post.dto.request.PostAnonymousUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostContentUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostCreateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostStatusUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.dto.response.ReportedPostDetailResponseDTO;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
@Validated
@Tag(name = "POST API", description = "게시글 API")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(
            summary = "게시물 작성",
            description = "JSON 형식의 게시물 데이터(postContent)와 이미지 파일(postImages) 업로드"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>>  createPost(
            @RequestPart("postContent") @Valid PostCreateRequestDTO postCreateRequestDTO,
            @RequestPart(value = "postImages", required = false) List<MultipartFile> postImages) {

        // postImages가 null이면 빈 리스트로 처리
        if (postImages == null) postImages = List.of();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "게시물이 작성되었습니다.", postService.createPost(postCreateRequestDTO, postImages)));
    }

    @Operation(
            summary = "게시물 내용 수정 및 이미지 수정&추가"
    )
    @PatchMapping(value = "/{postId}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>>  updatePostContent(
            @PathVariable String postId,
            @RequestPart("postContent") @Valid PostContentUpdateRequestDTO requestDTO,
            @RequestPart(value = "postImages", required = false) List<MultipartFile> postImages) {

        postService.updatePostContent(postId, requestDTO, postImages);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SingleResponse<>(200, "게시물 내용이 수정되었습니다.", null));
    }

    @Operation(
            summary = "상태 수정"
    )
    @PatchMapping("/{postId}/status")
    public ResponseEntity<SingleResponse<?>> updatePostStatus(
            @PathVariable String postId,
            @RequestBody PostStatusUpdateRequestDTO requestDTO) {
        postService.updatePostStatus(postId, requestDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SingleResponse<>(200, "게시물 상태가 수정되었습니다.", null));
    }


    @Operation(summary = "게시물 익명 설정 수정")
    @PatchMapping("/{postId}/anonymous")
    public ResponseEntity<SingleResponse<?>> updatePostAnonymous(
            @PathVariable String postId,
            @RequestBody @Valid PostAnonymousUpdateRequestDTO requestDTO) {
        postService.updatePostAnonymous(postId, requestDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SingleResponse<>(200, "게시물 익명 설정이 수정되었습니다.", null));
    }


    @Operation(
            summary = "카테고리별 삭제 되지 않은 모든 게시물 조회"
    )
    @GetMapping("/category")
    public ResponseEntity<SingleResponse<PostPageResponse>> getAllPosts(@RequestParam PostCategory postCategory,
                                                                        @RequestParam("page") @NotNull int pageNumber) {
        PostPageResponse postpages= postService.getAllPosts(postCategory, pageNumber);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "카테고리별 삭제 되지 않은 모든 게시물 조회 성공", postpages));
    }


    @Operation(summary = "해당 게시물 상세 조회 (댓글 조회는 Comment에서 따로 조회)")
    @GetMapping("/{postId}")
    public ResponseEntity<SingleResponse<PostDetailResponseDTO>> getPostWithDetail(@PathVariable String postId) {
        PostDetailResponseDTO post = postService.getPostWithDetail(postId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "게시물 상세 조회 성공", post));
    }


    @Operation(summary = "게시물 이미지 삭제")
    @DeleteMapping("/{postId}/images")
    public ResponseEntity<SingleResponse<?>> deletePostImage(
            @PathVariable String postId,
            @RequestParam String imageUrl) {

        postService.deletePostImage(postId, imageUrl);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "게시물 이미지가 삭제되었습니다.", null));
    }

    @Operation(summary = "게시물 삭제 (Soft Delete)")
    @DeleteMapping("/{postId}")
    public ResponseEntity<SingleResponse<?>> softDeletePost(@PathVariable String postId) {
        postService.softDeletePost(postId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "게시물이 삭제되었습니다.", null));
    }

    @Operation(
            summary = "검색 엔진"
    )
    @GetMapping("/search")
    public ResponseEntity<SingleResponse<?>> searchPosts(@RequestParam("keyword") @Size(min = 2) String keyword,
                                                         @RequestParam("pageNumber") @NotNull int pageNumber){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "'"+keyword+"'"+"으로 검색된 게시글 반환 완료", postService.searchPosts(keyword, pageNumber)));
    }

    @Operation(summary = "Top 3 베스트 게시글 가져오기")
    @GetMapping("/top3")
    public ResponseEntity<ListResponse<?>> getTop3BestPosts(){
        return ResponseEntity.ok()
                .body(new ListResponse<>(200, "Top3 베스트 게시글 반환 완료", postService.getTop3BestPosts()));
    }

    @Operation(summary = "Top3로 선정된 게시글들 모두 가져오기")
    @GetMapping("/best")
    public ResponseEntity<SingleResponse<?>> getBestPosts(@RequestParam("pageNumber") int pageNumber){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "Top3로 선정된 게시글들 모두 반환 완료", postService.getBestPosts(pageNumber)));
    }
}