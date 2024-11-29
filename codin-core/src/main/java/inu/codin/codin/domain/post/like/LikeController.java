package inu.codin.codin.domain.post.like;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes/{postId}")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(
            summary = "좋아요 추가"
    )
    @PostMapping("/{userId}")
    public ResponseEntity<String> likePost(@PathVariable String postId, @PathVariable String userId) {
        likeService.addLike(postId, userId);
        return ResponseEntity.ok("Liked successfully");
    }

    @Operation(
            summary = "좋아요 삭제"
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> unlikePost(@PathVariable String postId, @PathVariable String userId) {
        likeService.removeLike(postId, userId);
        return ResponseEntity.ok("Unliked successfully");
    }

    @Operation(
            summary = "해당 게시물 좋아요 조회"
    )
    @GetMapping
    public ResponseEntity<Long> getLikeCount(@PathVariable String postId) {
        long likeCount = likeService.getLikeCount(postId);
        return ResponseEntity.ok(likeCount);
    }
}