package inu.codin.codin.domain.like.controller;


import inu.codin.codin.domain.like.dto.LikeRequestDto;
import inu.codin.codin.domain.like.dto.LikeType;
import inu.codin.codin.domain.like.dto.LikedResponseDto;
import inu.codin.codin.global.common.response.SingleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "likeClient", url = "${server.feign.url}")
public interface LikeFeignClient {

    @PostMapping
    ResponseEntity<SingleResponse<?>> toggleLike(@RequestBody LikeRequestDto likeRequestDto);

    @GetMapping
    Integer getLikeCount(@RequestParam("likeType") LikeType likeType,
                                                     @RequestParam("id") String id);

    @GetMapping("/user")
    Boolean isUserLiked(@RequestParam("likeType") LikeType likeType,
                                                    @RequestParam("id") String id,
                                                    @RequestParam("userId") String userId);

    @GetMapping("/list")
    List<LikedResponseDto> getLiked(@RequestParam("likeType") LikeType likeType,
                                    @RequestParam("userId") String userId);
}
