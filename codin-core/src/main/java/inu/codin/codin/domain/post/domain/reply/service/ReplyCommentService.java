package inu.codin.codin.domain.post.domain.reply.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.reply.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.domain.reply.dto.request.ReplyUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.dto.response.UserDto;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.report.repository.ReportRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.service.RedisBestService;
import inu.codin.codin.infra.s3.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplyCommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReplyCommentRepository replyCommentRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    private final LikeService likeService;
    private final NotificationService notificationService;
    private final RedisBestService redisBestService;
    private final S3Service s3Service;

    // 대댓글 추가
    public void addReply(String id, ReplyCreateRequestDTO requestDTO) {
        ObjectId commentId = new ObjectId(id);
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
        PostEntity post = postRepository.findByIdAndNotDeleted(comment.getPostId())
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));

        ObjectId userId = SecurityUtils.getCurrentUserId();

        ReplyCommentEntity reply = ReplyCommentEntity.builder()
                .commentId(commentId)
                .userId(userId)
                .content(requestDTO.getContent())
                .anonymous(requestDTO.isAnonymous())
                .build();

        replyCommentRepository.save(reply);

        // 댓글 수 증가 (대댓글도 댓글 수에 포함)
        post.plusCommentCount();
        post.getAnonymous().setAnonNumber(post, userId);
        postRepository.save(post);

        redisBestService.applyBestScore(1, post.get_id());
        log.info("대댓글 추가 완료 - replyId: {}, postId: {}, commentCount: {}",
                reply.get_id(), post.get_id(), post.getCommentCount());
        if (!userId.equals(post.getUserId())) notificationService.sendNotificationMessageByReply(post.getPostCategory(), comment.getUserId(), post.get_id().toString(), reply.getContent());
    }

    // 대댓글 삭제 (Soft Delete)
    public void softDeleteReply(String replyId) {
        ReplyCommentEntity reply = replyCommentRepository.findByIdAndNotDeleted(new ObjectId(replyId))
                .orElseThrow(() -> new NotFoundException("대댓글을 찾을 수 없습니다."));
        SecurityUtils.validateUser(reply.getUserId());
        // 대댓글 삭제
        reply.delete();
        replyCommentRepository.save(reply);

        log.info("대댓글 성공적 삭제  replyId: {}", replyId);
    }

    // 특정 댓글의 대댓글 조회
    public List<CommentResponseDTO> getRepliesByCommentId(PostAnonymous postAnonymous, ObjectId commentId) {
        List<ReplyCommentEntity> replies = replyCommentRepository.findByCommentId(commentId);
        String defaultImageUrl = s3Service.getDefaultProfileImageUrl();

        Map<ObjectId, UserDto> userMap = userRepository.findAllById(
                replies.stream()
                        .map(ReplyCommentEntity::getUserId).distinct().toList()
        ).stream()
            .collect(Collectors.toMap(
                    UserEntity::get_id,
                    user -> new UserDto(user.getNickname(), user.getProfileImageUrl(), user.getDeletedAt() != null)
            ));

        return replies.stream()
                .map(reply -> {
                    UserDto userDto = userMap.get(reply.getUserId());
                    int anonNum = postAnonymous.getAnonNumber(reply.getUserId().toString());
                    String nickname;
                    String userImageUrl;

                    if (userDto.isDeleted()){
                        nickname = userMap.get(reply.getUserId()).nickname();
                        userImageUrl = userMap.get(reply.getUserId()).imageUrl();
                    } else {
                        nickname = reply.isAnonymous()?
                                anonNum==0? "글쓴이" : "익명"+anonNum
                                        : userMap.get(reply.getUserId()).nickname();
                        userImageUrl = reply.isAnonymous()? defaultImageUrl: userMap.get(reply.getUserId()).imageUrl();
                    }
                    return CommentResponseDTO.replyOf(reply, nickname, userImageUrl, List.of(),
                            likeService.getLikeCount(LikeType.REPLY, reply.get_id()), // 대댓글 좋아요 수
                            getUserInfoAboutReply(reply.get_id()));
                }).toList();
    }

    public CommentResponseDTO.UserInfo getUserInfoAboutReply(ObjectId replyId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        //log.info("대댓글 userInfo - replyId: {}, userId: {}", replyId, userId);
        return CommentResponseDTO.UserInfo.builder()
                .isLike(likeService.isLiked(LikeType.REPLY, replyId, userId))
                .build();
    }


    public void updateReply(String id, @Valid ReplyUpdateRequestDTO requestDTO) {

        ObjectId replyId = new ObjectId(id);
        ReplyCommentEntity reply = replyCommentRepository.findByIdAndNotDeleted(replyId)
                .orElseThrow(() -> new NotFoundException("대댓글을 찾을 수 없습니다."));

        reply.updateReply(requestDTO.getContent());
        replyCommentRepository.save(reply);

        log.info("대댓글 수정 완료 - replyId: {}", replyId);

    }

}
