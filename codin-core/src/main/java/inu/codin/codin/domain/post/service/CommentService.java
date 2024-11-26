package inu.codin.codin.domain.post.service;

import inu.codin.codin.domain.post.dto.request.CommentCreateRequsetDTO;
import inu.codin.codin.domain.post.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.dto.response.CommentsResponseDTO;
import inu.codin.codin.domain.post.entity.CommentEntity;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final PostRepository postRepository;

    public void addComment(String postId, CommentCreateRequsetDTO requestDTO) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        CommentEntity comment = CommentEntity.builder()
                .commentId(UUID.randomUUID().toString()) // UUID로 고유 ID 생성
                .userId(requestDTO.getUserId())
                .content(requestDTO.getContent())
                .build();
        post.addComment(comment);
        postRepository.save(post);
        log.info("Comment added successfully to postId: {}. Request data: {}", postId, requestDTO);
    }

    public void addReply(String postId, String parentCommentId, ReplyCreateRequestDTO requestDTO) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        CommentEntity reply = CommentEntity.builder()
                .commentId(UUID.randomUUID().toString()) // UUID로 고유 ID 생성
                .userId(requestDTO.getUserId())
                .content(requestDTO.getContent())
                .build();
        post.addReply(parentCommentId, reply);
        postRepository.save(post);
    }

    public List<CommentsResponseDTO> getCommentsByUser(String userId) {
        List<PostEntity> posts = postRepository.findAll(); // 모든 게시물 조회
        List<CommentsResponseDTO> userComments = new ArrayList<>();

        for (PostEntity post : posts) {
            // 게시물의 댓글 중 해당 사용자가 작성한 댓글 필터링
            post.getComments().stream()
                    .filter(comment -> comment.getUserId().equals(userId) && !comment.isDeleted())
                    .map(this::convertToDTO) // CommentEntity -> CommentsResponseDTO
                    .forEach(userComments::add);

            // 게시물의 대댓글 중 해당 사용자가 작성한 대댓글 필터링
            post.getComments().forEach(comment -> comment.getReplies().stream()
                    .filter(reply -> reply.getUserId().equals(userId) && !reply.isDeleted())
                    .map(this::convertToDTO) // CommentEntity -> CommentsResponseDTO
                    .forEach(userComments::add));
        }

        return userComments;
    }

    private CommentsResponseDTO convertToDTO(CommentEntity comment) {
        return new CommentsResponseDTO(
                comment.getCommentId(),
                comment.getUserId(),
                comment.getContent(),
                comment.getReplies().stream()
                        .filter(reply -> !reply.isDeleted()) // 삭제되지 않은 대댓글만 변환
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()) // 대댓글도 재귀적으로 DTO로 변환
        );
    }


    public void deleteComment(String postId, String commentId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        post.softDeleteComment(commentId);
        postRepository.save(post);
    }

    public void deleteReply(String postId, String parentCommentId, String replyId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));
        post.softDeleteReply(parentCommentId, replyId);
        postRepository.save(post);
    }
}
