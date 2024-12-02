package inu.codin.codin.domain.post.domain.like.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.post.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.post.domain.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(
            summary = "게시물, 댓글, 대댓글 좋아요 추가 "
    )
    @PostMapping
    public ResponseEntity<SingleResponse<?>> addLike(@RequestBody @Valid LikeRequestDto likeRequestDto) {
        likeService.addLike(likeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "좋아요가 추가되었습니다.", null));
    }

    @Operation(summary = "게시물, 댓글, 대댓글 좋아요 삭제 ")
    @DeleteMapping
    public ResponseEntity<SingleResponse<?>> removeLike(@RequestBody @Valid LikeRequestDto likeRequestDto) {
        likeService.removeLike(likeRequestDto);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "좋아요가 취소되었습니다.", null));
    }
}