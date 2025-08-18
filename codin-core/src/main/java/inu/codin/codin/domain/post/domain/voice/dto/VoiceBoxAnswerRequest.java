package inu.codin.codin.domain.post.domain.voice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class VoiceBoxAnswerRequest {

    @Schema(description = "답변 내용", example = "학회비를 납부하면 다양한 혜택을 받을 수 있습니다.")
    @NotBlank(message = "답변 내용은 필수입니다.")
    private String answer;
}
