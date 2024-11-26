package inu.codin.codin.infra.fcm.controller;

import inu.codin.codin.common.ResponseUtils;
import inu.codin.codin.infra.fcm.dto.FcmTokenRequest;
import inu.codin.codin.infra.fcm.service.FcmService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fcm")
@Tag(name = "FCM API", description = "FCM 토큰 저장 API")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/save")
    public ResponseEntity<?> sendFcmMessage(
            @RequestBody @Valid FcmTokenRequest fcmTokenRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fcmService.saveFcmToken(fcmTokenRequest, userDetails);
        return ResponseUtils.successMsg("FCM 토큰 저장 성공");
    }
}
