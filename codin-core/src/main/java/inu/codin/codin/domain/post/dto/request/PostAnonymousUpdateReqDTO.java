package inu.codin.codin.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostAnonymousUpdateReqDTO {
    @Schema(description = "익명 여부", example = "true")
    @NotBlank
    private boolean isAnonymous;
}
