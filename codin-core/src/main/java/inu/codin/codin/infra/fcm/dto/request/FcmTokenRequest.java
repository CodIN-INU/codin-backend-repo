package inu.codin.codin.infra.fcm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class FcmTokenRequest {

    @Schema(description = "Fcm Token", example = "FCM 토큰")
    @NotBlank
    private String fcmToken;

    @Schema(description = "Android, IOS", example = "디바이스 종류")
    @NotBlank
    private String deviceType;

}
