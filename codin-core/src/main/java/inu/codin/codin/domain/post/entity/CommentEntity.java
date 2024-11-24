package inu.codin.codin.domain.post.entity;

import inu.codin.codin.common.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommentEntity extends BaseTimeEntity {
    @Id //몽고디비에선 독립된 컬렉션이 아닐 경우 자동으로 ID 값을 생성하지 않음
    private String commentId;
    private String userId;
    private String content;
    private boolean isDeleted = false; // Soft delete 상태
    private List<CommentEntity> replies = new ArrayList<>();
    @Builder
    public CommentEntity(String commentId, String userId, String content, List<CommentEntity> replies) {
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.replies = replies != null ? replies : new ArrayList<>(); // null 체크 후 초기화

    }

    // Soft Delete
    public void softDelete() {
        this.isDeleted = true;
        this.delete();
    }

    public void softDeleteReply(String replyId) {
        this.replies.stream()
                .filter(reply -> reply.getCommentId().equals(replyId))
                .findFirst()
                .ifPresent(CommentEntity::softDelete);
    }

    // 대댓글 추가
    public void addReply(CommentEntity reply) {
        if (this.replies == null) {
            this.replies = new ArrayList<>(); // null 방지
        }
        this.replies.add(reply);
    }
}
