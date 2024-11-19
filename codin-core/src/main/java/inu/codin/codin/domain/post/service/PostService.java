package inu.codin.codin.domain.post.service;

import inu.codin.codin.domain.post.dto.request.PostContentUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostStatusUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.domain.post.dto.request.PostCreateReqDTO;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final S3Service s3Service;

    public PostService(PostRepository postRepository, S3Service s3Service) {
        this.postRepository = postRepository;
        this.s3Service = s3Service;
    }

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

    public List<PostDetailResponseDTO> getAllPosts(String userId) {
        List<PostEntity> posts = postRepository.findByUserId(userId);
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
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

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

    public void deletePost(String postId) {
        postRepository.deleteById(postId);
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
}

