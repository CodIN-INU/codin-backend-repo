package inu.codin.codin.domain.post.domain.comment.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentCreateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO.UserInfo;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.reply.service.ReplyCommentService;
import inu.codin.codin.domain.post.dto.response.UserDto;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.service.RedisBestService;
import inu.codin.codin.infra.s3.S3Service;
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
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private final UserRepository userRepository;
    private final LikeService likeService;
    private final ReplyCommentService replyCommentService;
    private final NotificationService notificationService;
    private final RedisBestService redisBestService;
    private final S3Service s3Service;

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
        post.plusCommentCount();
        post.getAnonymous().setAnonNumber(post, userId);
        postRepository.save(post);

        redisBestService.applyBestScore(1, postId);
        log.info("댓글 추가완료 postId: {} commentId : {}", postId, comment.get_id());
        if (!userId.equals(post.getUserId())) notificationService.sendNotificationMessageByComment(post.getPostCategory(), post.getUserId(), post.get_id().toString(), comment.getContent());

    }

    // 댓글 삭제 (Soft Delete)
    public void softDeleteComment(String id) {
        ObjectId commentId = new ObjectId(id);
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
        SecurityUtils.validateUser(comment.getUserId());

        // 댓글 Soft Delete 처리
        comment.delete();
        commentRepository.save(comment);

        log.info("삭제된 commentId: {}", commentId);
    }


    // 특정 게시물의 댓글 및 대댓글 조회
    public List<CommentResponseDTO> getCommentsByPostId(String id) {
        ObjectId postId = new ObjectId(id);
        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
        List<CommentEntity> comments = commentRepository.findByPostId(postId);

        String defaultImageUrl = s3Service.getDefaultProfileImageUrl();

        Map<ObjectId, UserDto> userMap = userRepository.findAllById(
                        comments.stream()
                                .map(CommentEntity::getUserId)
                                .distinct()
                                .toList()
                ).stream()
                .collect(Collectors.toMap(
                        UserEntity::get_id,
                        user -> new UserDto(user.getNickname(), user.getProfileImageUrl(), user.getDeletedAt() != null)
                ));


        return comments.stream()
                .map(comment -> {
                    UserDto userDto = userMap.get(comment.getUserId());
                    int anonNum = post.getAnonymous().getAnonNumber(comment.getUserId().toString());
                    String nickname;
                    String userImageUrl;

                    if (userDto.isDeleted()){
                        nickname = userMap.get(comment.getUserId()).nickname();
                        userImageUrl = userMap.get(comment.getUserId()).imageUrl();
                    } else {
                        nickname = comment.isAnonymous()?
                                anonNum==0? "글쓴이" : "익명" + anonNum
                                : userMap.get(comment.getUserId()).nickname();
                        userImageUrl = comment.isAnonymous()? defaultImageUrl: userMap.get(comment.getUserId()).imageUrl();
                    }
                    return CommentResponseDTO.commentOf(comment, nickname, userImageUrl,
                            replyCommentService.getRepliesByCommentId(post.getAnonymous(), comment.get_id()),
                            likeService.getLikeCount(LikeType.COMMENT, comment.get_id()),
                            getUserInfoAboutComment(comment.get_id()));
                })
                .toList();
    }

    public void updateComment(String id, CommentUpdateRequestDTO requestDTO) {
        log.info("댓글 업데이트 요청. commentId: {}, 새로운 내용: {}", id, requestDTO.getContent());

        ObjectId commentId = new ObjectId(id);
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        comment.updateComment(requestDTO.getContent());
        commentRepository.save(comment);

        log.info("댓글 업데이트 완료. commentId: {}", commentId);

    }

    public UserInfo getUserInfoAboutComment(ObjectId commentId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        return UserInfo.builder()
                .isLike(likeService.isLiked(LikeType.COMMENT, commentId, userId))
                .build();
    }


}