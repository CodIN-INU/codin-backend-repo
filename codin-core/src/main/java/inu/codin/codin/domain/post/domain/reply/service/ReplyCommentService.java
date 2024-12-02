package inu.codin.codin.domain.post.domain.reply.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.comment.dto.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.like.LikeService;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.domain.reply.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        PostEntity post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

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
    public void deleteReply(String replyId) {
        ReplyCommentEntity reply = replyCommentRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다."));

        if (reply.isDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 대댓글입니다.");
        }

        // 대댓글 삭제
        reply.softDelete();
        replyCommentRepository.save(reply);

        // 댓글 수 감소 (대댓글도 댓글 수에서 감소)
        CommentEntity comment = commentRepository.findById(reply.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        PostEntity post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        post.updateCommentCount(post.getCommentCount() - 1);
        postRepository.save(post);

        log.info("대댓글 성공적 삭제  replyId: {} , postId: {}.", replyId, post.getPostId());
    }

    // 특정 댓글의 대댓글 조회
    public List<CommentResponseDTO> getRepliesByCommentId(String commentId) {
        List<ReplyCommentEntity> replies = replyCommentRepository.findByCommentId(commentId);

        return replies.stream()
                .filter(reply -> !reply.isDeleted())
                .map(reply -> new CommentResponseDTO(
                        reply.getReplyId(),
                        reply.getUserId(),
                        reply.getContent(),
                        List.of(), // 대댓글은 하위 대댓글이 없음
                        likeService.getLikeCount(LikeType.valueOf("reply"), reply.getReplyId())
                ))
                .collect(Collectors.toList());
    }


}
