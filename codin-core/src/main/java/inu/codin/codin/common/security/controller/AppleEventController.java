package inu.codin.codin.common.security.controller;

import inu.codin.codin.common.security.dto.apple.AppleAuthRequest;
import inu.codin.codin.common.security.dto.apple.AppleLoginResponse;
import inu.codin.codin.common.security.service.AppleOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/apple")
@RequiredArgsConstructor
public class AppleEventController {

    private final AppleOAuth2UserService appleOAuth2UserService;

//    @PostMapping("/login")
//    public ResponseEntity<AppleLoginResponse> handleAppleLogin(@RequestBody AppleAuthRequest appleAuthRequest) {
//        try {
//            // AppleOAuth2UserService에 AppleAuthRequest를 직접 처리하는 메서드가 있다고 가정합니다.
//            OAuth2User principalUser = appleOAuth2UserService.loadUser(appleAuthRequest);
//            // principalUser.getName()는 Apple의 고유 식별자(sub)를 반환하도록 구현되어 있습니다.
//            String accountId = principalUser.getName();
//            return ResponseEntity.ok(new AppleLoginResponse(accountId, "User processed successfully"));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new AppleLoginResponse(null, "Authentication failed: " + e.getMessage()));
//        }
//    }
}