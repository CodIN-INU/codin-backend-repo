package inu.codin.codin.domain.post.domain.comment.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.comment.dto.CommentCreateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.dto.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.reply.service.ReplyCommentService;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyCommentRepository replyCommentRepository;

    private final LikeService likeService;
    private final ReplyCommentService replyCommentService;

    // 댓글 추가
    public void addComment(String postId, CommentCreateRequestDTO requestDTO) {
        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        String userId = SecurityUtils.getCurrentUserId();
        CommentEntity comment = CommentEntity.builder()
                .postId(postId)
                .userId(userId)
                .content(requestDTO.getContent())
                .build();
        commentRepository.save(comment);

        // 댓글 수 증가
        post.updateCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        log.info("댓글 추가완료 postId: {}.", postId);
    }

    // 댓글 삭제 (Soft Delete)
    public void softDeleteComment(String commentId) {
        CommentEntity comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

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
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        post.updateCommentCount(post.getCommentCount() - (1 + replies.size()));
        postRepository.save(post);

        log.info("삭제된 commentId: {} , 대댓글 {} . 총 삭제 수: {} postId: {}",
                commentId, replies.size(), (1 + replies.size()), post.getPostId());
    }



    // 특정 게시물의 댓글 및 대댓글 조회
    public List<CommentResponseDTO> getCommentsByPostId(String postId) {
        List<CommentEntity> comments = commentRepository.findByPostIdAndNotDeleted(postId);

        return comments.stream()
                .map(comment -> new CommentResponseDTO(
                        comment.getCommentId(),
                        comment.getUserId(),
                        comment.getContent(),
                        replyCommentService.getRepliesByCommentId(comment.getCommentId()), // 대댓글 조회
                        likeService.getLikeCount(LikeType.valueOf("comment"), comment.getCommentId()) // 댓글 좋아요 수
                )).toList();
    }
}