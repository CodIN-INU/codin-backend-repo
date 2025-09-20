package inu.codin.codin.domain.post.security;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.service.PostQueryService;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.service.CommentQueryService;
import inu.codin.codin.domain.post.domain.comment.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.comment.reply.service.ReplyQueryService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OwnershipPolicy {

    private final PostQueryService postQueryService;
    private final CommentQueryService commentQueryService;
    private final ReplyQueryService replyQueryService;

    /** 존재 + 소유자 검증 후 엔티티 반환 (실패 시 예외) */
    public PostEntity assertPostOwner(ObjectId postId) {
        PostEntity post = postQueryService.findPostById(postId);
        validateOwner(post.getUserId());
        return post;
    }

    public CommentEntity assertCommentOwner(ObjectId commentId) {
        CommentEntity comment = commentQueryService.findCommentById(commentId);
        validateOwner(comment.getUserId());
        return comment;
    }

    public ReplyCommentEntity assertReplyOwner(ObjectId replyId) {
        ReplyCommentEntity reply = replyQueryService.findReplyById(replyId);
        validateOwner(reply.getUserId());
        return reply;
    }

    private void validateOwner(ObjectId ownerId) {
        ObjectId current = SecurityUtils.getCurrentUserId();
        SecurityUtils.validateOwners(current, ownerId); // 불일치 시 예외
    }
}