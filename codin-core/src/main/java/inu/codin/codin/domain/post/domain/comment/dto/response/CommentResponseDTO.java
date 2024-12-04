package inu.codin.codin.domain.post.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.List;

@Getter
public class CommentResponseDTO {
    @Schema(description = "댓글 또는 대댓글 ID", example = "111111")
    @NotBlank
    private final String _id;

    @Schema(description = "유저 ID", example = "user123")
    @NotBlank
    private final String userId;

    @Schema(description = "댓글 또는 대댓글 내용", example = "This is a comment.")
    @NotBlank
    private final String content;

    @Schema(description = "대댓글 리스트", example = "[...]")
    private final List<CommentResponseDTO> replies;

    @Schema(description = "좋아요 수", example = "5")
    private final int likeCount;

    @Schema(description = "삭제 여부", example = "false")
    private final boolean isDeleted;

    public CommentResponseDTO(String _id, String userId, String content, List<CommentResponseDTO> replies, int likeCount, boolean isDeleted) {
        this._id = _id;
        this.userId = userId;
        this.content = content;
        this.replies = replies;
        this.likeCount = likeCount;
        this.isDeleted = isDeleted;
    }
}