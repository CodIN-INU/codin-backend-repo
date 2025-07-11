package inu.codin.codin.domain.post.domain.comment.service;

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
import inu.codin.codin.domain.post.domain.comment.exception.CommentException;
import inu.codin.codin.domain.post.domain.comment.exception.CommentErrorCode;
import inu.codin.codin.domain.post.domain.reply.service.ReplyCommentService;
import inu.codin.codin.domain.post.dto.UserDto;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.service.PostCommandService;
import inu.codin.codin.domain.post.service.PostQueryService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.service.RedisBestService;
import inu.codin.codin.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;


    private final S3Service s3Service;

    // 댓글 추가
    public void addComment(String id, CommentCreateRequestDTO requestDTO) {

        ObjectId postId = new ObjectId(id);
        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.POST_NOT_FOUND));

        ObjectId userId = SecurityUtils.getCurrentUserId();

        CommentEntity comment = CommentEntity.create(postId, userId, requestDTO);
        commentRepository.save(comment);

        postCommandService.handleCommentCreation(post, userId);
        redisBestService.applyBestScore(1, postId);

        log.info("댓글 추가완료 postId: {} commentId : {}", postId, comment.get_id());
        if (!userId.equals(post.getUserId())) notificationService.sendNotificationMessageByComment(post.getPostCategory(), post.getUserId(), post.get_id().toString(), comment.getContent());

    }

    // 댓글 삭제 (Soft Delete)
    public void softDeleteComment(String id) {
        ObjectId commentId = new ObjectId(id);
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
        SecurityUtils.validateUser(comment.getUserId());

        ObjectId postId = comment.getPostId();
        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.POST_NOT_FOUND));

        // 댓글 Soft Delete 처리
        comment.delete();
        commentRepository.save(comment);

        postCommandService.decreaseCommentCount(post);
        redisBestService.applyBestScore(-1, postId);

        log.info("삭제된 commentId: {}", commentId);
    }


    /**
     * 특정 게시물의 댓글 및 대댓글 조회
     */
    public List<CommentResponseDTO> getCommentsByPostId(String id) {
        // 1. 입력 검증 및 게시물 조회
        ObjectId postId = validateAndConvertPostId(id);
        PostEntity post = findPostById(postId);

        // 2. 댓글 목록 조회
        List<CommentEntity> comments = commentRepository.findByPostId(postId);
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 사용자 정보 맵 생성
        Map<ObjectId, UserEntity> userMap = createUserMap(comments);
        String defaultImageUrl = s3Service.getDefaultProfileImageUrl();

        // 4. 댓글 DTO 변환
        return comments.stream()
                .map(comment -> buildCommentResponseDTO(post.getAnonymous(), comment, userMap, defaultImageUrl))
                .collect(Collectors.toList());
    }

    /**
     * 게시물 ID 검증 및 ObjectId 변환
     */
    private ObjectId validateAndConvertPostId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("게시물 ID는 필수입니다.");
        }return new ObjectId(id);
    }

    private PostEntity findPostById(ObjectId postId) {
        return postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.POST_NOT_FOUND));
    }
    /**
     * 댓글 작성자들의 사용자 정보 맵 생성
     */
    private Map<ObjectId, UserEntity> createUserMap(List<CommentEntity> comments) {
        List<ObjectId> userIdsInOrder = comments.stream()
                .map(CommentEntity::getUserId)
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
     * 댓글 응답 DTO 생성
     */
    private CommentResponseDTO buildCommentResponseDTO(
            PostAnonymous postAnonymous,
            CommentEntity comment,
            Map<ObjectId, UserEntity> userMap,
            String defaultImageUrl) {

        int anonNum = postQueryService.getUserAnonymousNumber(postAnonymous, comment.getUserId());

        UserEntity user = userMap.get(comment.getUserId());

        // 댓글용 사용자 DTO 생성
        UserDto commentUserDto = UserDto.ofComment(comment, user, anonNum ,defaultImageUrl);

        return CommentResponseDTO.commentOf(
                comment,
                commentUserDto,
                replyCommentService.getRepliesByCommentId(postAnonymous, comment.get_id()),
                likeService.getLikeCount(LikeType.COMMENT, comment.get_id()),
                getUserInfoAboutComment(comment.get_id())
        );
    }

    public void updateComment(String id, CommentUpdateRequestDTO requestDTO) {
        log.info("댓글 업데이트 요청. commentId: {}, 새로운 내용: {}", id, requestDTO.getContent());

        ObjectId commentId = new ObjectId(id);
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        //본인 댓글만 수정 가능
        ObjectId userId = SecurityUtils.getCurrentUserId();
        SecurityUtils.validateUser(userId);


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