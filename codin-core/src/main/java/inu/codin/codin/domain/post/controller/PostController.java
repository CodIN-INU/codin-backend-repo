package inu.codin.codin.domain.post.controller;

import inu.codin.codin.domain.post.dto.request.PostAnonymousUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostContentUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostCreateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostStatusUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.response.PostWithCountsResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostWithDetailResponseDTO;
import inu.codin.codin.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
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
    public ResponseEntity<String> createPost(
            @RequestPart("postContent") @Valid PostCreateRequestDTO postCreateRequestDTO,
            @RequestPart(value = "postImages", required = false) List<MultipartFile> postImages) {

        // postImages가 null이면 빈 리스트로 처리
        if (postImages == null) {
            postImages = List.of();
        }

        postService.createPost(postCreateRequestDTO, postImages);
        return ResponseEntity.status(HttpStatus.CREATED).body("게시물 작성 성공");
    }

    @Operation(
            summary = "게시물 내용 이미지 수정 추가"
    )
    @PatchMapping(value = "/{postId}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updatePostContent(
            @PathVariable String postId,
            @RequestPart("postContent") @Valid PostContentUpdateRequestDTO requestDTO,
            @RequestPart(value = "postImages", required = false) List<MultipartFile> postImages) {

        postService.updatePostContent(postId, requestDTO, postImages);
        return ResponseEntity.status(HttpStatus.OK).body("게시물 수정 성공");
    }

    @Operation(
            summary = "상태 수정"
    )
    @PatchMapping("/{postId}/status")
    public ResponseEntity<String> updatePostStatus(
            @PathVariable String postId,
            @RequestBody PostStatusUpdateRequestDTO requestDTO) {
        postService.updatePostStatus(postId, requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body("게시물 상태 수정 성공");
    }


    @Operation(summary = "게시물 익명 설정 수정")
    @PatchMapping("/{postId}/anonymous")
    public ResponseEntity<String> updatePostAnonymous(
            @PathVariable String postId,
            @RequestBody @Valid PostAnonymousUpdateRequestDTO requestDTO) {
        postService.updatePostAnonymous(postId, requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body("게시물 익명 설정이 수정되었습니다.");
    }


    @Operation(
            summary = "삭제되지 않은 모든 게시물 조회"
    )
    @GetMapping("")
    public ResponseEntity<List<PostWithCountsResponseDTO>> getAllPosts() {
        List<PostWithCountsResponseDTO> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @Operation(
            summary = "해당 사용자 게시물 전체 조회"
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostWithCountsResponseDTO>> getAllUserPosts(@PathVariable String userId) {
        List<PostWithCountsResponseDTO> posts = postService.getAllUserPosts(userId);
        return ResponseEntity.ok(posts);
    }

    @Operation(
            summary = "해당 게시물 상세 조회"
    )
    @GetMapping("/{postId}")
    public ResponseEntity<PostWithDetailResponseDTO> getPostWithDetail(@PathVariable String postId) {
        PostWithDetailResponseDTO post = postService.getPostWithDetail(postId);
        return ResponseEntity.ok(post);
    }


    @Operation(summary = "게시물 이미지 삭제")
    @DeleteMapping("/{postId}/images")
    public ResponseEntity<String> deletePostImage(
            @PathVariable String postId,
            @RequestParam String imageUrl) {
        postService.deletePostImage(postId, imageUrl);
        return ResponseEntity.status(HttpStatus.OK).body("이미지가 삭제되었습니다.");
    }

    @Operation(summary = "게시물 삭제 (Soft Delete)")
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> softDeletePost(@PathVariable String postId) {
        postService.softDeletePost(postId);
        return ResponseEntity.status(HttpStatus.OK).body("게시물이 삭제되었습니다.");
    }




}

