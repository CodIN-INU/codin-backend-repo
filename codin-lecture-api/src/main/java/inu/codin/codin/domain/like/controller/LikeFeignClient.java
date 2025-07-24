package inu.codin.codin.domain.like.controller;


import inu.codin.codin.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.like.dto.LikeType;
import inu.codin.codin.global.common.response.SingleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "likeClient", url = "http://localhost:8080")
public interface LikeFeignClient {

    @PostMapping("/likes")
    ResponseEntity<SingleResponse<?>> toggleLike(@RequestBody LikeRequestDto likeRequestDto);

    @GetMapping("/likes")
    ResponseEntity<SingleResponse<?>> getLikeCount(@RequestParam("likeType") LikeType likeType,
                                                     @RequestParam("id") String id);

    @GetMapping("/likes/user")
    ResponseEntity<SingleResponse<?>> isUserLiked(@RequestParam("likeType") LikeType likeType,
                                                    @RequestParam("id") String id,
                                                    @RequestParam("userId") String userId);
}
