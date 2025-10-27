package inu.codin.codin.infra.fcm.controller.swagger;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.infra.fcm.dto.request.FcmTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "FCM API", description = "FCM 토큰 저장 API")
public interface FcmController {

    @Operation(summary = "FCM 토큰 저장", description = "알림 설정을 위해서는 미리 토큰 저장이 필요합니다")
    public ResponseEntity<SingleResponse<Null>> sendFcmMessage(
            @RequestBody @Valid FcmTokenRequest fcmTokenRequest
    );

    @Operation(summary = "FCM 토픽 구독", description = "토픽을 구독하여 해당 토픽으로 알림이 옵니다.")
    public ResponseEntity<SingleResponse<Null>> subscribeTopic(
            @RequestParam String topic
    );

    @Operation(summary = "FCM 토픽 구독 해제", description = "토픽 구독을 해제합니다.")
    public ResponseEntity<SingleResponse<Null>> unsubscribeTopic(
            @RequestParam String topic
    );
}
