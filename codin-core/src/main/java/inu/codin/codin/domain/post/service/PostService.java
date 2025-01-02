package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollVoteRepository;
import inu.codin.codin.domain.post.domain.scrap.service.ScrapService;
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
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.RedisService;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.infra.s3.exception.ImageRemoveException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    private final S3Service s3Service;
    private final LikeService likeService;
    private final ScrapService scrapService;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;

    public void createPost(PostCreateRequestDTO postCreateRequestDTO, List<MultipartFile> postImages) {
        List<String> imageUrls = s3Service.handleImageUpload(postImages);
        ObjectId userId = SecurityUtils.getCurrentUserId();

        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER) &&
                postCreateRequestDTO.getPostCategory().toString().split("_")[0].equals("EXTRACURRICULAR")){
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
    }


    public void updatePostContent(String postId, PostContentUpdateRequestDTO requestDTO, List<MultipartFile> postImages) {

        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()->new NotFoundException("해당 게시물 없음"));
        validateUserAndPost(post);

        List<String> imageUrls = s3Service.handleImageUpload(postImages);

        post.updatePostContent(requestDTO.getContent(), imageUrls);
        postRepository.save(post);
    }

    public void updatePostAnonymous(String postId, PostAnonymousUpdateRequestDTO requestDTO) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()->new NotFoundException("해당 게시물 없음"));
        validateUserAndPost(post);

        post.updatePostAnonymous(requestDTO.isAnonymous());
        postRepository.save(post);
    }
    public void updatePostStatus(String postId, PostStatusUpdateRequestDTO requestDTO) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()->new NotFoundException("해당 게시물 없음"));
        validateUserAndPost(post);

        post.updatePostStatus(requestDTO.getPostStatus());
        postRepository.save(post);
    }
    private void validateUserAndPost(PostEntity post) {
        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER) &&
                post.getPostCategory().toString().split("_")[0].equals("EXTRACURRICULAR")){
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "비교과 게시글에 대한 권한이 없습니다.");
        }
        SecurityUtils.validateUser(post.getUserId());
    }


    // Post 정보를 처리하여 DTO를 생성하는 공통 메소드
    private PostDetailResponseDTO createPostDetailResponse(PostEntity post) {

        //Post 관련 인자 처리
        String nickname = post.isAnonymous() ? "익명" : getNicknameByUserId(post.getUserId());

        int likeCount = likeService.getLikeCount(LikeType.POST, post.get_id());
        int scrapCount = scrapService.getScrapCount(post.get_id());
        int hitsCount = redisService.getHitsCount(post.get_id());
        int commentCount = post.getCommentCount();

        UserInfo userInfo = getUserInfoAboutPost(post.get_id());

        // 투표 게시물 처리
        if (post.getPostCategory() == PostCategory.POLL) {
            PollEntity poll = pollRepository.findByPostId(post.get_id())
                    .orElseThrow(() -> new NotFoundException("투표 정보를 찾을 수 없습니다."));

            long totalParticipants = pollVoteRepository.countByPollId(poll.get_id());
            List<Integer> userVotes = pollVoteRepository.findByPollIdAndUserId(poll.get_id(), post.getUserId())
                    .map(PollVoteEntity::getSelectedOptions)
                    .orElse(Collections.emptyList());
            boolean pollFinished = poll.getPollEndTime() != null && LocalDateTime.now().isAfter(poll.getPollEndTime());
            boolean hasUserVoted = pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), post.getUserId());

            //투표 DTO 생성
            PostPollDetailResponseDTO.PollInfo pollInfo = PostPollDetailResponseDTO.PollInfo.of(poll.getPollOptions(), poll.getPollEndTime(), poll.isMultipleChoice(),
                    poll.getPollVotesCounts(), userVotes, totalParticipants, hasUserVoted, pollFinished);

            //게시물 + 투표 DTO 생성
            return PostPollDetailResponseDTO.of(
                    PostDetailResponseDTO.of(post, nickname, likeCount, scrapCount, hitsCount, commentCount, userInfo),
                    pollInfo
            );
        }

        // 일반 게시물 처리
        return PostDetailResponseDTO.of(post, nickname, likeCount, scrapCount, hitsCount, commentCount, userInfo);
    }

    // 모든 글 반환 ::  게시글 내용 + 댓글+대댓글의 수 + 좋아요,스크랩 count 수 반환
    public PostPageResponse getAllPosts(PostCategory postCategory, int pageNumber) {
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page;
        if (postCategory.equals(PostCategory.REQUEST) || postCategory.equals(PostCategory.COMMUNICATION) || postCategory.equals(PostCategory.EXTRACURRICULAR))
            page = postRepository.findByPostCategoryStartingWithOrderByCreatedAt(postCategory.toString(), pageRequest);
        else page = postRepository.findAllByCategoryOrderByCreatedAt(postCategory, pageRequest);
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
        if (redisService.validateHits(post.get_id(), userId))
            redisService.addHits(post.get_id(), userId);

        return createPostDetailResponse(post);
    }



    public void softDeletePost(String postId) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()-> new NotFoundException("게시물을 찾을 수 없음."));
        validateUserAndPost(post);

        post.delete();
        postRepository.save(post);

    }

    public void deletePostImage(String postId, String imageUrl) {
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("게시물을 찾을 수 없습니다."));
        validateUserAndPost(post);

        if (!post.getPostImageUrls().contains(imageUrl))
            throw new NotFoundException("이미지가 게시물에 존재하지 않습니다.");

        try {
            // S3에서 이미지 삭제
            s3Service.deleteFile(imageUrl);
            // 게시물의 이미지 리스트에서 제거
            post.removePostImage(imageUrl);
            postRepository.save(post);
        } catch (Exception e) {
            throw new ImageRemoveException("이미지 삭제 중 오류 발생: " + imageUrl);
        }
    }

    public UserInfo getUserInfoAboutPost(ObjectId postId){
        ObjectId userId = SecurityUtils.getCurrentUserId();
        return UserInfo.builder()
                .isLike(redisService.isPostLiked(postId, userId))
                .isScrap(redisService.isPostScraped(postId, userId))
                .build();
    }

    //user id 기반 nickname 반환
    public String getNicknameByUserId(ObjectId userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        return user.getNickname();
    }

}

