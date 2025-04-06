package inu.codin.codin.domain.like.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LikeEntity extends BaseTimeEntity {
    @Id @NotNull
    private ObjectId _id;
    @NotNull
    private ObjectId likeTypeId; // 게시글, 댓글, 대댓글의 ID

    @NotNull
    private LikeType likeType; // 엔티티 타입 (post, comment, reply)

    @NotNull
    private ObjectId userId; // 좋아요를 누른 사용자 ID

    @Builder
    public LikeEntity(ObjectId likeTypeId, LikeType likeType, ObjectId userId) {
        this.likeTypeId = likeTypeId;
        this.likeType = likeType;
        this.userId = userId;
    }
}