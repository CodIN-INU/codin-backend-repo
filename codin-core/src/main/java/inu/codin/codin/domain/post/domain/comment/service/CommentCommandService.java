package inu.codin.codin.domain.post.domain.comment.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentCreateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.domain.best.BestService;
import inu.codin.codin.domain.post.service.PostCommandService;
import inu.codin.codin.domain.post.service.PostQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentCommandService {
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final CommentQueryService commentQueryService;
    private final BestService bestService;

    // 댓글 추가
    public void addComment(String id, CommentCreateRequestDTO requestDTO) {

        ObjectId postId = ObjectIdUtil.toObjectId(id);
        PostEntity post = postQueryService.findPostById(postId);

        ObjectId userId = SecurityUtils.getCurrentUserId();

        CommentEntity comment = CommentEntity.create(postId, userId, requestDTO);
        commentRepository.save(comment);

        postCommandService.handleCommentCreation(post, userId);
        bestService.applyBestScore(postId);

        log.info("댓글 추가완료 postId: {} commentId : {}", postId, comment.get_id());
        if (!userId.equals(post.getUserId())) notificationService.sendNotificationMessageByComment(post.getPostCategory(), post.getUserId(), post.get_id().toString(), comment.getContent());

    }

    public void updateComment(String id, CommentUpdateRequestDTO requestDTO) {
        log.info("댓글 업데이트 요청. commentId: {}, 새로운 내용: {}", id, requestDTO.getContent());

        ObjectId commentId = ObjectIdUtil.toObjectId(id);
        CommentEntity comment = commentQueryService.findCommentById(commentId);

        assertOwner(comment.getUserId());

        comment.updateComment(requestDTO.getContent());
        commentRepository.save(comment);

        log.info("댓글 업데이트 완료. commentId: {}", commentId);

    }

    // 댓글 삭제 (Soft Delete)
    public void softDeleteComment(String id) {
        ObjectId commentId = ObjectIdUtil.toObjectId(id);
        CommentEntity comment = commentQueryService.findCommentById(commentId);

        assertOwner(comment.getUserId());


        ObjectId postId = comment.getPostId();
        PostEntity post = postQueryService.findPostById(postId);

        // 댓글 Soft Delete 처리
        comment.delete();
        commentRepository.save(comment);

        postCommandService.decreaseCommentCount(post);
//        bestService.applyBestScore( postId);

        log.info("삭제된 commentId: {}", commentId);
    }

    private void assertOwner(ObjectId ownerId) {
        ObjectId currentUserId = SecurityUtils.getCurrentUserId();
        SecurityUtils.validateOwners(currentUserId, ownerId);
    }
}
