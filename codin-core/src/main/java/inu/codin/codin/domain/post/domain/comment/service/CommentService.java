package inu.codin.codin.domain.post.domain.comment.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentCreateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.reply.service.ReplyCommentService;
import inu.codin.codin.domain.post.dto.response.UserDto;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.service.RedisService;
import inu.codin.codin.infra.s3.S3Service;
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
    private final RedisService redisService;
    private final S3Service s3Service;

    // 댓글 추가
    public void addComment(String id, CommentCreateRequestDTO requestDTO) {
        log.info("댓글 추가 요청. postId: {}, 사용자: {}, 내용: {}", id, SecurityUtils.getCurrentUserId(), requestDTO.getContent());

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
    }

    // 댓글 삭제 (Soft Delete)
    public void softDeleteComment(String id) {
        log.info("댓글 삭제 요청. commentId: {}", id);
        ObjectId commentId = new ObjectId(id);
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
        SecurityUtils.validateUser(comment.getUserId());

//        // 댓글의 대댓글 조회
//        List<ReplyCommentEntity> replies = replyCommentRepository.findByCommentId(commentId);
//        // 대댓글 Soft Delete 처리
//        replies.forEach(reply -> {
//            if (reply.getDeletedAt()!=null) {
//                reply.delete();
//                replyCommentRepository.save(reply);
//            }
//        });

        // 댓글 Soft Delete 처리
        comment.delete();
        commentRepository.save(comment);

//        // 댓글 수 감소 (댓글 + 대댓글 수만큼 감소)
//        PostEntity post = postRepository.findByIdAndNotDeleted(comment.getPostId())
//                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
//        post.updateCommentCount(post.getCommentCount() - (1 + replies.size()));
//        postRepository.save(post);

        log.info("삭제된 commentId: {}", commentId);
    }


    // 특정 게시물의 댓글 및 대댓글 조회
    public List<CommentResponseDTO> getCommentsByPostId(String id) {
        ObjectId postId = new ObjectId(id);
        postRepository.findByIdAndNotDeleted(postId)
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

                    String nickname;
                    String userImageUrl;

                    if (userDto.isDeleted()){
                        nickname = "탈퇴한 사용자";
                        userImageUrl = defaultImageUrl;
                    } else {
                        nickname = comment.isAnonymous()? "익명" : userMap.get(comment.getUserId()).nickname();
                        userImageUrl = comment.isAnonymous()? defaultImageUrl: userMap.get(comment.getUserId()).imageUrl();
                    }
                    return CommentResponseDTO.commentOf(comment, nickname, userImageUrl,
                            replyCommentService.getRepliesByCommentId(comment.get_id()),
                            likeService.getLikeCount(LikeType.valueOf("COMMENT"), comment.get_id()),
                            getUserInfoAboutPost(comment.get_id()));
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

    public UserInfo getUserInfoAboutPost(ObjectId commentId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        return UserInfo.builder()
                .isLike(likeService.isCommentLiked(commentId, userId))
                .build();
    }

}