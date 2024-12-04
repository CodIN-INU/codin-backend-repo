package inu.codin.codin.domain.post.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
public class CommentResponseDTO {
    @Schema(description = "댓글 또는 대댓글 ID", example = "111111")
    @NotBlank
    private String _id;

    @Schema(description = "유저 ID", example = "user123")
    @NotBlank
    private String userId;

    @Schema(description = "댓글 또는 대댓글 내용", example = "This is a comment.")
    @NotBlank
    private String content;

    @Schema(description = "대댓글 리스트", example = "[...]")
    private List<CommentResponseDTO> replies;

    @Schema(description = "좋아요 수", example = "5")
    private int likeCount;

    @Schema(description = "삭제 여부", example = "false")
    private boolean isDeleted;

    public CommentResponseDTO(String _id, String userId, String content, List<CommentResponseDTO> replies, int likeCount, boolean isDeleted) {
        this._id = _id;
        this.userId = userId;
        this.content = content;
        this.replies = replies;
        this.likeCount = likeCount;
        this.isDeleted = isDeleted;
    }
}