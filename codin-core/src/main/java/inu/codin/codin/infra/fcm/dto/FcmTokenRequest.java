package inu.codin.codin.infra.fcm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class FcmTokenRequest {

    @Schema(description = "Fcm Token")
    @NotBlank
    private String fcmToken;

    @Schema(description = "Android, IOS")
    @NotBlank
    private String deviceType;

}
