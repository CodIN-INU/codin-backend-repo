package inu.codin.codin.infra.fcm.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.infra.fcm.controller.swagger.FcmController;
import inu.codin.codin.infra.fcm.dto.request.FcmTokenRequest;
import inu.codin.codin.infra.fcm.service.FcmService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmControllerImpl implements FcmController {

    private final FcmService fcmService;

    @PostMapping("/save")
    public ResponseEntity<SingleResponse<Null>> sendFcmMessage(
            @RequestBody @Valid FcmTokenRequest fcmTokenRequest
    ) {
        fcmService.saveFcmToken(fcmTokenRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new SingleResponse<>(202, "FCM 토큰 저장 성공", null));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<SingleResponse<Null>> subscribeTopic(
            @RequestParam String topic
    ) {
        fcmService.subscribeTopic(topic);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new SingleResponse<>(202, "FCM 토픽 구독 성공", null));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<SingleResponse<Null>> unsubscribeTopic(
            @RequestParam String topic
    ) {
        fcmService.unsubscribeTopic(topic);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new SingleResponse<>(202, "FCM 토픽 구독 해제 성공", null));
    }

}
