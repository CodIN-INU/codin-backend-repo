package inu.codin.codin.domain.post.domain.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
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

    @Schema(description = "유저 이미지 url")
    private final String userImageUrl;

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
    private final UserInfo userInfo;

    @Builder
    public CommentResponseDTO(String _id, String userId, String content,
                              String nickname, String userImageUrl, Boolean anonymous ,
                              List<CommentResponseDTO> replies, int likeCount,
                              boolean isDeleted, LocalDateTime createdAt, UserInfo userInfo) {
        this._id = _id;
        this.userId = userId;
        this.content = content;
        this.nickname = nickname;
        this.userImageUrl = userImageUrl;
        this.anonymous = anonymous;
        this.replies = replies;
        this.likeCount = likeCount;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.userInfo = userInfo;
    }

    public static CommentResponseDTO commentOf(CommentEntity commentEntity, String nickname, String userImageUrl, List<CommentResponseDTO> repliesByCommentId, int like, UserInfo userInfoAboutPost){
        return CommentResponseDTO.builder()
                ._id(commentEntity.get_id().toString())
                .userId(commentEntity.getUserId().toString())
                .content(commentEntity.getContent())
                .nickname(nickname)
                .userImageUrl(userImageUrl)
                .anonymous(commentEntity.isAnonymous())
                .replies(repliesByCommentId)
                .likeCount(like)
                .isDeleted(commentEntity.getDeletedAt() != null)
                .createdAt(commentEntity.getCreatedAt())
                .userInfo(userInfoAboutPost)
                .build();
    }

    public static CommentResponseDTO replyOf(ReplyCommentEntity replyCommentEntity, String nickname, String userImageUrl, List<CommentResponseDTO> repliesByCommentId, int like, UserInfo userInfoAboutPost){
        return CommentResponseDTO.builder()
                ._id(replyCommentEntity.get_id().toString())
                .userId(replyCommentEntity.getUserId().toString())
                .content(replyCommentEntity.getContent())
                .nickname(nickname)
                .userImageUrl(userImageUrl)
                .anonymous(replyCommentEntity.isAnonymous())
                .replies(repliesByCommentId)
                .likeCount(like)
                .isDeleted(replyCommentEntity.getDeletedAt() != null)
                .createdAt(replyCommentEntity.getCreatedAt())
                .userInfo(userInfoAboutPost)
                .build();
    }
    // 기존 객체에서 replies 리스트만 변경
    public CommentResponseDTO repliesFrom(List<ReportedCommentDetailResponseDTO> updatedReplies) {
        List<CommentResponseDTO> commentReplies = updatedReplies.stream()
                .map(reply -> (CommentResponseDTO) reply) // 변환
                .toList();

        return CommentResponseDTO.builder()
                ._id(this._id)
                .userId(this.userId)
                .content(this.content)
                .nickname(this.nickname)
                .userImageUrl(this.userImageUrl)
                .anonymous(this.anonymous)
                .replies(commentReplies) // 수정된 대댓글 리스트 적용
                .likeCount(this.likeCount)
                .createdAt(this.createdAt)
                .userInfo(this.userInfo)
                .build();
    }

    @Getter
    public static class UserInfo {
        private final boolean isLike;

        @Builder
        public UserInfo(boolean isLike) {
            this.isLike = isLike;
        }
    }

}