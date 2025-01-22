package inu.codin.codin.domain.user.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.user.dto.request.UserDeleteRequestDto;
import inu.codin.codin.domain.user.dto.request.UserNicknameRequestDto;
import inu.codin.codin.domain.user.dto.response.UserInfoResponseDto;
import inu.codin.codin.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/users")
@Tag(name = "User Auth API", description = "유저 회원가입, 로그인, 로그아웃, 리이슈 API")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "해당 사용자 게시물 전체 조회"
    )
    @GetMapping("/post")
    public ResponseEntity<SingleResponse<PostPageResponse>> getAllUserPosts(@RequestParam("page") int pageNumber) {
        PostPageResponse posts = userService.getAllUserPosts(pageNumber);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "사용자 게시물 조회 성공", posts));
    }

    @Operation(
            summary = "유저가 좋아요 누른 게시글 반환"
    )
    @GetMapping("/like")
    public ResponseEntity<SingleResponse<PostPageResponse>> getUserLike(@RequestParam("page") int pageNumber){
        PostPageResponse posts = userService.getPostUserInteraction(pageNumber, UserService.InteractionType.LIKE);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "사용자가 좋아요 누른 게시물 조회 성공", posts));
    }

    @Operation(
            summary = "유저가 스크랩한 게시글 반환"
    )
    @GetMapping("/scrap")
    public ResponseEntity<SingleResponse<PostPageResponse>> getUserScrap(@RequestParam("page") int pageNumber){
        PostPageResponse posts = userService.getPostUserInteraction(pageNumber, UserService.InteractionType.SCRAP);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "사용자가 스크랩한 게시물 조회 성공", posts));
    }

    @Operation(
            summary = "유저가 작성한 댓글의 게시글 반환"
    )
    @GetMapping("/comment")
    public ResponseEntity<SingleResponse<PostPageResponse>> getUserComment(@RequestParam("page") int pageNumber){
        PostPageResponse posts = userService.getPostUserInteraction(pageNumber, UserService.InteractionType.COMMENT);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "사용자가 작성한 댓글의 게시물 조회 성공", posts));
    }

    @Operation(
            summary = "회원 탈퇴"
    )
    @DeleteMapping
    public ResponseEntity<SingleResponse<?>> deleteUser(@RequestBody @Valid UserDeleteRequestDto userDeleteRequestDto){
        userService.deleteUser(userDeleteRequestDto);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "회원 탈퇴 완료", null));
    }

    @Operation(
            summary = "유저 정보 반환"
    )
    @GetMapping
    public ResponseEntity<SingleResponse<UserInfoResponseDto>> getUserInfo(){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "유저 정보 반환 완료", userService.getUserInfo()));
    }

    @Operation(
            summary = "유저 닉네임 수정"
    )
    @PutMapping
    public ResponseEntity<SingleResponse<?>> updateUserInfo(@RequestBody @Valid UserNicknameRequestDto userUpdateRequestDto){
        userService.updateUserInfo(userUpdateRequestDto);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "유저 정보 수정 완료", null));
    }

    @Operation(
            summary = "유저 사진 수정"
    )
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>> updateUserProfile(@RequestPart(value = "postImages", required = true) MultipartFile profileImage){
        userService.updateUserProfile(profileImage);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "유저 사진 수정 완료", null));
    }
}