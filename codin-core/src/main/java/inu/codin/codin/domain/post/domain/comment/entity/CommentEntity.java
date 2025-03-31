package inu.codin.codin.domain.post.domain.comment.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comments")
@Getter
public class CommentEntity extends BaseTimeEntity {
    @Id @NotBlank
    private ObjectId _id;

    private ObjectId postId;  //게시글 ID 참조
    private ObjectId userId;
    private String content;

    private boolean anonymous;

    @Builder
    public CommentEntity(ObjectId _id, ObjectId postId, ObjectId userId, String content, Boolean anonymous) {
        this._id = _id;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.anonymous = anonymous;
    }

    public void updateComment(String content) {
        this.content = content;
    }



}
