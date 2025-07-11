package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.poll.service.PollService;
import inu.codin.codin.domain.post.dto.request.PostAnonymousUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostContentUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostCreateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostStatusUpdateRequestDTO;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.scrap.service.ScrapService;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostCommandService {
    private final PostRepository postRepository;
    private final SeperatedPostService seperatedPostService;

    /**
     * 게시글 생성
     * @param postCreateRequestDTO 게시글 생성 요청 DTO
     * @param postImages 이미지 파일 리스트
     */
    public void createPost(PostCreateRequestDTO postCreateRequestDTO, List<MultipartFile> postImages) {
        log.info("게시물 생성 시작. UserId: {}, 제목: {}", SecurityUtils.getCurrentUserId(), postCreateRequestDTO.getTitle());
        List<String> imageUrls = seperatedPostService.handleImageUpload(postImages);
        ObjectId userId = SecurityUtils.getCurrentUserId();

        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER) &&
                postCreateRequestDTO.getPostCategory().toString().split("_")[0].equals("EXTRACURRICULAR")){
            log.error("비교과 게시물에 대한 접근권한 없음. UserId: {}", userId);
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "비교과 게시글에 대한 권한이 없습니다.");
        }

        PostEntity postEntity = PostEntity.create(userId, postCreateRequestDTO, imageUrls);
        postRepository.save(postEntity);
        log.info("게시물 성공적으로 생성됨. PostId: {}, UserId: {}", postEntity.get_id(), userId);
    }

    /**
     * 게시글 내용 및 이미지 수정
     */
    public void updatePostContent(String postId, PostContentUpdateRequestDTO requestDTO, List<MultipartFile> postImages) {
        log.info("게시물 수정 시작. PostId: {}", postId);
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("해당 게시물 없음. id=" + postId));
        validateUserAndPost(post);
        List<String> imageUrls = seperatedPostService.handleImageUpload(postImages);
        post.updatePostContent(requestDTO.getContent(), imageUrls);
        postRepository.save(post);
        log.info("게시물 수정 성공. PostId: {}", postId);
    }

    /**
     * 게시글 익명 설정 수정
     */
    public void updatePostAnonymous(String postId, PostAnonymousUpdateRequestDTO requestDTO) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("해당 게시물 없음. id=" + postId));
        validateUserAndPost(post);
        post.updatePostAnonymous(requestDTO.isAnonymous());
        postRepository.save(post);
        log.info("게시물 익명 수정 성공. PostId: {}", postId);
    }

    /**
     * 게시글 상태 수정
     */
    public void updatePostStatus(String postId, PostStatusUpdateRequestDTO requestDTO) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("해당 게시물 없음. id=" + postId));
        validateUserAndPost(post);
        post.updatePostStatus(requestDTO.getPostStatus());
        postRepository.save(post);
        log.info("게시물 상태 수정 성공. PostId: {}, Status: {}", postId, requestDTO.getPostStatus());
    }



    /**
     * 게시물 소프트 삭제
     */
    public void softDeletePost(String postId) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없음. id=" + postId));
        validateUserAndPost(post);
        post.delete();
        log.info("게시물 안전 삭제. PostId: {}", postId);
        postRepository.save(post);
    }

    /**
     * 게시물 이미지 삭제
     */
    public void deletePostImage(String postId, String imageUrl) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다. id=" + postId));
        validateUserAndPost(post);
        seperatedPostService.deletePostImageInternal(post, imageUrl);
    }

    /**
     * 댓글 생성시 필요한 모든 처리를 한 번에
     */
    public void handleCommentCreation(PostEntity post, ObjectId userId) {
        assignAnonymousNumber(post, userId);
        increaseCommentCount(post);
        postRepository.save(post);
    }

    /**
     * 댓글/ 대댓글 작성시 댓글/대댓글 작성수 증가
     * @param post - postEntity
     */
    public void increaseCommentCount(PostEntity post) {
        post.plusCommentCount();
        log.info("댓글 수 증가. PostId: {}, 현재: {}", post.get_id(), post.getCommentCount());
    }

    /**
     * 댓글/ 대댓글 작성시 댓글/대댓글 작성수 감소
     * @param post - postEntity
     */
    public void decreaseCommentCount(PostEntity post) {
        post.minusCommentCount();
        postRepository.save(post);
        log.info("댓글 수 감소. PostId: {}, 현재: {}", post.get_id(), post.getCommentCount());
    }

    /**
     * 익명 번호 할당
     */
    public void assignAnonymousNumber(PostEntity post, ObjectId userId) {
        if (!post.isAnonymous()) {
            return;
        }

        PostAnonymous anonymous = post.getAnonymous();

        // 이미 익명 번호가 있으면 할당하지 않음
        if (anonymous.hasAnonNumber(userId)) {
            return;
        }

        if (post.isWriter(userId)) {
            anonymous.setWriter(userId);
        } else {anonymous.setAnonNumber(userId);
        }
        postRepository.save(post);
        log.info("익명 번호 할당. PostId: {}, UserId: {}", post.get_id(), userId);
    }

    /**
     * 유저의 익명 번호 조회
     */
    public Integer getUserAnonymousNumber(PostAnonymous postAnonymous, ObjectId userId) {
        return postAnonymous.getAnonNumber(userId);
    }

    private void validateUserAndPost(PostEntity post) {
        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER) &&
                post.getPostCategory().toString().split("_")[0].equals("EXTRACURRICULAR")){
            log.error("비교과 게시글에 대한 권한이 없음. PostId: {}", post.get_id());
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "비교과 게시글에 대한 권한이 없습니다.");
        }
        SecurityUtils.validateUser(post.getUserId());
    }


}
