package inu.codin.codin.domain.post.like;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "게시물, 댓글, 대댓글 좋아요 추가"+
            "entityType = post,comment,reply"
            +"entityId = postId, commentId, replyId")
    @PostMapping("/{entityType}/{entityId}/{userId}")
    public ResponseEntity<String> likeEntity(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @PathVariable String userId) {
        likeService.addLike(entityType, entityId, userId);
        return ResponseEntity.ok("Liked successfully");
    }

    @Operation(summary = "게시물, 댓글, 대댓글 좋아요 삭제 " +
            "entityType = post,comment,reply"
    +"entityId = postId, commentId, replyId")
    @DeleteMapping("/{entityType}/{entityId}/{userId}")
    public ResponseEntity<String> unlikeEntity(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @PathVariable String userId) {
        likeService.removeLike(entityType, entityId, userId);
        return ResponseEntity.ok("Unliked successfully");
    }
}