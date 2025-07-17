package inu.codin.codin.domain.post.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.post.dto.request.PostCreateRequestDTO;
import inu.codin.codin.domain.post.exception.PostErrorCode;
import inu.codin.codin.domain.post.exception.PostException;
import inu.codin.codin.domain.post.schedular.exception.SchedulerErrorCode;
import inu.codin.codin.domain.post.schedular.exception.SchedulerException;
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
    public PostEntity(ObjectId userId, PostCategory postCategory, String title, String content, List<String> postImageUrls ,boolean isAnonymous, PostStatus postStatus) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.postImageUrls = postImageUrls != null ? new ArrayList<>(postImageUrls) : new ArrayList<>();
        this.isAnonymous = isAnonymous;
        this.postCategory = postCategory;
        this.postStatus = postStatus;
    }

    public static PostEntity create(ObjectId userId, PostCreateRequestDTO dto, List<String> imageUrls) {
        return new PostEntity(
                userId,
                dto.getPostCategory(),
                dto.getTitle(),
                dto.getContent(),
                imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>(),
                dto.isAnonymous(),
                PostStatus.ACTIVE
        );
    }

    public void updatePostContent(String content, List<String> postImageUrls) {
        this.content = content;
        this.postImageUrls = postImageUrls != null ? new ArrayList<>(postImageUrls) : new ArrayList<>();
    }

    public void updatePostAnonymous(boolean isAnonymous) {
        if (this.isAnonymous == isAnonymous) {
            throw new PostException(PostErrorCode.DUPLICATE_ANONYMOUS_STATE);
        }
        this.isAnonymous = isAnonymous;
    }

    public void updatePostStatus(PostStatus postStatus) {
        if (this.postStatus == postStatus) {
            throw new SchedulerException(SchedulerErrorCode.DUPLICATE_POST_STATUS);
        }
        this.postStatus = postStatus;
    }
    public void removePostImage(String imageUrl) {
        this.postImageUrls.remove(imageUrl);
    }

    //댓글+대댓글 수 증가
    public void plusCommentCount() {
        this.commentCount++;
    }

    //댓글+대댓글 수 감소
    public void minusCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    //신고 수 업데이트
    public void updateReportCount(int reportCount) {
        this.reportCount=reportCount;
    }

    //작성자 확인 로직
    public boolean isWriter(ObjectId userId) {
        return this.userId.equals(userId);
    }

}
