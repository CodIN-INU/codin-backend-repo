package inu.codin.codin.domain.post.domain.like.entity;

import inu.codin.codin.common.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "likes")
@Getter
public class LikeEntity extends BaseTimeEntity {
    @Id
    private String id;
    private String likeTypeId; // 게시글, 댓글, 대댓글의 ID
    private LikeType likeType; // 엔티티 타입 (post, comment, reply)
    private String userId; // 좋아요를 누른 사용자 ID

    @Builder
    public LikeEntity(String likeTypeId, LikeType likeType, String userId) {
        this.likeTypeId = likeTypeId;
        this.likeType = likeType;
        this.userId = userId;
    }
}