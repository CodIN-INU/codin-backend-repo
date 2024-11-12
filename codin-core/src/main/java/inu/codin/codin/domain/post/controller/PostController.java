package inu.codin.codin.domain.post.controller;

import inu.codin.codin.domain.post.dto.request.PostCreateReqDTO;
import inu.codin.codin.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "게시물 작성")
    @PostMapping
    public ResponseEntity<String> createPost(@RequestBody @Valid PostCreateReqDTO postCreateReqDTO) {
        postService.createPost(postCreateReqDTO);
        //게시물 작성 성공시 상태코드 201 Created , 게시물 작성성공 메세지 반환.
        return ResponseEntity.status(HttpStatus.CREATED).body("게시물 작성 성공");
    }

}
