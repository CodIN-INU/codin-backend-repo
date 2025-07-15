package inu.codin.codin.common.security.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.common.security.dto.SignUpAndLoginRequestDto;
import inu.codin.codin.common.security.service.AuthCommonService;
import inu.codin.codin.common.security.service.AuthSessionService;
import inu.codin.codin.common.security.service.JwtService;
import inu.codin.codin.domain.user.dto.request.UserProfileRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/auth")
@Tag(name = "User Auth API", description = "유저 회원가입, 로그인, 로그아웃, 리이슈 API")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final AuthCommonService authCommonService;
    private final AuthSessionService authSessionService;

    @GetMapping("/google")
    public ResponseEntity<SingleResponse<?>> googleLogin(HttpServletResponse response,
                                                         @RequestParam(required = false, value = "redirect_url") String redirect_url) throws IOException {
        authSessionService.setSession(redirect_url);
        response.sendRedirect("/oauth2/authorization/google");
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "google OAuth2 Login Redirect",null));
    }

    @GetMapping("/dev/google")
    public ResponseEntity<SingleResponse<?>> devGoogleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/dev/oauth2/authorization/google");
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "google OAuth2 Login Redirect",null));
    }

    @GetMapping("/apple")
    public ResponseEntity<SingleResponse<?>> appleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/api/oauth2/authorization/apple");
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "apple OAuth2 Login Redirect",null));
    }

    @GetMapping("/dev/apple")
    public ResponseEntity<SingleResponse<?>> devAppleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/dev/oauth2/authorization/apple");
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "apple OAuth2 Login Redirect",null));
    }

    @Operation(summary = "회원 정보 입력 마무리")
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponse<?>> completeUserProfile(
            @RequestPart @Valid UserProfileRequestDto userProfileRequestDto,
            @RequestPart(value = "userImage", required = false) MultipartFile userImage,
            HttpServletResponse response) {
        authCommonService.completeUserProfile(userProfileRequestDto, userImage, response);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "회원 정보 입력 마무리 성공", null));
    }


    @Operation(
            summary = "Admin, test 계정 로그인"
    )
    @PostMapping("/login")
    public ResponseEntity<SingleResponse<?>> portalSignUp(@RequestBody @Valid SignUpAndLoginRequestDto signUpAndLoginRequestDto, HttpServletResponse response) {
        authCommonService.login(signUpAndLoginRequestDto, response);

        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "로그인 성공", "기존 유저 로그인 완료"));

    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        jwtService.deleteToken(response);
        return ResponseEntity.ok().body(new SingleResponse<>(200, "로그아웃 성공", null));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        jwtService.reissueToken(request, response);
        return ResponseEntity.ok().body(new SingleResponse<>(200, "토큰 재발급 성공", null));
    }
}
