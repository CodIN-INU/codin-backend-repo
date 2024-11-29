package inu.codin.codin.domain.post.comment.entity;

import inu.codin.codin.common.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "comments")
@Getter
public class CommentEntity extends BaseTimeEntity {
    @Id @NotBlank
    private String commentId;

    private String postId;  //게시글 ID 참조
    private String userId;
    private String content;
    private boolean isDeleted = false; // Soft delete 상태

    private int likeCount = 0;  // 좋아요 수 (Redis에서 관리)

    @Builder
    public CommentEntity(String commentId, String postId, String userId, String content) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
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
