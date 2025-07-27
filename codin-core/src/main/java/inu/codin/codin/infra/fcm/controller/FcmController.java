package inu.codin.codin.infra.fcm.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.infra.fcm.dto.request.FcmTokenRequest;
import inu.codin.codin.infra.fcm.service.FcmService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestBody @Valid FcmTokenRequest fcmTokenRequest
    ) {
        fcmService.saveFcmToken(fcmTokenRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new SingleResponse<>(202, "FCM 토큰 저장 성공", null));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribeTopic(
            @RequestBody String topic
    ) {
        fcmService.subscribeTopic(topic);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new SingleResponse<>(202, "FCM 토픽 구독 성공", null));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribeTopic(
            @RequestBody String topic
    ) {
        fcmService.unsubscribeTopic(topic);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new SingleResponse<>(202, "FCM 토픽 구독 해제 성공", null));
    }

}
