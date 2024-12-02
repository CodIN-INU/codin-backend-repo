package inu.codin.codin.domain.post.domain.reply.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyCreateRequestDTO {
//    @Schema(description = "유저 ID", example = "111111")
//    @NotBlank
//    private String userId;

    @Schema(description = "댓글 내용", example = "content")
    @NotBlank
    private String content;
}
