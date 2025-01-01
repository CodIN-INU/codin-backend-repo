package inu.codin.codin.domain.post.domain.comment.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentCreateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.reply.service.ReplyCommentService;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO.UserInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyCommentRepository replyCommentRepository;

    private final UserRepository userRepository;
    private final LikeService likeService;
    private final ReplyCommentService replyCommentService;
    private final NotificationService notificationService;
    private final RedisService redisService;

    // 댓글 추가
    public void addComment(String id, CommentCreateRequestDTO requestDTO) {
        ObjectId postId = new ObjectId(id);
        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));

        ObjectId userId = SecurityUtils.getCurrentUserId();
        CommentEntity comment = CommentEntity.builder()
                .postId(postId)
                .userId(userId)
                .anonymous(requestDTO.isAnonymous())
                .content(requestDTO.getContent())
                .build();
        commentRepository.save(comment);

        // 댓글 수 증가
        post.updateCommentCount(post.getCommentCount() + 1);
        redisService.applyBestScore(1, postId);
        postRepository.save(post);
        log.info("댓글 추가완료 postId: {}.", postId);
        notificationService.sendNotificationMessageByComment(post.getUserId(), comment.getContent());
    }

    // 댓글 삭제 (Soft Delete)
    public void softDeleteComment(String id) {
        ObjectId commentId = new ObjectId(id);
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
        SecurityUtils.validateUser(comment.getUserId());

        // 댓글의 대댓글 조회
        List<ReplyCommentEntity> replies = replyCommentRepository.findByCommentIdAndNotDeleted(commentId);
        // 대댓글 Soft Delete 처리
        replies.forEach(reply -> {
            if (reply.getDeletedAt()!=null) {
                reply.delete();
                replyCommentRepository.save(reply);
            }
        });

        // 댓글 Soft Delete 처리
        comment.delete();
        commentRepository.save(comment);

        // 댓글 수 감소 (댓글 + 대댓글 수만큼 감소)
        PostEntity post = postRepository.findByIdAndNotDeleted(comment.getPostId())
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
        post.updateCommentCount(post.getCommentCount() - (1 + replies.size()));
        postRepository.save(post);

        log.info("삭제된 commentId: {} , 대댓글 {} . 총 삭제 수: {} postId: {}",
                commentId, replies.size(), (1 + replies.size()), post.get_id());
    }


    // 특정 게시물의 댓글 및 대댓글 조회
    public List<CommentResponseDTO> getCommentsByPostId(String id) {
        ObjectId postId = new ObjectId(id);
        postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
        List<CommentEntity> comments = commentRepository.findByPostId(postId);

        Map<ObjectId, String> userNicknameMap = userRepository.findAllById(
                comments.stream().map(CommentEntity::getUserId).distinct().toList()
        ).stream().collect(Collectors.toMap(UserEntity::get_id, UserEntity::getNickname));

        return comments.stream()
                .map(comment -> {
                    String nickname = comment.isAnonymous() ? "익명" : userNicknameMap.get(comment.getUserId());
                    boolean isDeleted = comment.getDeletedAt() != null;
                    return new CommentResponseDTO(
                            comment.get_id().toString(),
                            comment.getUserId().toString(),
                            comment.getContent(),
                            nickname,
                            comment.isAnonymous(),
                            replyCommentService.getRepliesByCommentId(comment.get_id()), // 대댓글 조회
                            likeService.getLikeCount(LikeType.valueOf("COMMENT"), comment.get_id()), // 댓글 좋아요 수
                            isDeleted,
                            comment.getCreatedAt(),
                            getUserInfoAboutPost(comment.get_id())
                    );


                    })
                .toList();
    }

    public void updateComment(String id, CommentUpdateRequestDTO requestDTO) {

        ObjectId commentId = new ObjectId(id);
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        comment.updateComment(requestDTO.getContent());
        commentRepository.save(comment);
    }

    public UserInfo getUserInfoAboutPost(ObjectId commentId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        return UserInfo.builder()
                .isLike(redisService.isCommentLiked(commentId, userId))
                .build();
    }

}