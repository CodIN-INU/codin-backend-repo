package inu.codin.codin.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OneCharNameRequestDto {
    @NotBlank(message = "알림 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "알림 내용은 필수입니다.")
    private String body;
}