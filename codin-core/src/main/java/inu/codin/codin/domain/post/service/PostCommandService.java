package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.post.dto.request.PostAnonymousUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostContentUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostCreateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostStatusUpdateRequestDTO;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.security.OwnershipPolicy;
import inu.codin.codin.domain.user.entity.UserRole;
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
    private final PostInteractionService postInteractionService;
    private final OwnershipPolicy ownershipPolicy;

    /**
     * 게시글 생성
     * @param postCreateRequestDTO 게시글 생성 요청 DTO
     * @param postImages 이미지 파일 리스트
     */
    public void createPost(PostCreateRequestDTO postCreateRequestDTO, List<MultipartFile> postImages) {
        log.info("게시물 생성 시작. UserId: {}, 제목: {}", SecurityUtils.getCurrentUserId(), postCreateRequestDTO.getTitle());
        List<String> imageUrls = postInteractionService.handleImageUpload(postImages);

        ObjectId userId = validateUserAndPost(postCreateRequestDTO.getPostCategory());

        PostEntity postEntity = PostEntity.create(userId, postCreateRequestDTO, imageUrls);
        postRepository.save(postEntity);

        log.info("게시물 성공적으로 생성됨. PostId: {}, UserId: {}", postEntity.get_id(), userId);
    }

    public ObjectId createPostWithoutImagesAndReturn(PostCreateRequestDTO dto) {
        ObjectId userId = validateUserAndPost(dto.getPostCategory());
        PostEntity post = PostEntity.create(userId, dto, List.of());
        PostEntity saved = postRepository.save(post);
        return saved.get_id();
    }

    /**
     * 게시글 내용 및 이미지 수정
     */
    public void updatePostContent(String postId, PostContentUpdateRequestDTO requestDTO, List<MultipartFile> postImages) {
        log.info("게시물 수정 시작. PostId: {}", postId);
        PostEntity post = assertPostOwner(ObjectIdUtil.toObjectId(postId));
        assertCategoryWriteAllowed(post.getPostCategory());

        List<String> imageUrls = postInteractionService.handleImageUpload(postImages);
        post.updatePostContent(requestDTO.getContent(), imageUrls);
        postRepository.save(post);
        log.info("게시물 수정 성공. PostId: {}", postId);
    }

    /**
     * 게시글 익명 설정 수정
     */
    public void updatePostAnonymous(String postId, PostAnonymousUpdateRequestDTO requestDTO) {
        PostEntity post = assertPostOwner(ObjectIdUtil.toObjectId(postId));
        post.updatePostAnonymous(requestDTO.isAnonymous());
        postRepository.save(post);
        log.info("게시물 익명 수정 성공.PostId: {}", postId);
    }

    /**
     * 게시글 상태 수정
     */
    public void updatePostStatus(String postId, PostStatusUpdateRequestDTO requestDTO) {
        PostEntity post = assertPostOwner(ObjectIdUtil.toObjectId(postId));
        post.updatePostStatus(requestDTO.getPostStatus());
        postRepository.save(post);
        log.info("게시물 상태 수정 성공.  PostId: {}, Status: {}",  postId, requestDTO.getPostStatus());
    }



    /**
     * 게시물 소프트 삭제
     */
    public void softDeletePost(String postId) {
        PostEntity post = assertPostOwner(ObjectIdUtil.toObjectId(postId));
        post.delete();
        postRepository.save(post);
        log.info("게시물 안전 삭제. PostId: {}",  postId);
    }

    /**
     * 게시물 이미지 삭제
     */
    public void deletePostImage(String postId, String imageUrl) {
        PostEntity post = assertPostOwner(ObjectIdUtil.toObjectId(postId));
        postInteractionService.deletePostImageInternal(post, imageUrl);
        log.info("게시물 이미지 삭제. PostId: {}", postId);
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
        log.info("익명 번호 할당. PostId: {}, UserId: {}", post.get_id(), userId);
    }


    private ObjectId validateUserAndPost(PostCategory postCategory) {
        if (isPrivileged()) {
            // ADMIN / MANAGER 는 카테고리/유저 상태 검증을 통과시킴
            return SecurityUtils.getCurrentUserId();
        }
        assertCategoryWriteAllowed(postCategory);

        ObjectId userId = SecurityUtils.getCurrentUserId();
        SecurityUtils.validateUser(userId);
        return userId;
    }

    private PostEntity assertPostOwner(ObjectId postId){
        PostEntity post = ownershipPolicy.assertPostOwner(postId);
        assertCategoryWriteAllowed(post.getPostCategory());
        return post;
    }


    private void assertCategoryWriteAllowed(PostCategory postCategory) {
        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER) &&
                postCategory.toString().split("_")[0].equals("EXTRACURRICULAR")) {
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "비교과 게시글에 대한 권한이 없습니다.");
        }
    }

    private boolean isPrivileged() {
        UserRole role = SecurityUtils.getCurrentUserRole();
        return role == UserRole.ADMIN || role == UserRole.MANAGER;
    }

}
