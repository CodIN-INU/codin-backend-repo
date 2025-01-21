package inu.codin.codin.common.security.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.common.security.dto.SignUpAndLoginRequestDto;
import inu.codin.codin.common.security.service.AuthService;
import inu.codin.codin.common.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth")
@Tag(name = "User Auth API", description = "유저 회원가입, 로그인, 로그아웃, 리이슈 API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody SignUpAndLoginRequestDto loginRequestDto, HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(loginRequestDto.getStudentId(), loginRequestDto.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        jwtService.createToken(response);

        return ResponseEntity.ok().body(new SingleResponse<>(200, "로그인 성공", null));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        jwtService.deleteToken();
        return ResponseEntity.ok().body(new SingleResponse<>(200, "로그아웃 성공", null));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        jwtService.reissueToken(request, response);
        return ResponseEntity.ok().body(new SingleResponse<>(200, "토큰 재발급 성공", null));
    }

    @Operation(
            summary = "포탈 로그인",
            description = "포탈 아이디, 비밀번호를 통해 로그인 진행 및 학적 정보 반환"
    )
    @PostMapping("/portal")
    public ResponseEntity<SingleResponse<?>> portalSignUp(@RequestBody @Valid SignUpAndLoginRequestDto signUpAndLoginRequestDto) throws Exception {
        authService.signUp(signUpAndLoginRequestDto);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "포탈 로그인을 통한 학적 정보 반환 완료", null));
    }
}
