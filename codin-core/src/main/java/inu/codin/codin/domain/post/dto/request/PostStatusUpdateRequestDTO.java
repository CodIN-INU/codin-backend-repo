package inu.codin.codin.domain.post.dto.request;

import inu.codin.codin.domain.post.entity.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostStatusUpdateRequestDTO {
    @Schema(description = "게시물 상태", example = "ACTIVE")
    @NotNull
    private PostStatus postStatus;

}
