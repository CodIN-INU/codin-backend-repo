package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.comment.service.CommentService;
import inu.codin.codin.domain.post.dto.request.PostAnonymousUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostContentUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostCreateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostStatusUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.response.PostWithCountsResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostWithDetailResponseDTO;

import inu.codin.codin.domain.post.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.domain.scrap.service.ScrapService;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private final S3Service s3Service;
    private final LikeService likeService;
    private final ScrapService scrapService;
    private final CommentService commentService;

    //이미지 업로드 메소드
    private List<String> handleImageUpload(List<MultipartFile> postImages) {
        if (postImages != null && !postImages.isEmpty()) {
            return s3Service.uploadFiles(postImages); // 실제 업로드 처리
        }
        return List.of(); // 이미지가 없을 경우 빈 리스트 반환
    }


    public void createPost(PostCreateRequestDTO postCreateRequestDTO, List<MultipartFile> postImages) {
        validateCreatePostRequest(postCreateRequestDTO);

        List<String> imageUrls = handleImageUpload(postImages);
        String userId = SecurityUtils.getCurrentUserId();

        PostEntity postEntity = PostEntity.builder()
                .userId(userId)
                .title(postCreateRequestDTO.getTitle())
                .content(postCreateRequestDTO.getContent())
                //이미지 Url List 저장
                .postImageUrls(imageUrls)
                .isAnonymous(postCreateRequestDTO.isAnonymous())
                .postCategory(postCreateRequestDTO.getPostCategory())
                //Default Status = Active
                .postStatus(PostStatus.ACTIVE)
                .build();
        postRepository.save(postEntity);
    }


    public void updatePostContent(String postId, PostContentUpdateRequestDTO requestDTO, List<MultipartFile> postImages) {
        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(()->new IllegalArgumentException("해당 게시물 없음"));

        List<String> imageUrls = handleImageUpload(postImages);

        post.updatePostContent(requestDTO.getContent(), imageUrls);
        postRepository.save(post);
    }

    public void updatePostAnonymous(String postId, PostAnonymousUpdateRequestDTO requestDTO) {
        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(()->new IllegalArgumentException("해당 게시물 없음"));
        post.updatePostAnonymous(requestDTO.isAnonymous());
        postRepository.save(post);
    }

    public void updatePostStatus(String postId, PostStatusUpdateRequestDTO requestDTO) {
        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(()->new IllegalArgumentException("해당 게시물 없음"));
        post.updatePostStatus(requestDTO.getPostStatus());
        postRepository.save(post);
    }


    // 모든 글 반환 ::  게시글 내용 + 댓글+대댓글의 수 + 좋아요,스크랩 count 수 반환
    public List<PostWithCountsResponseDTO> getAllPosts() {
        List<PostEntity> posts = postRepository.findAllAndNotDeleted();

        return posts.stream()
                .map(post -> new PostWithCountsResponseDTO(
                        post.getUserId(),
                        post.getPostId(),
                        post.getContent(),
                        post.getTitle(),
                        post.getPostCategory(),
                        post.getPostStatus(),
                        post.getPostImageUrls(),
                        post.isAnonymous(),
                        post.getCommentCount(), // 댓글 수
                        likeService.getLikeCount(LikeType.valueOf("post"),post.getPostId()),       // 좋아요 수
                        scrapService.getScrapCount(post.getPostId())       // 스크랩 수
                ))
                .collect(Collectors.toList());
    }


    //해당 유저가 작성한 모든 글 반환 :: 게시글 내용 + 댓글+대댓글의 수 + 좋아요,스크랩 count 수 반환
    public List<PostWithCountsResponseDTO> getAllUserPosts() {

        String userId = SecurityUtils.getCurrentUserId();

        List<PostEntity> posts = postRepository.findByUserIdAndNotDeleted(userId);

        return posts.stream()
                .map(post -> new PostWithCountsResponseDTO(
                        post.getUserId(),
                        post.getPostId(),
                        post.getContent(),
                        post.getTitle(),
                        post.getPostCategory(),
                        post.getPostStatus(),
                        post.getPostImageUrls(),
                        post.isAnonymous(),
                        post.getCommentCount(),
                        likeService.getLikeCount(LikeType.valueOf("post"),post.getPostId()),       // 좋아요 수
                        scrapService.getScrapCount(post.getPostId())       // 스크랩 수
                ))
                .toList();
    }

    //게시물 상세 조회 :: 게시글 (내용 + 좋아요,스크랩 count 수)  + 댓글 +대댓글 (내용 +좋아요,스크랩 count 수 ) 반환
    public PostWithDetailResponseDTO getPostWithDetail(String postId) {
        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        return new PostWithDetailResponseDTO(
                post.getUserId(),
                post.getPostId(),
                post.getContent(),
                post.getTitle(),
                post.getPostCategory(),
                post.getPostStatus(),
                post.getPostImageUrls(),
                post.isAnonymous(),
                commentService.getCommentsByPostId(postId),                   // 댓글 및 대댓글
                likeService.getLikeCount(LikeType.valueOf("post"),post.getPostId()),   // 좋아요 수
                scrapService.getScrapCount(post.getPostId())   // 스크랩 수
        );
    }



    public void softDeletePost(String postId) {
        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(()-> new IllegalArgumentException("게시물을 찾을 수 없음."));
        post.delete();
        postRepository.save(post);

    }

    public void deletePostImage(String postId, String imageUrl) {

        PostEntity post = postRepository.findByIdAndNotDeleted(postId)
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
    private void validateCreatePostRequest(PostCreateRequestDTO postCreateReqDTO) {
//        if (postRepository.findByTitle(postCreateReqDTO.getTitle()).isPresent()) {
//            throw new PostCreateFailException("이미 존재하는 제목입니다. 다른 제목을 사용해주세요.");
//        }
    }

}

