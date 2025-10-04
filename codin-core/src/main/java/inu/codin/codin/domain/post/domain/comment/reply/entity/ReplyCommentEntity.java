package inu.codin.codin.domain.post.domain.comment.reply.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.post.domain.comment.reply.dto.request.ReplyCreateRequestDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "replies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReplyCommentEntity extends BaseTimeEntity {
    @Id
    @NotNull
    private ObjectId _id;

    @NotNull
    private ObjectId commentId; // 댓글 ID 참조

    @NotNull
    private ObjectId userId; // 작성자 ID

    @NotBlank
    private String content;

    private boolean anonymous;

    @Builder
    public ReplyCommentEntity(ObjectId commentId, ObjectId userId,String content,  boolean anonymous) {
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.anonymous = anonymous;
    }

    public static ReplyCommentEntity create(ObjectId commentId, ObjectId userId, ReplyCreateRequestDTO requestDTO) {
        return new ReplyCommentEntity(
                commentId,
                userId,
                requestDTO.getContent(),
                requestDTO.isAnonymous()
        );
    }

    public void updateReply(String content) {
        this.content = content;
    }

}
