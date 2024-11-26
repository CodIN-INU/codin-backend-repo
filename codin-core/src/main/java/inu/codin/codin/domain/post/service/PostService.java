package inu.codin.codin.domain.post.service;

import inu.codin.codin.domain.post.dto.request.PostContentUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostStatusUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.response.CommentsResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostWithCommentsResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostWithLikeAndScrapResponseDTO;
import inu.codin.codin.domain.post.entity.CommentEntity;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.domain.post.dto.request.PostCreateReqDTO;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final S3Service s3Service;
    private final RedisTemplate<String, String> redisTemplate;

    //이미지 업로드 메소드
    private List<String> handleImageUpload(List<MultipartFile> postImages) {
        if (postImages != null && !postImages.isEmpty()) {
            return s3Service.uploadFiles(postImages); // 실제 업로드 처리
        }
        return List.of(); // 이미지가 없을 경우 빈 리스트 반환
    }


    public void createPost(PostCreateReqDTO postCreateReqDTO, List<MultipartFile> postImages) {
        validateCreatePostRequest(postCreateReqDTO);

        List<String> imageUrls = handleImageUpload(postImages);


        PostEntity postEntity = PostEntity.builder()
                .userId(postCreateReqDTO.getUserId())
                .content(postCreateReqDTO.getContent())
                .title(postCreateReqDTO.getTitle())
                .postCategory(postCreateReqDTO.getPostCategory())

                //이미지 Url List 저장
                .postImageUrls(imageUrls)

                .isAnonymous(postCreateReqDTO.isAnonymous())

                //Default Status = Active
                .postStatus(PostStatus.ACTIVE)
                .comments(new ArrayList<>())
                .build();
        postRepository.save(postEntity);
    }


    public void updatePostContent(String postId, PostContentUpdateRequestDTO requestDTO, List<MultipartFile> postImages) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(()->new IllegalArgumentException("해당 게시물 없음"));

        List<String> imageUrls = handleImageUpload(postImages);

        //이미지 삭제 로직

        post.updatePostContent(requestDTO.getContent(), imageUrls);
    }


    public void updatePostStatus(String postId, PostStatusUpdateRequestDTO requestDTO) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(()->new IllegalArgumentException("해당 게시물 없음"));
        post.updatePostStatus(requestDTO.getPostStatus());
    }

    public List<PostDetailResponseDTO> getAllPosts() {
        List<PostEntity> posts = postRepository.findALlNotDeleted();
        return posts.stream()
                .map(post -> new PostDetailResponseDTO(
                        post.getUserId(),
                        post.getPostId(),
                        post.getContent(),
                        post.getTitle(),
                        post.getPostCategory(),
                        post.getPostStatus(),
                        post.getPostImageUrls(),
                        post.isAnonymous()
                ))
                .collect(Collectors.toList());
    }

    public List<PostDetailResponseDTO> getAllUserPosts(String userId) {
        List<PostEntity> posts = postRepository.findByUserIdNotDeleted(userId);
        return posts.stream()
                .map(post -> new PostDetailResponseDTO(
                        post.getUserId(),
                        post.getPostId(),
                        post.getContent(),
                        post.getTitle(),
                        post.getPostCategory(),
                        post.getPostStatus(),
                        post.getPostImageUrls(),
                        post.isAnonymous()
                ))
                .collect(Collectors.toList());
    }

    public PostDetailResponseDTO getPost(String postId) {
        PostEntity post = postRepository.findByPostIdNotDeleted(postId);

        return new PostDetailResponseDTO(
                post.getUserId(),
                post.getPostId(),
                post.getContent(),
                post.getTitle(),
                post.getPostCategory(),
                post.getPostStatus(),
                post.getPostImageUrls(),
                post.isAnonymous()
        );

    }

    public void softDeletePost(String postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("게시물을 찾을 수 없음."));

        if (post.isDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 게시물");
        }

        post.softDeletePost();
        postRepository.save(post);

    }

    public void deletePostImage(String postId, String imageUrl) {

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        if (!post.getPostImageUrls().contains(imageUrl)) {
            throw new IllegalArgumentException("이미지가 게시물에 존재하지 않습니다.");
        }

        try {
            // S3에서 이미지 삭제
            s3Service.deleteFile(imageUrl);
            // 게시물의 이미지 리스트에서 제거
            post.removePostImage(imageUrl);
            postRepository.save(post);
        } catch (Exception e) {
            throw new IllegalStateException("이미지 삭제 중 오류 발생: " + imageUrl, e);
        }
    }




    //유효성체크
    private void validateCreatePostRequest(PostCreateReqDTO postCreateReqDTO) {
//        if (postRepository.findByTitle(postCreateReqDTO.getTitle()).isPresent()) {
//            throw new PostCreateFailException("이미 존재하는 제목입니다. 다른 제목을 사용해주세요.");
//        }

    }
    public List<PostWithCommentsResponseDTO> getAllUserPostsAndComments(String userId) {
        // 삭제되지 않은 사용자 게시물 조회
        List<PostEntity> posts = postRepository.findByUserIdNotDeleted(userId);

        // PostEntity를 PostWithCommentsResponseDTO로 변환
        return posts.stream()
                .map(post -> new PostWithCommentsResponseDTO(
                        post.getUserId(),
                        post.getPostId(),
                        post.getContent(),
                        post.getTitle(),
                        post.getPostCategory(),
                        post.getPostStatus(),
                        post.getPostImageUrls(),
                        post.isAnonymous(),
                        convertCommentsToDTO(post.getComments()) // 댓글과 대댓글을 DTO로 변환
                ))
                .collect(Collectors.toList());
    }

    public PostWithLikeAndScrapResponseDTO getPostWithLikeAndScrap(String postId) {
        String likeKey = "post:likes:" + postId;
        String scrapKey = "post:scraps:" + postId;

        // Redis에서 좋아요 및 스크랩 카운트 조회
        Long likeCount = redisTemplate.opsForSet().size(likeKey);
        Long scrapCount = redisTemplate.opsForSet().size(scrapKey);

        // Redis에 데이터가 없을 경우 MongoDB에서 조회 후 Redis 갱신
        if (likeCount == null || scrapCount == null) {
            PostEntity post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

            if (likeCount == null) {
                likeCount = (long) post.getLikeCount();
                redisTemplate.opsForSet().add(likeKey, String.valueOf(likeCount));
            }

            if (scrapCount == null) {
                scrapCount = (long) post.getScrapCount();
                redisTemplate.opsForSet().add(scrapKey, String.valueOf(scrapCount));
            }
        }

        // MongoDB에서 게시물 조회
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        // PostWithLikeAndScrapResponseDTO 반환
        return new PostWithLikeAndScrapResponseDTO(
                post.getUserId(),
                post.getPostId(),
                post.getContent(),
                post.getTitle(),
                post.getPostCategory(),
                post.getPostStatus(),
                post.getPostImageUrls(),
                post.isAnonymous(),
                convertCommentsToDTO(post.getComments()), // 댓글 변환 메소드 호출
                likeCount.intValue(),
                scrapCount.intValue()
        );
    }
    private List<CommentsResponseDTO> convertCommentsToDTO(List<CommentEntity> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of(); // 댓글이 없는 경우 빈 리스트 반환
        }

        return comments.stream()
                .filter(comment -> !comment.isDeleted()) // 삭제되지 않은 댓글만 포함
                .map(comment -> new CommentsResponseDTO(
                        comment.getCommentId(),
                        comment.getUserId(),
                        comment.getContent(),
                        convertCommentsToDTO(comment.getReplies()) // 재귀적으로 대댓글 변환
                ))
                .collect(Collectors.toList());
    }
}

