package inu.codin.codin.domain.post.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.post.exception.StateUpdateException;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "posts")
@Getter
public class PostEntity extends BaseTimeEntity {
    @Id @NotBlank
    private ObjectId _id;

    private ObjectId userId; // User 엔티티와의 관계를 유지하기 위한 필드
    private String title;
    private String content;
    private List<String> postImageUrls = new ArrayList<>();
    private boolean isAnonymous;

    private PostCategory postCategory; // Enum('구해요', '소통해요', '비교과', ...)
    private PostStatus postStatus; // Enum(ACTIVE, DISABLED, SUSPENDED)

    private int commentCount = 0; // 댓글 + 대댓글 카운트
    private int likeCount = 0; // 좋아요 카운트 (redis)
    private int scrapCount = 0; // 스크랩 카운트 (redis)
    private int hitCount = 0;

    private Integer reportCount = 0; // 신고 카운트

    @Builder
    public PostEntity(ObjectId _id, ObjectId userId, PostCategory postCategory, String title, String content, List<String> postImageUrls ,boolean isAnonymous, PostStatus postStatus,
                      int commentCount, int likeCount, int scrapCount, Integer reportCount) {
        this._id = _id;
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
        this.reportCount = (reportCount == null) ? 0 : reportCount;
    }

    public void updatePostContent(String content, List<String> postImageUrls) {
        this.content = content;
        this.postImageUrls = postImageUrls;
    }

    public void updatePostAnonymous(boolean isAnonymous) {
        if (this.isAnonymous == isAnonymous) {
            throw new StateUpdateException("현재 상태와 동일한 상태로 변경할 수 없습니다.");
        }
        this.isAnonymous = isAnonymous;
    }

    public void updatePostStatus(PostStatus postStatus) {
        if (this.postStatus == postStatus) {
            throw new StateUpdateException("현재 상태와 동일한 상태로 변경할 수 없습니다.");
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

    //신고 수 업데이트
    public void updateReportCount(int reportCount) {
        this.reportCount=reportCount;
    }

    public void updateHitCount(int hitCount) {this.hitCount = hitCount; }


}
