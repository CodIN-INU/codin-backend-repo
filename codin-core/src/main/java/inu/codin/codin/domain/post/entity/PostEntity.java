package inu.codin.codin.domain.post.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.post.exception.StateUpdateException;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "posts")
@Getter
public class PostEntity extends BaseTimeEntity {
    @Id @NotBlank
    private ObjectId _id;

    private final ObjectId userId; // User 엔티티와의 관계를 유지하기 위한 필드
    private final String title;
    private String content;
    private List<String> postImageUrls;
    private boolean isAnonymous;

    private final PostCategory postCategory; // Enum('구해요', '소통해요', '비교과', ...)
    private PostStatus postStatus; // Enum(ACTIVE, DISABLED, SUSPENDED)

    private int commentCount = 0; // 댓글 + 대댓글 카운트
    private int reportCount = 0; // 신고 카운트

    private PostAnonymous anonymous = new PostAnonymous();

    @Builder
    public PostEntity(ObjectId _id, ObjectId userId, PostCategory postCategory, String title, String content, List<String> postImageUrls ,boolean isAnonymous, PostStatus postStatus) {
        this._id = _id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.postImageUrls = postImageUrls;
        this.isAnonymous = isAnonymous;
        this.postCategory = postCategory;
        this.postStatus = postStatus;
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
    public void plusCommentCount() {
        this.commentCount++;
    }

    //신고 수 업데이트
    public void updateReportCount(int reportCount) {
        this.reportCount=reportCount;
    }

}
