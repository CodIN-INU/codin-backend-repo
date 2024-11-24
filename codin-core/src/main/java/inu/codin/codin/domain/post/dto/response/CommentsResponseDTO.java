package inu.codin.codin.domain.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CommentsResponseDTO {
    @Schema(description = "댓글 ID", example = "111111")
    @NotBlank
    private String commentId;

    @Schema(description = "유저 ID", example = "111111")
    @NotBlank
    private String userId;

    @Schema(description = "댓글 내용", example = "111111")
    @NotBlank
    private String content;

    @Schema(description = "대댓글", example = "111111")
    private List<CommentsResponseDTO> replies;

    public CommentsResponseDTO(String commentId, String userId, String content, List<CommentsResponseDTO> replies) {
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.replies = replies;
    }
}
