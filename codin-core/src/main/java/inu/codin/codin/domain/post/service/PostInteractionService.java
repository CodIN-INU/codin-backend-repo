package inu.codin.codin.domain.post.service;
import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.infra.s3.exception.ImageRemoveException;
import inu.codin.codin.domain.post.exception.PostException;
import inu.codin.codin.domain.post.exception.PostErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class PostInteractionService {
    private final S3Service s3Service;
    private final PostRepository postRepository;
    private final HitsService hitsService;


    // [ImageService] - 이미지 업로드 처리
    public List<String> handleImageUpload(List<MultipartFile> postImages) {
        return s3Service.handleImageUpload(postImages);
    }

    // [ImageService] - 게시글 이미지 삭제 처리
    public void deletePostImageInternal(PostEntity post, String imageUrl) {
        if (!post.getPostImageUrls().contains(imageUrl)) {
            log.error("게시물에 이미지 없음. PostId: {}, ImageUrl: {}", post.get_id(), imageUrl);
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }
        try {
            post.removePostImage(imageUrl);
            postRepository.save(post);
            log.info("이미지 삭제 성공. PostId: {}, ImageUrl: {}", post.get_id(), imageUrl);
            s3Service.deleteFile(imageUrl);
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생. PostId: {}, ImageUrl: {}", post.get_id(), imageUrl, e);
            throw new ImageRemoveException("이미지 삭제 중 오류 발생: " + imageUrl);
        }
    }

    // [HitsService] - 조회수 증가 처리
    // 비로그인(null) → 무조건 증가, 로그인 → 중복 아닐 때만 증가
    public void increaseHits(PostEntity post, ObjectId userId) {
        if (userId==null || !hitsService.validateHits(post.get_id(), userId)) {
            hitsService.addHits(post.get_id(), userId);
            log.info("조회수 업데이트. PostId: {}, UserId: {}", post.get_id(), userId);
        }
    }

}
