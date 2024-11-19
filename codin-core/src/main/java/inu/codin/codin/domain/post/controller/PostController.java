package inu.codin.codin.domain.post.controller;

import inu.codin.codin.domain.post.dto.request.PostContentUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostCreateReqDTO;
import inu.codin.codin.domain.post.dto.request.PostStatusUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
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
            @RequestPart("postContent") @Valid PostCreateReqDTO postCreateReqDTO,
            @RequestPart(value = "postImages", required = false) List<MultipartFile> postImages) {

        // postImages가 null이면 빈 리스트로 처리
        if (postImages == null) {
            postImages = List.of();
        }

        postService.createPost(postCreateReqDTO, postImages);
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

    @Operation(
            summary = "해당 사용자 게시물 전체 조회"
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDetailResponseDTO>> getAllPosts(@PathVariable String userId) {
        List<PostDetailResponseDTO> posts = postService.getAllPosts(userId);
        return ResponseEntity.status(HttpStatus.OK).body(posts);
    }

    @Operation(
            summary = "해당 게시물 상세 조회"
    )
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponseDTO> getPost(@PathVariable String postId) {
        PostDetailResponseDTO post = postService.getPost(postId);
        return ResponseEntity.status(HttpStatus.OK).body(post);
    }

    @Operation(
            summary = "해당 게시물 삭제"
    )
    @DeleteMapping("/postId")
    public ResponseEntity<String> deletePost(@PathVariable String postId) {
        postService.deletePost(postId);
        return ResponseEntity.status(HttpStatus.OK).body("게시물 삭제");
    }

    @Operation(
            summary = "해당 이미지 삭제"
    )
    @DeleteMapping("/{postId}/images")
    public ResponseEntity<String> deletePostImage(
            @PathVariable String postId,
            @RequestParam String imageUrls) {
        postService.deletePostImage(postId, imageUrls);
        return ResponseEntity.status(HttpStatus.OK).body("이미지 삭제");
    }




    //익명 수정 불가?





}

