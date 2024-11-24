package inu.codin.codin.domain.post.controller;

import inu.codin.codin.domain.post.dto.request.CommentCreateRequsetDTO;
import inu.codin.codin.domain.post.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.dto.response.CommentsResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostWithCommentsResponseDTO;
import inu.codin.codin.domain.post.service.CommentService;
import inu.codin.codin.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;
    private final PostService postService;

    public CommentController(CommentService commentService, PostService postService) {
        this.commentService = commentService;
        this.postService = postService;
    }


    @Operation(
            summary = "댓글 추가"
    )
    @PostMapping("/{postId}")
    public ResponseEntity<String> addComment(@PathVariable String postId, @RequestBody @Valid CommentCreateRequsetDTO requestDTO) {
        commentService.addComment(postId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("댓글이 추가되었습니다.");
    }

    @Operation(
            summary = "대댓글 추가"
    )
    @PostMapping("/{postId}/{parentCommentId}")
    public ResponseEntity<String> addReply(@PathVariable String postId, @PathVariable String parentCommentId, @RequestBody @Valid ReplyCreateRequestDTO requestDTO) {
        commentService.addReply(postId, parentCommentId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("대댓글이 추가되었습니다.");
    }

    @Operation(
            summary = "해당 유저가 남긴 댓글 및 대댓글 조회"
    )
    @GetMapping("/{userId}")
    public ResponseEntity<List<CommentsResponseDTO>> getPostWithComments(@PathVariable String userId) {
        List<CommentsResponseDTO>  response = commentService.getCommentsByUser(userId);
        return ResponseEntity.ok(response);
    }
    @Operation(
            summary = "해당 사용자 게시물 전체 조회"
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostWithCommentsResponseDTO>> getAllPosts(@PathVariable String userId) {
        List<PostWithCommentsResponseDTO> posts = postService.getAllUserPostsAndComments(userId);
        return ResponseEntity.status(HttpStatus.OK).body(posts);
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{postId}/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable String postId, @PathVariable String commentId) {
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.status(HttpStatus.OK).body("댓글이 삭제되었습니다.");
    }

    @Operation(summary = "대댓글 삭제")
    @DeleteMapping("/{postId}/{parentCommentId}/{replyId}")
    public ResponseEntity<String> deleteReply(@PathVariable String postId, @PathVariable String parentCommentId, @PathVariable String replyId) {
        commentService.deleteReply(postId, parentCommentId, replyId);
        return ResponseEntity.status(HttpStatus.OK).body("대댓글이 삭제되었습니다.");
    }



}
