package inu.codin.codin.domain.post.domain.comment.reply.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.comment.reply.exception.ReplyErrorCode;
import inu.codin.codin.domain.post.domain.comment.reply.exception.ReplyException;
import inu.codin.codin.domain.post.domain.comment.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.dto.UserDto;
import inu.codin.codin.domain.post.dto.UserInfo;
import inu.codin.codin.domain.post.entity.PostAnonymous;
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
public class ReplyQueryService {

    private final ReplyCommentRepository replyCommentRepository;
    private final UserRepository userRepository;
    private final PostQueryService postQueryService;

    private final LikeService likeService;
    private final S3Service s3Service;

    /**
     * Query Method
     */

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

    public UserInfo getUserInfoAboutReply(ObjectId replyId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        return UserInfo.ofComment(
                likeService.isLiked(LikeType.COMMENT, replyId, userId)
        );
    }

    /**
     *
     * @param replyId
     * @return validated PostEntity
     */
    public ReplyCommentEntity findReplyById(ObjectId replyId) {
        return replyCommentRepository.findByIdAndNotDeleted(replyId)
                .orElseThrow(() -> new ReplyException(ReplyErrorCode.REPLY_NOT_FOUND));
    }

}
