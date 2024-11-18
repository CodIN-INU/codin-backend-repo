package inu.codin.codin.domain.post.service;

import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.domain.post.dto.request.PostCreateReqDTO;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final S3Service s3Service;

    public PostService(PostRepository postRepository, S3Service s3Service) {
        this.postRepository = postRepository;
        this.s3Service = s3Service;
    }


    public void createPost(PostCreateReqDTO postCreateReqDTO, List<MultipartFile> postImages) {
        validateCreatePostRequest(postCreateReqDTO);

        List<String> imageUrls;
        if (postImages != null && !postImages.isEmpty()) {
            imageUrls = s3Service.uploadFiles(postImages);
        } else {
            imageUrls = List.of(); // 이미지가 없으면 빈 리스트로 초기화
        }

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

    private void validateCreatePostRequest(PostCreateReqDTO postCreateReqDTO) {
//        if (postRepository.findByTitle(postCreateReqDTO.getTitle()).isPresent()) {
//            throw new PostCreateFailException("이미 존재하는 제목입니다. 다른 제목을 사용해주세요.");
//        }

    }
}

