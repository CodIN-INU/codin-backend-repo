package inu.codin.codin.domain.post.domain.comment.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentEntity extends BaseTimeEntity {
    @Id @NotNull
    private ObjectId _id;

    @NotNull
    private ObjectId postId;  //게시글 ID 참조

    @NotNull
    private ObjectId userId;

    @NotBlank
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
