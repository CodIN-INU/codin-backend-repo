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

    public Map<String, String> createPost(PostCreateRequestDTO postCreateRequestDTO, List<MultipartFile> postImages) {
        log.info("게시물 생성 시작. UserId: {}, 제목: {}", SecurityUtils.getCurrentUserId(), postCreateRequestDTO.getTitle());
        List<String> imageUrls = handleImageUpload(postImages);
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

        List<String> imageUrls = handleImageUpload(postImages);

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
    public List<PostDetailResponseDTO> getPostListResponseDtos(List<PostEntity> posts) {
        return posts.stream()
                .map(this::createPostDetailResponse)
                .toList();
    }

    // 게시물 상세 조회
    public PostDetailResponseDTO getPostWithDetail(String postId) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));

        ObjectId userId = SecurityUtils.getCurrentUserId();
        increaseHitsIfNeeded(post, userId); // [HitsService] - 조회수 증가 위임

        return createPostDetailResponse(post);
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

        deletePostImageInternal(post, imageUrl);
    }

    public PostPageResponse searchPosts(String keyword, int pageNumber) {
        // 차단 목록 조회
        List<ObjectId> blockedUsersId = blockService.getBlockedUsers();

        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page = postRepository.findAllByKeywordAndDeletedAtIsNull(keyword, blockedUsersId, pageRequest);
        log.info("키워드 기반 게시물 검색: {}, Page: {}", keyword, pageNumber);
        return PostPageResponse.of(getPostListResponseDtos(page.getContent()), page.getTotalPages() - 1, page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1);
    }

    public List<PostDetailResponseDTO> getTop3BestPosts() {
        List<PostEntity> bestPosts = getTop3BestPostsInternal(); // [BestService] - 베스트 게시물 조회 위임
        log.info("Top 3 베스트 게시물 반환.");
        return getPostListResponseDtos(bestPosts);
    }

    public PostPageResponse getBestPosts(int pageNumber) {
        Page<PostEntity> page = getBestPostsInternal(pageNumber); // [BestService] - 베스트 게시물 페이지 조회 위임
        return PostPageResponse.of(getPostListResponseDtos(page.getContent()), page.getTotalPages() - 1, page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1);
    }

    public Optional<PostDetailResponseDTO> getPostDetailById(ObjectId postId) {
        return postRepository.findById(postId)
                .map(this::createPostDetailResponse);
    }

    // Post 정보를 처리하여 DTO를 생성하는 공통 메소드
    private PostDetailResponseDTO createPostDetailResponse(PostEntity post) {
        // 1. 닉네임/이미지 결정
        UserProfile userProfile = resolveUserProfile(post);

        // 2. 부가 정보 조회
        int likeCount = getLikeCount(post);
        int scrapCount = getScrapCount(post);
        int hitsCount = getHitsCount(post);
        int commentCount = post.getCommentCount();
        ObjectId userId = SecurityUtils.getCurrentUserId();
        UserInfoResponseDTO userInfo = getUserInfoAboutPost(userId, post.getUserId(), post.get_id());

        // 3. 투표 게시글 분기
        if (post.getPostCategory() == PostCategory.POLL) {
            PollInfoResponseDTO pollInfo = getPollInfo(post, userId);
            log.info("게시글-투표 상세정보 생성 성공. PostId: {}", post.get_id());
            return PostPollDetailResponseDTO.of(
                    PostDetailResponseDTO.of(post, userProfile.nickname, userProfile.userImageUrl, likeCount, scrapCount, hitsCount, commentCount, userInfo),
                    pollInfo
            );
        }
        // 4. 일반 게시물 처리
        return PostDetailResponseDTO.of(post, userProfile.nickname, userProfile.userImageUrl, likeCount, scrapCount, hitsCount, commentCount, userInfo);
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

    public UserInfoResponseDTO getUserInfoAboutPost(ObjectId currentUserId, ObjectId postUserId, ObjectId postId){
        return UserInfoResponseDTO.of(
                likeService.isLiked(LikeType.POST, postId, currentUserId),
                scrapService.isPostScraped(postId, currentUserId),
                postUserId.equals(currentUserId)
        );
    }

       // ===================== 도메인별 부가 기능/서브 로직 (분리 예정) =====================

    // [ImageService] - 이미지 업로드 처리
    private List<String> handleImageUpload(List<MultipartFile> postImages) {
        return s3Service.handleImageUpload(postImages);
    }

    // [ImageService] - 게시글 이미지 삭제 처리
    private void deletePostImageInternal(PostEntity post, String imageUrl) {
        if (!post.getPostImageUrls().contains(imageUrl)) {
            log.error("게시물에 이미지 없음. PostId: {}, ImageUrl: {}", post.get_id(), imageUrl);
            throw new NotFoundException("이미지가 게시물에 존재하지 않습니다.");
        }
        try {
            s3Service.deleteFile(imageUrl);
            post.removePostImage(imageUrl);
            postRepository.save(post);
            log.info("이미지 삭제 성공. PostId: {}, ImageUrl: {}", post.get_id(), imageUrl);
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생. PostId: {}, ImageUrl: {}", post.get_id(), imageUrl, e);
            throw new ImageRemoveException("이미지 삭제 중 오류 발생: " + imageUrl);
        }
    }

    // [HitsService] - 조회수 증가 처리
    private void increaseHitsIfNeeded(PostEntity post, ObjectId userId) {
        if (!hitsService.validateHits(post.get_id(), userId)) {
            hitsService.addHits(post.get_id(), userId);
            log.info("조회수 업데이트. PostId: {}, UserId: {}", post.get_id(), userId);
        }
    }

    // [BestService] - Top 3 베스트 게시물 조회
    private List<PostEntity> getTop3BestPostsInternal() {
        Map<String, Double> posts = redisBestService.getBests();
        return posts.entrySet().stream()
                .map(post -> postRepository.findByIdAndNotDeleted(new ObjectId(post.getKey()))
                        .orElseGet(() -> {
                            redisBestService.deleteBest(post.getKey());
                            return null;
                        }))
                .filter(Objects::nonNull)
                .toList();
    }

    // [BestService] - 베스트 게시물 페이지 조회
    private Page<PostEntity> getBestPostsInternal(int pageNumber) {
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<BestEntity> bests = bestRepository.findAll(pageRequest);
        return bests.map(bestEntity -> postRepository.findByIdAndNotDeleted(bestEntity.getPostId())
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다.")));
    }

    // [BestService] - 베스트 점수 적용 처리
    private void applyBestScoreIfNeeded(PostEntity post) {
        redisBestService.applyBestScore(1, post.get_id());
        log.info("베스트 점수 적용. PostId: {}", post.get_id());
    }

    // [CommentService/ReplyCommentService] - 댓글 수 증가
    private void increaseCommentCount(PostEntity post) {
        post.plusCommentCount();
        postRepository.save(post);
        log.info("댓글 수 증가. PostId: {}, 현재: {}", post.get_id(), post.getCommentCount());
    }

    // [PollService] - 투표 관련 처리 (예시)
    private void processPollIfNeeded(PostEntity post) {
        // 투표 게시글일 경우 PollService와 협력하여 투표 생성/집계 등 처리
        // 예시: pollService.createPoll(...)
    }

    // [좋아요] - 게시글 좋아요 수 조회
    private int getLikeCount(PostEntity post) {
        return likeService.getLikeCount(LikeType.POST, post.get_id());
    }

    // [스크랩] - 게시글 스크랩 수 조회
    private int getScrapCount(PostEntity post) {
        return scrapService.getScrapCount(post.get_id());
    }

    // [조회수] - 게시글 조회수 조회
    private int getHitsCount(PostEntity post) {
        return hitsService.getHitsCount(post.get_id());
    }

    // [투표] - 투표 게시글 PollInfo 생성
    private PollInfoResponseDTO getPollInfo(PostEntity post, ObjectId userId) {
        PollEntity poll = pollRepository.findByPostId(post.get_id())
                .orElseThrow(() -> new NotFoundException("투표 정보를 찾을 수 없습니다."));
        long totalParticipants = pollVoteRepository.countByPollId(poll.get_id());
        List<Integer> userVotes = pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)
                .map(PollVoteEntity::getSelectedOptions)
                .orElse(Collections.emptyList());
        boolean pollFinished = poll.getPollEndTime() != null && LocalDateTime.now().isAfter(poll.getPollEndTime());
        boolean hasUserVoted = pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId);
        return PollInfoResponseDTO.of(
                poll.getPollOptions(), poll.getPollEndTime(), poll.isMultipleChoice(),
                poll.getPollVotesCounts(), userVotes, totalParticipants, hasUserVoted, pollFinished);
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
}

