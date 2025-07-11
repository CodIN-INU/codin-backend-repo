package inu.codin.codin.domain.post.domain.reply.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.exception.CommentErrorCode;
import inu.codin.codin.domain.post.domain.comment.exception.CommentException;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.reply.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.domain.reply.dto.request.ReplyUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.domain.reply.exception.ReplyException;
import inu.codin.codin.domain.post.domain.reply.exception.ReplyErrorCode;
import inu.codin.codin.domain.post.dto.UserDto;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.exception.PostErrorCode;
import inu.codin.codin.domain.post.exception.PostException;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.service.PostCommandService;
import inu.codin.codin.domain.post.service.PostQueryService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.service.RedisBestService;
import inu.codin.codin.infra.s3.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplyCommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReplyCommentRepository replyCommentRepository;
    private final UserRepository userRepository;
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    private final LikeService likeService;
    private final NotificationService notificationService;
    private final RedisBestService redisBestService;
    private final S3Service s3Service;

    // 대댓글 추가
    public void addReply(String id, ReplyCreateRequestDTO requestDTO) {
        ObjectId commentId = new ObjectId(id);
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        PostEntity post = postRepository.findByIdAndNotDeleted(comment.getPostId())
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        ObjectId userId = SecurityUtils.getCurrentUserId();


        ReplyCommentEntity reply = ReplyCommentEntity.create(commentId, userId, requestDTO);
        replyCommentRepository.save(reply);

        postCommandService.handleCommentCreation(post, userId);
        redisBestService.applyBestScore(1, post.get_id());

        log.info("대댓글 추가 완료 - replyId: {}, postId: {}, commentCount: {}",
                reply.get_id(), post.get_id(), post.getCommentCount());
        if (!userId.equals(post.getUserId())) notificationService.sendNotificationMessageByReply(post.getPostCategory(), comment.getUserId(), post.get_id().toString(), reply.getContent());
    }

    public void updateReply(String id, @Valid ReplyUpdateRequestDTO requestDTO) {

        ObjectId replyId = new ObjectId(id);
        ReplyCommentEntity reply = replyCommentRepository.findByIdAndNotDeleted(replyId)
                .orElseThrow(() -> new ReplyException(ReplyErrorCode.REPLY_NOT_FOUND));

        reply.updateReply(requestDTO.getContent());
        replyCommentRepository.save(reply);

        log.info("대댓글 수정 완료 - replyId: {}", replyId);

    }

    // 대댓글 삭제 (Soft Delete)
    public void softDeleteReply(String replyId) {
        ReplyCommentEntity reply = replyCommentRepository.findByIdAndNotDeleted(new ObjectId(replyId))
                .orElseThrow(() -> new ReplyException(ReplyErrorCode.REPLY_NOT_FOUND));
        SecurityUtils.validateUser(reply.getUserId());

        ObjectId commentId = reply.getCommentId();
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        ObjectId postId = comment.getPostId();
        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        // 대댓글 삭제
        reply.delete();
        replyCommentRepository.save(reply);

        postCommandService.decreaseCommentCount(post);
        redisBestService.applyBestScore(-1, postId);

        log.info("대댓글 성공적 삭제  replyId: {}", replyId);
    }

    /**
     * 특정 댓글의 대댓글 조회
     */
    public List<CommentResponseDTO> getRepliesByCommentId(PostAnonymous postAnonymous, ObjectId commentId) {
        // 1. 대댓글 목록 조회
        List<ReplyCommentEntity> replies = replyCommentRepository.findByCommentId(commentId);
        if (replies.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 사용자 정보 맵 생성
        Map<ObjectId, UserEntity> userMap = createUserMapFromReplies(replies);
        String defaultImageUrl = s3Service.getDefaultProfileImageUrl();

        // 3. 대댓글 DTO 변환
        return replies.stream()
                .map(reply -> buildReplyResponseDTO(reply, postAnonymous, userMap, defaultImageUrl))
                .toList();
    }

    /**
     * 대댓글 작성자들의 사용자 정보 맵 생성
     */
    private Map<ObjectId, UserEntity> createUserMapFromReplies(List<ReplyCommentEntity> replies) {
        List<ObjectId> userIdsInOrder = replies.stream()
                .map(ReplyCommentEntity::getUserId)
                .toList();

        List<ObjectId> distinctIds = userIdsInOrder.stream()
                .distinct()
                .toList();

        return userRepository.findAllById(distinctIds)
                .stream()
                .collect(Collectors.toMap(
                        UserEntity::get_id,
                        user -> user
                ));
    }

    /**
     * 대댓글 응답 DTO 생성
     */
    private CommentResponseDTO buildReplyResponseDTO(
            ReplyCommentEntity reply,
            PostAnonymous postAnonymous,
            Map<ObjectId, UserEntity> userMap,
            String defaultImageUrl) {

        ObjectId userId = reply.getUserId();
        UserEntity user = userMap.get(userId);
        int anonNum = postQueryService.getUserAnonymousNumber(postAnonymous, reply.getUserId());

        UserDto replyUserDto = UserDto.ofReply(reply, user, anonNum, defaultImageUrl);

        return CommentResponseDTO.replyOf(
                reply,
                replyUserDto,
                likeService.getLikeCount(LikeType.REPLY, reply.get_id()),
                getUserInfoAboutReply(reply.get_id())
        );
    }

    public CommentResponseDTO.UserInfo getUserInfoAboutReply(ObjectId replyId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        return CommentResponseDTO.UserInfo.builder()
                .isLike(likeService.isLiked(LikeType.COMMENT, replyId, userId))
                .build();
    }




}
