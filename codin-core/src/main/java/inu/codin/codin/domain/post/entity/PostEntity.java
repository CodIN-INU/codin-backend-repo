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

    public void updatePostContent(String content, List<String> postImageUrls) {
        this.content = content;
        this.postImageUrls = postImageUrls;
    }

    public void updatePostStatus(PostStatus postStatus) {
        if (this.postStatus == postStatus) {
            throw new IllegalStateException("현재 상태와 동일한 상태로 변경할 수 없습니다.");
        }
        this.postStatus = postStatus;
    }
    public void removePostImage(String imageUrl) {
        this.postImageUrls.remove(imageUrl);
    }

    public void removeAllPostImages() {
        this.postImageUrls.clear();
    }
}
