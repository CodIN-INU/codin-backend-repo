package inu.codin.codin.domain.post.entity;

import inu.codin.codin.common.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "post")
@Getter
public class PostEntity extends BaseTimeEntity {
    @Id
    private String postId;

    private String userId; // User 엔티티와의 관계를 유지하기 위한 필드

    private PostCategory postCategory; // Enum('구해요', '소통해요', '비교과', ...)

    private String title;

    private String content;

    private String postImageUrl;

    private boolean isAnonymous;

    private PostStatus postStatus; // Enum(ACTIVE, DISABLED, SUSPENDED)


    @Builder
    public PostEntity(String postId, String userId, PostCategory postCategory, String title, String content, boolean isAnonymous, String postImageUrl, PostStatus postStatus) {
        this.postId = postId;
        this.userId = userId;
        this.postCategory = postCategory;
        this.title = title;
        this.content = content;
        this.isAnonymous = isAnonymous;
        this.postImageUrl = postImageUrl;
        this.postStatus = postStatus;
    }
}
