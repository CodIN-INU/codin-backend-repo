package inu.codin.codin.domain.post.domain.comment.reply.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.post.domain.comment.reply.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.reply.dto.request.ReplyUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.reply.service.ReplyCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/replies")
@Tag(name = "ReplyComment API", description = "대댓글 API")
public class ReplyCommentController {

    private final ReplyCommandService replyCommandService;

    @Operation(summary = "대댓글 추가")
    @PostMapping("/{commentId}")
    public ResponseEntity<SingleResponse<Void>> addReply(@PathVariable String commentId,
                                                      @RequestBody @Valid ReplyCreateRequestDTO requestDTO) {
        replyCommandService.addReply(commentId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "대댓글이 추가되었습니다.", null));

    }

    @Operation(summary = "대댓글 삭제")
    @DeleteMapping("/{replyId}")
    public ResponseEntity<SingleResponse<Void>> softDeleteReply(@PathVariable String replyId) {
        replyCommandService.softDeleteReply(replyId);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "대댓글이 삭제되었습니다.", null));
    }

    @Operation(summary = "대댓글 수정")
    @PatchMapping("/{replyId}")
    public ResponseEntity<SingleResponse<Void>> updateReply(@PathVariable String replyId, @RequestBody @Valid ReplyUpdateRequestDTO requestDTO){
        replyCommandService.updateReply(replyId, requestDTO);
        return ResponseEntity.status(HttpStatus.OK).
                body(new SingleResponse<>(200, "대댓글이 수정되었습니다.", null));

    }
}
