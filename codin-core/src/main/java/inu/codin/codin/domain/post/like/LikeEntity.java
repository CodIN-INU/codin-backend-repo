package inu.codin.codin.domain.post.like;

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
    private String postId;
    private String userId;

    @Builder
    public LikeEntity(String postId, String userId) {
        this.postId = postId;
        this.userId = userId;
    }
}