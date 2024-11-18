package inu.codin.codin.domain.post.controller;

import inu.codin.codin.domain.post.dto.request.PostCreateReqDTO;
import inu.codin.codin.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
}

