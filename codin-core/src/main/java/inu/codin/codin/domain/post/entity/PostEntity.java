package inu.codin.codin.domain.post.entity;

import inu.codin.codin.common.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "post")
@Getter
public class PostEntity extends BaseTimeEntity {
    @Id @NotBlank
    private String id;
    private String postId;

    private String userId; // User 엔티티와의 관계를 유지하기 위한 필드

    private PostCategory postCategory; // Enum('구해요', '소통해요', '비교과', ...)

    private String title;

    private String content;

    private List<String> postImageUrls;

    private boolean isAnonymous;

    private PostStatus postStatus; // Enum(ACTIVE, DISABLED, SUSPENDED)


    @Builder
    public PostEntity(String postId, String userId, PostCategory postCategory, String title, String content, boolean isAnonymous, List<String> postImageUrls, PostStatus postStatus) {
        this.postId = postId;
        this.userId = userId;
        this.postCategory = postCategory;
        this.title = title;
        this.content = content;
        this.isAnonymous = isAnonymous;
        this.postImageUrls = postImageUrls;
        this.postStatus = postStatus;
    }
}
