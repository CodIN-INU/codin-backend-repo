package inu.codin.codin.domain.post.comment.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.comment.dto.CommentCreateRequestDTO;
import inu.codin.codin.domain.post.comment.dto.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.comment.dto.CommentResponseDTO;
import inu.codin.codin.domain.post.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.comment.entity.ReplyEntity;
import inu.codin.codin.domain.post.like.LikeService;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.comment.repository.ReplyRepository;
import inu.codin.codin.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final RedisService redisService;
    private final LikeService likeService;

    // 댓글 추가
    public void addComment(String postId, CommentCreateRequestDTO requestDTO) {
        PostEntity post = postRepository.findById(postId)
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

    // 대댓글 추가
    public void addReply(String commentId, ReplyCreateRequestDTO requestDTO) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        PostEntity post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        String userId = SecurityUtils.getCurrentUserId();

        ReplyEntity reply = ReplyEntity.builder()
                .commentId(commentId)
                .userId(userId)
                .content(requestDTO.getContent())
                .build();

        replyRepository.save(reply);

        // 댓글 수 증가 (대댓글도 댓글 수에 포함)
        log.info("대댓글 추가전, commentCount: {}", post.getCommentCount());
        post.updateCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        log.info("대댓글 추가후, commentCount: {}", post.getCommentCount());
    }

    // 댓글 삭제 (Soft Delete)
    public void deleteComment(String commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (comment.isDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 댓글입니다.");
        }

        // 댓글의 대댓글 조회
        List<ReplyEntity> replies = replyRepository.findByCommentId(commentId);

        // 대댓글 Soft Delete 처리
        replies.forEach(reply -> {
            if (!reply.isDeleted()) {
                reply.softDelete();
                replyRepository.save(reply);
            }
        });

        // 댓글 Soft Delete 처리
        comment.softDelete();
        commentRepository.save(comment);

        // 댓글 수 감소 (댓글 + 대댓글 수만큼 감소)
        PostEntity post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        post.updateCommentCount(post.getCommentCount() - (1 + replies.size()));
        postRepository.save(post);

        log.info("삭제된 commentId: {} , 대댓글 {} . 총 삭제 수: {} postId: {}",
                commentId, replies.size(), (1 + replies.size()), post.getPostId());
    }

    // 대댓글 삭제 (Soft Delete)
    public void deleteReply(String replyId) {
        ReplyEntity reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다."));

        if (reply.isDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 대댓글입니다.");
        }

        // 대댓글 삭제
        reply.softDelete();
        replyRepository.save(reply);

        // 댓글 수 감소 (대댓글도 댓글 수에서 감소)
        CommentEntity comment = commentRepository.findById(reply.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        PostEntity post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        post.updateCommentCount(post.getCommentCount() - 1);
        postRepository.save(post);

        log.info("대댓글 성공적 삭제  replyId: {} , postId: {}.", replyId, post.getPostId());
    }

    // 특정 게시물의 댓글 및 대댓글 조회
    public List<CommentResponseDTO> getCommentsByPostId(String postId) {
        List<CommentEntity> comments = commentRepository.findByPostId(postId);

        return comments.stream()
                .filter(comment -> !comment.isDeleted())
                .map(comment -> new CommentResponseDTO(
                        comment.getCommentId(),
                        comment.getUserId(),
                        comment.getContent(),
                        getRepliesByCommentId(comment.getCommentId()), // 대댓글 조회
                        likeService.getLikeCount("comment", comment.getCommentId()) // 댓글 좋아요 수
                ))
                .collect(Collectors.toList());
    }

    // 특정 댓글의 대댓글 조회
    private List<CommentResponseDTO> getRepliesByCommentId(String commentId) {
        List<ReplyEntity> replies = replyRepository.findByCommentId(commentId);

        return replies.stream()
                .filter(reply -> !reply.isDeleted())
                .map(reply -> new CommentResponseDTO(
                        reply.getReplyId(),
                        reply.getUserId(),
                        reply.getContent(),
                        List.of(), // 대댓글은 하위 대댓글이 없음
                        likeService.getLikeCount("reply", reply.getReplyId())
                ))
                .collect(Collectors.toList());
    }
}