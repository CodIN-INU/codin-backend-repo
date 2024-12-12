package inu.codin.codin.domain.post.domain.comment.entity;

import inu.codin.codin.common.BaseTimeEntity;
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

    private int likeCount = 0;  // 좋아요 수 (Redis에서 관리)

    @Builder
    public CommentEntity(ObjectId _id, ObjectId postId, ObjectId userId, String content) {
        this._id = _id;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
    }


    //좋아요 수 업데이트
    public void updateLikeCount(int likeCount) {
        this.likeCount=likeCount;
    }


}
