package inu.codin.codin.domain.post.service;

import inu.codin.codin.domain.post.dto.request.PostCreateReqDTO;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.exception.PostCreateFailException;
import inu.codin.codin.domain.post.repository.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostRepository postRepository;
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }


    public void createPost(PostCreateReqDTO postCreateReqDTO) {
        validateCreatePostRequest(postCreateReqDTO);

        PostEntity postEntity = PostEntity.builder()
                .userId(postCreateReqDTO.getUserId())
                .content(postCreateReqDTO.getContent())
                .title(postCreateReqDTO.getTitle())
                .postCategory(postCreateReqDTO.getPostCategory())
                .postImageUrl(postCreateReqDTO.getPostImageUrl())
                .isAnonymous(postCreateReqDTO.isAnonymous())

                //Default Status = Active
                .postStatus(PostStatus.ACTIVE)

                .build();
        postRepository.save(postEntity);
    }

    private void validateCreatePostRequest(PostCreateReqDTO postCreateReqDTO) {
        if (postRepository.findByTitle(postCreateReqDTO.getTitle()).isPresent()) {
            throw new PostCreateFailException("이미 존재하는 제목입니다. 다른 제목을 사용해주세요.");
        }

    }
}

