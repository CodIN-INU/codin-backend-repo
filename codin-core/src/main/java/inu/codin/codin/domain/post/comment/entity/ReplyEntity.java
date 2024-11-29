package inu.codin.codin.domain.post.comment.entity;

import inu.codin.codin.common.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "replies")
@Getter
public class ReplyEntity extends BaseTimeEntity {
    @Id
    @NotBlank
    private String replyId;

    private String commentId; // 댓글 ID 참조
    private String userId; // 작성자 ID
    private String content;
    private boolean isDeleted = false;

    private int likeCount = 0; // 좋아요 카운트

    @Builder
    public ReplyEntity(String replyId, String commentId, String userId, String content, int likeCount) {
        this.replyId = replyId;
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.likeCount = likeCount;
    }

    // Soft Delete
    public void softDelete() {
        this.isDeleted = true;
        this.delete();
    }

    //좋아요 수 업데이트
    public void updateLikeCount(int likeCount) {
        this.likeCount=likeCount;
    }

}
