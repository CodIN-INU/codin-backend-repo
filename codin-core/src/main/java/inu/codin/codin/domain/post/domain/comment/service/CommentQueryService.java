package inu.codin.codin.domain.post.domain.comment.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.exception.CommentErrorCode;
import inu.codin.codin.domain.post.domain.comment.exception.CommentException;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.comment.reply.service.ReplyQueryService;
import inu.codin.codin.domain.post.dto.UserDto;
import inu.codin.codin.domain.post.dto.UserInfo;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.service.PostQueryService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
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
public class CommentQueryService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    private final LikeService likeService;
    private final PostQueryService postQueryService;
    private final S3Service s3Service;
    private final ReplyQueryService replyQueryService;

    /**
     * 특정 게시물의 댓글 및 대댓글 조회
     */
    public List<CommentResponseDTO> getCommentsByPostId(String id) {
        // 1. 입력 검증 및 게시물 조회
        ObjectId postId = ObjectIdUtil.toObjectId(id);
        PostEntity post = postQueryService.findPostById(postId);

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
        UserDto commentUserDto = UserDto.forComment(comment, user, anonNum ,defaultImageUrl);

        return CommentResponseDTO.commentOf(
                comment,
                commentUserDto,
                replyQueryService.getRepliesByCommentId(postAnonymous, comment.get_id()),
                likeService.getLikeCount(LikeType.COMMENT, comment.get_id().toString()),
                getUserInfoAboutComment(comment.get_id())
        );
    }


    public UserInfo getUserInfoAboutComment(ObjectId commentId) {
        ObjectId userId = SecurityUtils.getCurrentUserIdOrNull();

        boolean isLiked = false;
        if (userId != null) {
            isLiked = likeService.isLiked(LikeType.COMMENT, commentId.toString(), userId);
        }

        return UserInfo.ofComment(isLiked);
    }
    /**
     *
     * @param commentId
     * @return validated PostEntity
     */
    public CommentEntity findCommentById(ObjectId commentId) {
        return commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));
    }
}
