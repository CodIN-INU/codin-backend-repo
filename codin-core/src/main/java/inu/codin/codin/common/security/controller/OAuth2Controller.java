package inu.codin.codin.common.security.controller;

import inu.codin.codin.common.response.SingleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login/oauth2/code")
public class OAuth2Controller {

    @GetMapping("/google")
    public ResponseEntity<SingleResponse<?>> googleLogin() {
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "OAuth2 로그인", "OAuth2 로그인 완료"));
    }
}
