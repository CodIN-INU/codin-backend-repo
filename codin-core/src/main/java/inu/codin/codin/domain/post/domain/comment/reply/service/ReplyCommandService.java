package inu.codin.codin.domain.post.domain.comment.reply.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.best.BestService;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.service.CommentQueryService;
import inu.codin.codin.domain.post.domain.comment.reply.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.reply.dto.request.ReplyUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.comment.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.security.OwnershipPolicy;
import inu.codin.codin.domain.post.service.PostCommandService;
import inu.codin.codin.domain.post.service.PostQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplyCommandService {

    private final ReplyCommentRepository replyCommentRepository;
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    private final NotificationService notificationService;
    private final BestService bestService;
    private final CommentQueryService commentQueryService;
    private final ReplyQueryService replyQueryService;
    private final OwnershipPolicy ownershipPolicy;

    /**
     *Command Method
     */
    // 대댓글 추가
    public void addReply(String id, ReplyCreateRequestDTO requestDTO) {

        CommentEntity comment = commentQueryService.findCommentById(ObjectIdUtil.toObjectId(id));
        PostEntity post = postQueryService.findPostById(comment.getPostId());
        ObjectId userId = SecurityUtils.getCurrentUserId();


        ReplyCommentEntity reply = ReplyCommentEntity.create(comment.get_id(), userId, requestDTO);
        replyCommentRepository.save(reply);

        postCommandService.handleCommentCreation(post, userId);
        bestService.applyBestScore(post.get_id());

        log.info("대댓글 추가 완료 - replyId: {}, postId: {}, commentCount: {}",
                reply.get_id(), post.get_id(), post.getCommentCount());
        if (!userId.equals(post.getUserId())) notificationService.sendNotificationMessageByReply(post.getPostCategory(), post.getUserId(), post.get_id().toString(), reply.getContent());
    }

    public void updateReply(String replyId, @Valid ReplyUpdateRequestDTO requestDTO) {
        ReplyCommentEntity reply = ownershipPolicy.assertReplyOwner(ObjectIdUtil.toObjectId(replyId));

        reply.updateReply(requestDTO.getContent());
        replyCommentRepository.save(reply);

        log.info("대댓글 수정 완료 - replyId: {}", reply.get_id());

    }

    // 대댓글 삭제 (Soft Delete)
    public void softDeleteReply(String replyId) {
        ReplyCommentEntity reply = ownershipPolicy.assertReplyOwner(ObjectIdUtil.toObjectId(replyId));

        CommentEntity comment = commentQueryService.findCommentById(reply.getCommentId());

        PostEntity post = postQueryService.findPostById(comment.getPostId());

        // 대댓글 삭제
        reply.delete();
        replyCommentRepository.save(reply);

        postCommandService.decreaseCommentCount(post);
//        bestService.applyBestScore(post.get_id());

        log.info("대댓글 성공적 삭제  replyId: {}", reply.get_id());
    }
}
