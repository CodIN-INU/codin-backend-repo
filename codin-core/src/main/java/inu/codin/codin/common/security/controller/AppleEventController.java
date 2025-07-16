package inu.codin.codin.common.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AppleEventController {

    private final ObjectMapper objectMapper;

    @PostMapping("/login/oauth2/code/apple")
    public ResponseEntity<?> appleCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Apple OAuth2에서 전달된 인가 코드, id_token, user 데이터 가져오기
            String authorizationCode = request.getParameter("code");
            String idToken = request.getParameter("id_token");
            String userJson = request.getParameter("user");  // 최초 로그인 시에만 제공됨

            log.info("Apple OAuth Callback - code: {}, id_token: {}", authorizationCode, idToken);
            log.info("Apple OAuth Callback - user 정보: {}", userJson);

            if (userJson != null) {
                // user 정보를 Base64 인코딩하여 쿠키로 저장 (보안상 HttpOnly 설정)
                Cookie userCookie = new Cookie("apple_user", Base64.getEncoder().encodeToString(userJson.getBytes(StandardCharsets.UTF_8)));
                userCookie.setHttpOnly(true);
                userCookie.setPath("/");
                userCookie.setMaxAge(60 * 5);  // 5분 유지
                response.addCookie(userCookie);
                log.info("Apple 최초 로그인 - user 정보 쿠키 저장 완료");
            }

            return ResponseEntity.ok("Apple 로그인 처리 중");

        } catch (Exception e) {
            log.error("Apple OAuth Callback 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Apple OAuth 처리 실패");
        }
    }
}