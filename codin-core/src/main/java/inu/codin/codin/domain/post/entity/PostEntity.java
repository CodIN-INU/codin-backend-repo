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

    private PostCategory postCategory; // Enum('구해요', '소통해요', '비교과', ...)

    private String title;

    private String content;

    private List<String> postImageUrls;

    private boolean isAnonymous;

    private boolean isDeleted = false;

    private PostStatus postStatus; // Enum(ACTIVE, DISABLED, SUSPENDED)

    private List<CommentEntity> comments = new ArrayList<>();

    private int likeCount = 0; // 좋아요 카운트

    private int scrapCount = 0; // 스크랩 카운트

    @Builder
    public PostEntity(String postId, String userId, PostCategory postCategory, String title, String content, boolean isAnonymous, List<String> postImageUrls, PostStatus postStatus, List<CommentEntity> comments, Integer likeCount, Integer scrapCount) {
        this.postId = postId;
        this.userId = userId;
        this.postCategory = postCategory;
        this.title = title;
        this.content = content;
        this.isAnonymous = isAnonymous;
        this.postImageUrls = postImageUrls;
        this.postStatus = postStatus;
        this.comments = comments != null ? comments : new ArrayList<>();
        this.likeCount = likeCount != null ? likeCount : 0; // 기본값 설정
        this.scrapCount = scrapCount != null ? scrapCount : 0; // 기본값 설정
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

    public void softDeletePost() {
        this.isDeleted = true;
        this.delete();
    }


    // 댓글 추가
    public void addComment(CommentEntity comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>(); // null 방지
        }
        this.comments.add(comment);
    }
    // 댓글 삭제 (Soft Delete)
    public void softDeleteComment(String commentId) {
        this.comments.stream()
                .filter(comment -> comment.getCommentId().equals(commentId))
                .findFirst()
                .ifPresent(CommentEntity::softDelete);
    }

    public void softDeleteReply(String parentCommentId, String replyId) {
        this.comments.stream()
                .filter(comment -> comment.getCommentId().equals(parentCommentId))
                .findFirst()
                .ifPresent(parentComment -> parentComment.softDeleteReply(replyId));
    }

    // 대댓글 추가
    public void addReply(String parentCommentId, CommentEntity reply) {
        this.comments.stream()
                .filter(comment -> comment.getCommentId().equals(parentCommentId))
                .findFirst()
                .ifPresent(parentComment -> parentComment.addReply(reply));
    }

    //좋아요 업데이트
    public void updateLikeCount(int likeCount) {
        this.likeCount=likeCount;
    }
    //스크랩 업데이트
    public void updateScrapCount(int scrapCount) {
        this.scrapCount=likeCount;
    }


}
