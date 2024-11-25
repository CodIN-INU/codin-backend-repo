package inu.codin.codin.infra.fcm.controller;

import inu.codin.codin.infra.fcm.dto.FcmTokenRequest;
import inu.codin.codin.infra.fcm.service.FcmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/send")
    public void sendFcmMessage(
            @RequestBody @Valid FcmTokenRequest fcmTokenRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        fcmService.saveFcmToken(fcmTokenRequest, userDetails);
    }
}
