package inu.codin.codin.domain.like.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Tag(name = "Like API", description = "게시물, 댓글, 대댓글, 수강후기 좋아요 API")
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "게시물, 댓글, 대댓글, 수강후기 좋아요 토글")
    @PostMapping
    public ResponseEntity<SingleResponse<?>> toggleLike(@RequestBody @Valid LikeRequestDto likeRequestDto) {
        String message = likeService.toggleLike(likeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, message, null));
    }

}