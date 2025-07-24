package inu.codin.codin.domain.like.controller;

import inu.codin.codin.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.like.dto.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.global.common.response.SingleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeFeignClient likeFeignClient;
    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<SingleResponse<?>> toggleLike(@RequestBody @Valid LikeRequestDto likeRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, likeService.toggleLike(likeRequestDto), null));
    }

    @GetMapping
    public ResponseEntity<SingleResponse<?>> getLikeCount(@RequestParam("likeType") LikeType likeType,
                                                          @RequestParam("id") String id) {
        return likeFeignClient.getLikeCount(likeType, id);
    }

    @GetMapping("/user")
    public ResponseEntity<SingleResponse<?>> isUserLiked(@RequestParam("likeType") LikeType likeType,
                                                         @RequestParam("id") String id) {
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "유저의 좋아요 현황 반환", likeService.isLiked(likeType, id)));
    }
}
