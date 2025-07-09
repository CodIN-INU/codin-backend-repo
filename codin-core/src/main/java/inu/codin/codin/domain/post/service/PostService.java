package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.best.BestEntity;
import inu.codin.codin.domain.post.domain.best.BestRepository;
import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollVoteRepository;
import inu.codin.codin.domain.post.dto.request.PostAnonymousUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostContentUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostCreateRequestDTO;
import inu.codin.codin.domain.post.dto.request.PostStatusUpdateRequestDTO;
import inu.codin.codin.domain.post.dto.response.*;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.scrap.service.ScrapService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.service.RedisBestService;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.infra.s3.exception.ImageRemoveException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final BestRepository bestRepository;
    private final UserRepository userRepository;
    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;

    private final S3Service s3Service;
    private final LikeService likeService;
    private final ScrapService scrapService;
    private final HitsService hitsService;
    private final RedisBestService redisBestService;
    private final BlockService blockService;
    private final SeperatedPostService seperatedPostService;

    public Map<String, String> createPost(PostCreateRequestDTO postCreateRequestDTO, List<MultipartFile> postImages) {
        log.info("게시물 생성 시작. UserId: {}, 제목: {}", SecurityUtils.getCurrentUserId(), postCreateRequestDTO.getTitle());
        List<String> imageUrls = seperatedPostService.handleImageUpload(postImages);
        ObjectId userId = SecurityUtils.getCurrentUserId();

        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER) &&
                postCreateRequestDTO.getPostCategory().toString().split("_")[0].equals("EXTRACURRICULAR")){
            log.error("비교과 게시물에 대한 접근권한 없음. UserId: {}", userId);
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "비교과 게시글에 대한 권한이 없습니다.");
        }

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
        log.info("게시물 성공적으로 생성됨. PostId: {}, UserId: {}", postEntity.get_id(), userId);
        Map<String, String> response = new HashMap<>();
        response.put("postId", postEntity.get_id().toString());
        return response;
    }

    public void updatePostContent(String postId, PostContentUpdateRequestDTO requestDTO, List<MultipartFile> postImages) {
        log.info("게시물 수정 시작. PostId: {}", postId);

        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()->new NotFoundException("해당 게시물 없음"));
        validateUserAndPost(post);

        List<String> imageUrls = seperatedPostService.handleImageUpload(postImages);

        post.updatePostContent(requestDTO.getContent(), imageUrls);
        postRepository.save(post);
        log.info("게시물 수정 성공. PostId: {}", postId);
    }

    public void updatePostAnonymous(String postId, PostAnonymousUpdateRequestDTO requestDTO) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()->new NotFoundException("해당 게시물 없음"));
        validateUserAndPost(post);

        post.updatePostAnonymous(requestDTO.isAnonymous());
        postRepository.save(post);
        log.info("게시물 익명 수정 성공. PostId: {}", postId);
    }

    public void updatePostStatus(String postId, PostStatusUpdateRequestDTO requestDTO) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()->new NotFoundException("해당 게시물 없음"));
        validateUserAndPost(post);

        post.updatePostStatus(requestDTO.getPostStatus());
        postRepository.save(post);
        log.info("게시물 상태 수정 성공. PostId: {}, Status: {}", postId, requestDTO.getPostStatus());
    }

    private void validateUserAndPost(PostEntity post) {
        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER) &&
                post.getPostCategory().toString().split("_")[0].equals("EXTRACURRICULAR")){
            log.error("비교과 게시글에 대한 권한이 없음. PostId: {}", post.get_id());
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "비교과 게시글에 대한 권한이 없습니다.");
        }
        SecurityUtils.validateUser(post.getUserId());
    }

    // 모든 글 반환 ::  게시글 내용 + 댓글+대댓글의 수 + 좋아요,스크랩 count 수 반환
    public PostPageResponse getAllPosts(PostCategory postCategory, int pageNumber) {
        // 차단 목록 조회
        List<ObjectId> blockedUsersId = blockService.getBlockedUsers();

        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page;
        page = postRepository.getPostsByCategoryWithBlockedUsers(postCategory.toString(), blockedUsersId ,pageRequest);

        log.info("모든 글 반환 성공 Category: {}, Page: {}", postCategory, pageNumber);
        return PostPageResponse.of(getPostListResponseDtos(page.getContent()), page.getTotalPages() - 1, page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1);
    }

    // 게시물 리스트 가져오기
    public List<PostPageItemResponseDTO> getPostListResponseDtos(List<PostEntity> posts) {
        return posts.stream()
                .map(this::toPageItemDTO)
                .toList();
    }

    // 게시물 상세 조회
    public PostPageItemResponseDTO getPostWithDetail(String postId) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
        ObjectId userId = SecurityUtils.getCurrentUserId();
        seperatedPostService.increaseHitsIfNeeded(post, userId); // [HitsService] - 조회수 증가 위임
        return toPageItemDTO(post);
    }

    // ReportService 등에서 사용할 수 있도록 ObjectId로 단건 상세 조회 (Optional)
    public Optional<PostPageItemResponseDTO> getPostDetailById(ObjectId postId) {
        return postRepository.findByIdAndNotDeleted(postId)
                .map(post -> {
                    ObjectId userId = SecurityUtils.getCurrentUserId();
                    seperatedPostService.increaseHitsIfNeeded(post, userId); // [HitsService] - 조회수 증가 위임
                    return toPageItemDTO(post);
                });
    }

    public void softDeletePost(String postId) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()-> new NotFoundException("게시물을 찾을 수 없음."));
        validateUserAndPost(post);

        post.delete();

        log.info("게시물 안전 삭제. PostId: {}", postId);
        postRepository.save(post);
    }

    public void deletePostImage(String postId, String imageUrl) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
        validateUserAndPost(post);

        seperatedPostService.deletePostImageInternal(post, imageUrl);
    }

    public PostPageResponse searchPosts(String keyword, int pageNumber) {
        // 차단 목록 조회
        List<ObjectId> blockedUsersId = blockService.getBlockedUsers();

        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page = postRepository.findAllByKeywordAndDeletedAtIsNull(keyword, blockedUsersId, pageRequest);
        log.info("키워드 기반 게시물 검색: {}, Page: {}", keyword, pageNumber);
        return PostPageResponse.of(getPostListResponseDtos(page.getContent()), page.getTotalPages() - 1, page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1);
    }

    public List<PostPageItemResponseDTO> getTop3BestPosts() {
        List<PostEntity> bestPosts = seperatedPostService.getTop3BestPostsInternal(); // [BestService] - 베스트 게시물 조회 위임
        log.info("Top 3 베스트 게시물 반환.");
        return getPostListResponseDtos(bestPosts);
    }

    public PostPageResponse getBestPosts(int pageNumber) {
        Page<PostEntity> page = seperatedPostService.getBestPostsInternal(pageNumber); // [BestService] - 베스트 게시물 페이지 조회 위임
        return PostPageResponse.of(getPostListResponseDtos(page.getContent()), page.getTotalPages() - 1, page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1);
    }


    // PostEntity → PostPageItemResponseDTO 변환 (공통 변환 로직)
    private PostPageItemResponseDTO toPageItemDTO(PostEntity post) {
        UserProfile userProfile = resolveUserProfile(post);
        int likeCount = seperatedPostService.getLikeCount(post);
        int scrapCount = seperatedPostService.getScrapCount(post);
        int hitsCount = seperatedPostService.getHitsCount(post);
        int commentCount = post.getCommentCount();
        ObjectId userId = SecurityUtils.getCurrentUserId();
        UserInfoResponseDTO userInfo = getUserInfoAboutPost(userId, post.getUserId(), post.get_id());
        PostDetailResponseDTO postDTO = PostDetailResponseDTO.of(post, userProfile.nickname, userProfile.userImageUrl, likeCount, scrapCount, hitsCount, commentCount, userInfo);
        if (post.getPostCategory() == PostCategory.POLL) {
            PollInfoResponseDTO pollInfo = seperatedPostService.getPollInfo(post, userId);
            return PostPageItemResponseDTO.of(postDTO, pollInfo);
        } else {
            return PostPageItemResponseDTO.of(postDTO, null);
        }
    }

    // [유저 프로필] - 닉네임/이미지 결정
    private UserProfile resolveUserProfile(PostEntity post) {
        UserEntity user = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        String nickname;
        String userImageUrl;
        if (user.getDeletedAt() == null) {
            if (post.isAnonymous()) {
                nickname = "익명";
                userImageUrl = s3Service.getDefaultProfileImageUrl();
            } else {
                nickname = user.getNickname();
                userImageUrl = user.getProfileImageUrl();
            }
        } else {
            nickname = user.getNickname();
            userImageUrl = user.getProfileImageUrl();
        }
        return new UserProfile(nickname, userImageUrl);
    }

    // [유저 프로필] - 게시물에 대한 유저정보 추출
    public UserInfoResponseDTO getUserInfoAboutPost(ObjectId currentUserId, ObjectId postUserId, ObjectId postId){
        return UserInfoResponseDTO.of(
                likeService.isLiked(LikeType.POST, postId, currentUserId),
                scrapService.isPostScraped(postId, currentUserId),
                postUserId.equals(currentUserId)
        );
    }

        // [내부 클래스] - 유저 프로필 정보
    private static class UserProfile {
        final String nickname;
        final String userImageUrl;
        UserProfile(String nickname, String userImageUrl) {
            this.nickname = nickname;
            this.userImageUrl = userImageUrl;
        }
    }

       // ===================== 도메인별 부가 기능/서브 로직 (임시 분리 - SeperatedPostService) =====================

}

