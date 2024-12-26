package inu.codin.codin.domain.user.controller;

import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.user.dto.UserCreateRequestDto;
import inu.codin.codin.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/users")
@Tag(name = "User Auth API", description = "유저 회원가입, 로그인, 로그아웃, 리이슈 API")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<SingleResponse<?>> signUpUser(
            @RequestBody @Valid UserCreateRequestDto userCreateRequestDto) {
        userService.createUser(userCreateRequestDto);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "회원가입 성공", null));
    }

    @Operation(
            summary = "유저가 좋아요 누른 게시글 반환"
    )
    @GetMapping("/like")
    public ResponseEntity<SingleResponse<PostPageResponse>> getUserLike(@RequestParam("page") int pageNumber){
        PostPageResponse posts = userService.getPostUserLike(pageNumber);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "사용자가 좋아요 누른 게시물 조회 성공", posts));
    }

    @Operation(
            summary = "유저가 스크랩한 게시글 반환"
    )
    @GetMapping("/scrap")
    public ResponseEntity<SingleResponse<PostPageResponse>> getUserScrap(@RequestParam("page") int pageNumber){
        PostPageResponse posts = userService.getPostUserScrap(pageNumber);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "사용자가 좋아요 누른 게시물 조회 성공", posts));
    }
}