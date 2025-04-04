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
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO.UserInfo;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.dto.response.PostPollDetailResponseDTO;
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
        List<String> imageUrls = s3Service.handleImageUpload(postImages);
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

        List<String> imageUrls = s3Service.handleImageUpload(postImages);

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




    // Post 정보를 처리하여 DTO를 생성하는 공통 메소드
    private PostDetailResponseDTO createPostDetailResponse(PostEntity post) {
        String nickname;
        String userImageUrl;

        UserEntity user = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        if (user.getDeletedAt() == null){
            if (post.isAnonymous()){
                nickname = "익명";
                userImageUrl = s3Service.getDefaultProfileImageUrl(); // S3Service에서 기본 이미지 URL 가져오기
            } else {
                nickname = user.getNickname();
                userImageUrl = user.getProfileImageUrl();
            }
        } else {
            nickname = user.getNickname();
            userImageUrl = user.getProfileImageUrl();
        }

        //Post 관련 인자 처리

        int likeCount = likeService.getLikeCount(LikeType.POST, post.get_id());
        int scrapCount = scrapService.getScrapCount(post.get_id());
        int hitsCount = hitsService.getHitsCount(post.get_id());
        int commentCount = post.getCommentCount();

        ObjectId userId = SecurityUtils.getCurrentUserId();

        UserInfo userInfo = getUserInfoAboutPost(userId, post.getUserId(), post.get_id());

        // 투표 게시물 처리
        if (post.getPostCategory() == PostCategory.POLL) {
            PollEntity poll = pollRepository.findByPostId(post.get_id())
                    .orElseThrow(() -> new NotFoundException("투표 정보를 찾을 수 없습니다."));

            long totalParticipants = pollVoteRepository.countByPollId(poll.get_id());
            List<Integer> userVotes = pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)
                    .map(PollVoteEntity::getSelectedOptions)
                    .orElse(Collections.emptyList());
            boolean pollFinished = poll.getPollEndTime() != null && LocalDateTime.now().isAfter(poll.getPollEndTime());
            boolean hasUserVoted = pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId);

            //투표 DTO 생성
            PostPollDetailResponseDTO.PollInfo pollInfo = PostPollDetailResponseDTO.PollInfo.of(poll.getPollOptions(), poll.getPollEndTime(), poll.isMultipleChoice(),
                    poll.getPollVotesCounts(), userVotes, totalParticipants, hasUserVoted, pollFinished);

            log.info("게시글-투표 상세정보 생성 성공. PostId: {}", post.get_id());

            //게시물 + 투표 DTO 생성
            return PostPollDetailResponseDTO.of(
                    PostDetailResponseDTO.of(post, nickname, userImageUrl, likeCount, scrapCount, hitsCount, commentCount, userInfo),
                    pollInfo
            );
        }
        // 일반 게시물 처리
        return PostDetailResponseDTO.of(post, nickname, userImageUrl, likeCount, scrapCount, hitsCount, commentCount ,userInfo);
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
        if (!hitsService.validateHits(post.get_id(), userId)) {
            hitsService.addHits(post.get_id(), userId);
            log.info("조회수 업데이트. PostId: {}, UserId: {}", post.get_id(), userId);
        }

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

        if (!post.getPostImageUrls().contains(imageUrl)) {
            log.error("게시물에 이미지 없음. PostId: {}, ImageUrl: {}", postId, imageUrl);
            throw new NotFoundException("이미지가 게시물에 존재하지 않습니다.");
        }
        try {
            // S3에서 이미지 삭제
            s3Service.deleteFile(imageUrl);
            // 게시물의 이미지 리스트에서 제거
            post.removePostImage(imageUrl);
            postRepository.save(post);
            log.info("이미지 삭제 성공. PostId: {}, ImageUrl: {}", postId, imageUrl);

        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생. PostId: {}, ImageUrl: {}", postId, imageUrl, e);
            throw new ImageRemoveException("이미지 삭제 중 오류 발생: " + imageUrl);
        }
    }

    public UserInfo getUserInfoAboutPost(ObjectId currentUserId, ObjectId postUserId, ObjectId postId){
        return UserInfo.builder()
                .isLike(likeService.isLiked(LikeType.POST, postId, currentUserId))
                .isScrap(scrapService.isPostScraped(postId, currentUserId))
                .isMine(postUserId.equals(currentUserId))
                .build();
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
        Map<String, Double> posts = redisBestService.getBests();
        List<PostEntity> bestPosts = posts.entrySet().stream()
                .map(post -> postRepository.findByIdAndNotDeleted(new ObjectId(post.getKey()))
                        .orElseGet(() -> {
                            redisBestService.deleteBest(post.getKey());
                            return null;
                        }))
                .filter(Objects::nonNull) // null 값 제거
                .toList();
        log.info("Top 3 베스트 게시물 반환.");
        return getPostListResponseDtos(bestPosts);
    }

    public PostPageResponse getBestPosts(int pageNumber) {
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<BestEntity> bests = bestRepository.findAll(pageRequest);
        Page<PostEntity> page = bests.map(bestEntity -> postRepository.findByIdAndNotDeleted(bestEntity.getPostId())
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다.")));
        return PostPageResponse.of(getPostListResponseDtos(page.getContent()), page.getTotalPages() - 1, page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1);
    }

    public Optional<PostDetailResponseDTO> getPostDetailById(ObjectId postId) {
        return postRepository.findById(postId)
                .map(this::createPostDetailResponse);
    }
}

