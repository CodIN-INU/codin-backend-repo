package inu.codin.codin.domain.post.entity;

import inu.codin.codin.common.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "post")
@Getter
public class PostEntity extends BaseTimeEntity {
    @Id @NotBlank
    private String postId;

    private String userId; // User 엔티티와의 관계를 유지하기 위한 필드
    private String title;
    private String content;
    private List<String> postImageUrls = new ArrayList<>();
    private boolean isAnonymous;

    private PostCategory postCategory; // Enum('구해요', '소통해요', '비교과', ...)
    private PostStatus postStatus; // Enum(ACTIVE, DISABLED, SUSPENDED)

    private int commentCount = 0; // 댓글 + 대댓글 카운트
    private int likeCount = 0; // 좋아요 카운트 (redis)
    private int scrapCount = 0; // 스크랩 카운트 (redis)

    @Builder
    public PostEntity(String postId, String userId, PostCategory postCategory, String title, String content, List<String> postImageUrls ,boolean isAnonymous, PostStatus postStatus, int commentCount, int likeCount, int scrapCount) {
        this.postId = postId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.postImageUrls = postImageUrls;
        this.isAnonymous = isAnonymous;
        this.postCategory = postCategory;
        this.postStatus = postStatus;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
    }

    public void updatePostContent(String content, List<String> postImageUrls) {
        this.content = content;
        this.postImageUrls = postImageUrls;
    }

    public void updatePostAnonymous(boolean isAnonymous) {
        if (this.isAnonymous == isAnonymous) {
            throw new IllegalStateException("현재 상태와 동일한 상태로 변경할 수 없습니다.");
        }
        this.isAnonymous = isAnonymous;
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

    //댓글+대댓글 수 업데이트
    public void updateCommentCount(int commentCount) {
        this.commentCount=commentCount;
    }
    //좋아요 수 업데이트
    public void updateLikeCount(int likeCount) {
        this.likeCount=likeCount;
    }
    //스크랩 수 업데이트
    public void updateScrapCount(int scrapCount) {
        this.scrapCount=scrapCount;
    }


}
