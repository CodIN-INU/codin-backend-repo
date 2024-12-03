package inu.codin.codin.domain.post.domain.reply.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.comment.dto.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.reply.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplyCommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReplyCommentRepository replyCommentRepository;

    private final LikeService likeService;

    // 대댓글 추가
    public void addReply(String commentId, ReplyCreateRequestDTO requestDTO) {
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
        PostEntity post = postRepository.findByIdAndNotDeleted(comment.getPostId())
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));

        String userId = SecurityUtils.getCurrentUserId();

        ReplyCommentEntity reply = ReplyCommentEntity.builder()
                .commentId(commentId)
                .userId(userId)
                .content(requestDTO.getContent())
                .build();

        replyCommentRepository.save(reply);

        // 댓글 수 증가 (대댓글도 댓글 수에 포함)
        log.info("대댓글 추가전, commentCount: {}", post.getCommentCount());
        post.updateCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        log.info("대댓글 추가후, commentCount: {}", post.getCommentCount());
    }

    // 대댓글 삭제 (Soft Delete)
    public void softDeleteReply(String replyId) {
        ReplyCommentEntity reply = replyCommentRepository.findByIdAndNotDeleted(replyId)
                .orElseThrow(() -> new NotFoundException("대댓글을 찾을 수 없습니다."));
        // 대댓글 삭제
        reply.delete();
        replyCommentRepository.save(reply);

        // 댓글 수 감소 (대댓글도 댓글 수에서 감소)
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(reply.getCommentId())
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        PostEntity post = postRepository.findByIdAndNotDeleted(comment.getPostId())
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
        post.updateCommentCount(post.getCommentCount() - 1);
        postRepository.save(post);

        log.info("대댓글 성공적 삭제  replyId: {} , postId: {}.", replyId, post.getPostId());
    }

    // 특정 댓글의 대댓글 조회
    public List<CommentResponseDTO> getRepliesByCommentId(String commentId) {
        List<ReplyCommentEntity> replies = replyCommentRepository.findByCommentIdAndNotDeleted(commentId);

        return replies.stream()
                .map(reply -> {
                    boolean isDeleted = reply.getDeletedAt() != null;
                    return new CommentResponseDTO(
                            reply.getCommentId(),
                            reply.getUserId(),
                            reply.getContent(),
                            List.of(), //대댓글은 대댓글이 없음
                            likeService.getLikeCount(LikeType.valueOf("REPLY"), reply.getCommentId()), // 대댓글 좋아요 수
                            isDeleted);
                }).toList();
    }


}
