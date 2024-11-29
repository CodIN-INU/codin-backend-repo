package inu.codin.codin.domain.post.comment.controller;

import inu.codin.codin.domain.post.dto.request.CommentCreateRequestDTO;
import inu.codin.codin.domain.post.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.dto.response.CommentsResponseDTO;
import inu.codin.codin.domain.post.comment.service.CommentService;
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

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "댓글 추가")
    @PostMapping("/{postId}")
    public ResponseEntity<String> addComment(@PathVariable String postId,
                                             @RequestBody @Valid CommentCreateRequestDTO requestDTO) {
        commentService.addComment(postId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("댓글이 추가되었습니다.");
    }

    @Operation(summary = "대댓글 추가")
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<String> addReply(@PathVariable String commentId,
                                           @RequestBody @Valid ReplyCreateRequestDTO requestDTO) {
        commentService.addReply(commentId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("대댓글이 추가되었습니다.");
    }

    @Operation(summary = "해당 게시물의 댓글 및 대댓글 조회")
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentsResponseDTO>> getCommentsByPostId(@PathVariable String postId) {
        List<CommentsResponseDTO> response = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.status(HttpStatus.OK).body("댓글이 삭제되었습니다.");
    }

    @Operation(summary = "대댓글 삭제")
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<String> deleteReply(@PathVariable String replyId) {
        commentService.deleteReply(replyId);
        return ResponseEntity.status(HttpStatus.OK).body("대댓글이 삭제되었습니다.");
    }
}