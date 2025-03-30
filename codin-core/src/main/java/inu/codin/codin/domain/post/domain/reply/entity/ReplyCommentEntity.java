package inu.codin.codin.domain.post.domain.reply.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "replies")
@Getter
public class ReplyCommentEntity extends BaseTimeEntity {
    @Id
    @NotBlank
    private ObjectId _id;
    private ObjectId commentId; // 댓글 ID 참조
    private ObjectId userId; // 작성자 ID
    private String content;

    private boolean anonymous;

    @Builder
    public ReplyCommentEntity(ObjectId _id, ObjectId commentId, ObjectId userId, boolean anonymous, String content) {
        this._id = _id;
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.anonymous = anonymous;
    }

    public void updateReply(String content) {
        this.content = content;
    }

}
