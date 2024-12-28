package inu.codin.codin.domain.post.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
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

    @Schema(description = "유저 nickname 익명시 익명으로 표시됨")
    private final String nickname;

    @Schema(description = "익명 여부", example = "true")
    @NotNull
    private final boolean anonymous;

    @Schema(description = "대댓글 리스트", example = "[...]")
    private final List<CommentResponseDTO> replies;

    @Schema(description = "좋아요 수", example = "5")
    private final int likeCount;

    @Schema(description = "삭제 여부", example = "false")
    private final boolean isDeleted;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Schema(description = "댓글 작성 시간", example = "2024-12-05 02:22:48")
    private final LocalDateTime createdAt;

    @Schema(description = "해당 댓글 대한 유저 반응 여부")
    private final CommnetUserInfo CommnetUserInfo;

    public CommentResponseDTO(String _id, String userId, String content,
                              String nickname, Boolean anonymous ,
                              List<CommentResponseDTO> replies, int likeCount,
                              boolean isDeleted, LocalDateTime createdAt, CommnetUserInfo CommnetUserInfo) {
        this._id = _id;
        this.userId = userId;
        this.content = content;
        this.nickname = nickname;
        this.anonymous = anonymous;
        this.replies = replies;
        this.likeCount = likeCount;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.CommnetUserInfo = CommnetUserInfo;
    }

    @Getter
    public static class CommnetUserInfo {
        private final boolean isLike;

        @Builder
        public CommnetUserInfo(boolean isLike) {
            this.isLike = isLike;
        }
    }

}