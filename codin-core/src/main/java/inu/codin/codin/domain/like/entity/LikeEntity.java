package inu.codin.codin.domain.like.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "likes")
@Getter
public class LikeEntity extends BaseTimeEntity {
    @Id
    private ObjectId _id;
    private String likeTypeId; // 게시글, 댓글, 대댓글의 ID
    private LikeType likeType; // 엔티티 타입 (post, comment, reply)
    private ObjectId userId; // 좋아요를 누른 사용자 ID

    @Builder
    public LikeEntity(String likeTypeId, LikeType likeType, ObjectId userId) {
        this.likeTypeId = likeTypeId;
        this.likeType = likeType;
        this.userId = userId;
    }
}