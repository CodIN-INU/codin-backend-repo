package inu.codin.codin.domain.like.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.like.dto.LikeResponseType;
import inu.codin.codin.domain.like.dto.LikedResponseDto;
import inu.codin.codin.domain.like.dto.request.LikeRequestDto;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Tag(name = "Like API", description = "게시물, 댓글, 대댓글, 수강후기 좋아요 API")
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "POST(게시물), COMMENT(댓글), REPLY(대댓글) 좋아요 토글",
            description = "LikeType = POST, COMMENT, REPLY, LECTURE, REVIEW <br>" +
                    "id = 좋아요를 누를 entity의 pk <br> " +
                    "외부 서버에서 LECTURE(과목), REVIEW(수강 후기) 좋아요 기능으로 사용")
    @PostMapping
    public ResponseEntity<SingleResponse<?>> toggleLike(@RequestBody @Valid LikeRequestDto likeRequestDto) {
        LikeResponseType message = likeService.toggleLike(likeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "좋아요가 " + message.getDescription() + "되었습니다.", message.toString()));
    }

    @Hidden
    @GetMapping
    public Integer getLikeCount(@RequestParam("likeType") LikeType likeType,
                                                          @RequestParam("id") String id) {
        return likeService.getLikeCount(likeType, id);
    }

    @Hidden
    @GetMapping("/user")
    public Boolean isUserLiked(@RequestParam("likeType") LikeType likeType,
                                                         @RequestParam("id") String id,
                                                         @RequestParam("userId") String userId) {
        return likeService.isLiked(likeType, id, userId);
    }

    @Hidden
    @GetMapping("/list")
    public List<LikedResponseDto> getLikedId(@RequestParam("likeType") LikeType likeType,
                                             @RequestParam("userId") String userId) {
        return likeService.getLikedId(likeType, userId);
    }
}