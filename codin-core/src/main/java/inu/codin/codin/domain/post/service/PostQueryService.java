package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.best.BestEntity;
import inu.codin.codin.domain.post.domain.best.BestService;
import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import inu.codin.codin.domain.post.domain.poll.service.PollQueryService;
import inu.codin.codin.domain.post.dto.UserDto;
import inu.codin.codin.domain.post.dto.UserInfo;
import inu.codin.codin.domain.post.dto.response.PollInfoResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostPageItemResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.exception.PostException;
import inu.codin.codin.domain.post.exception.PostErrorCode;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.scrap.service.ScrapService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryService
{
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final PollQueryService pollQueryService;
    private final BlockService blockService;
    private final PostInteractionService postInteractionService;
    private final BestService bestService;
    private final ScrapService scrapService;
    private final LikeService likeService;
    private final S3Service s3Service;
    private final HitsService hitsService;
    /**
     * 카테고리별 삭제되지 않은 게시물 목록 조회
     * @return PostPageResponse (불변 리스트)
     */
    public PostPageResponse getAllPosts(PostCategory postCategory, int pageNumber) {
        List<ObjectId> blockedUsersId = blockService.getBlockedUsers();
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page = postRepository.getPostsByCategoryWithBlockedUsers(postCategory.toString(), blockedUsersId, pageRequest);
        log.info("모든 글 반환 성공 Category: {}, Page: {}", postCategory, pageNumber);
        return PostPageResponse.of(getPostListResponseDtos(page.getContent()), page.getTotalPages() - 1, page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1);
    }

    /**
     * 게시물 리스트 DTO 변환 (불변 리스트)
     */
    public List<PostPageItemResponseDTO> getPostListResponseDtos(List<PostEntity> posts) {
        return posts.stream()
                .map(this::toPageItemDTO)
                .toList();
    }

    /**
     * 게시물 상세 조회
     */
    public PostPageItemResponseDTO getPostWithDetail(String postId) {
        PostEntity post = findPostById(ObjectIdUtil.toObjectId(postId));
        ObjectId userId = SecurityUtils.getCurrentUserId();
        postInteractionService.increaseHits(post, userId);
        return toPageItemDTO(post);
    }

    /**
     * Optional로 게시물 상세 조회 (null-safe)
     */
    public Optional<PostPageItemResponseDTO> getPostDetailById(ObjectId postId) {
        return postRepository.findByIdAndNotDeleted(postId)
                .map(post -> {
                    ObjectId userId = SecurityUtils.getCurrentUserId();
                    postInteractionService.increaseHits(post, userId);
                    return toPageItemDTO(post);
                });
    }

    /**
     * 키워드 기반 게시물 검색
     */
    public PostPageResponse searchPosts(String keyword, int pageNumber) {
        List<ObjectId> blockedUsersId = blockService.getBlockedUsers();
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page = postRepository.findAllByKeywordAndDeletedAtIsNull(keyword, blockedUsersId, pageRequest);
        log.info("키워드 기반 게시물 검색: {}, Page: {}", keyword, pageNumber);
        return PostPageResponse.of(getPostListResponseDtos(page.getContent()), page.getTotalPages() - 1, page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1);
    }

    /**
     * Top 3 베스트 게시물 조회 (불변 리스트)
     */
    public List<PostPageItemResponseDTO> getTop3BestPosts() {
        List<String> bestPostIds = bestService.getTop3BestPostIds();

        List<PostEntity> validPosts = bestPostIds.stream()
                .map(postId -> {
                    try {
                        return findPostById(ObjectIdUtil.toObjectId(postId));
                    } catch (PostException e) {
                        bestService.deleteBestPost(postId); // 검증 실패시 삭제
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        log.info("Top 3 베스트 게시물 반환.");
        return getPostListResponseDtos(validPosts);
    }

    /**
     * 베스트 게시물 페이지 조회
     */
    public PostPageResponse getBestPosts(int pageNumber) {
        Page<BestEntity> bestEntities = bestService.getBestEntities(pageNumber);

        List<PostEntity> validPosts = bestEntities.getContent().stream()
                .map(bestEntity -> {
                    try {
                        return findPostById(bestEntity.getPostId());
                    } catch (PostException e) {
                        bestService.deleteBestPost(bestEntity.getPostId().toString());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        return PostPageResponse.of(
                getPostListResponseDtos(validPosts),
                bestEntities.getTotalPages() - 1,
                bestEntities.hasNext() ? bestEntities.getPageable().getPageNumber() + 1 : -1
        );
    }


    // PostEntity → PostPageItemResponseDTO 변환 (공통 변환 로직)
    private PostPageItemResponseDTO toPageItemDTO(PostEntity post) {
        UserDto userDto = resolveUserProfile(post);
        int likeCount = getLikeCount(post);
        int scrapCount = getScrapCount(post);
        int hitsCount = getHitsCount(post);
        int commentCount = post.getCommentCount();
        ObjectId userId = SecurityUtils.getCurrentUserId();
        UserInfo userInfo = getUserInfoAboutPost(userId, post.getUserId(), post.get_id());
        PostDetailResponseDTO postDTO = PostDetailResponseDTO.of(post, userDto, likeCount, scrapCount, hitsCount, commentCount, userInfo);
        if (post.getPostCategory() == PostCategory.POLL) {
            PollInfoResponseDTO pollInfo = pollQueryService.getPollInfo(post, userId);
            return PostPageItemResponseDTO.of(postDTO, pollInfo);
        } else {
            return PostPageItemResponseDTO.of(postDTO, null);
        }
    }

    // [유저 프로필] - 닉네임/이미지 결정
    private UserDto resolveUserProfile(PostEntity post) {
        UserEntity user = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new PostException(PostErrorCode.USER_NOT_FOUND));
        return UserDto.forPost(post, user, s3Service.getDefaultProfileImageUrl());
    }

    // [유저 프로필] - 게시물에 대한 유저정보 추출
    private UserInfo getUserInfoAboutPost(ObjectId currentUserId, ObjectId postUserId, ObjectId postId){
        return UserInfo.ofPost(
                likeService.isLiked(LikeType.POST, postId.toString(), currentUserId),
                scrapService.isPostScraped(postId, currentUserId),
                postUserId.equals(currentUserId)
        );
    }


    /**
     * 유저의 익명 번호 조회
     */
    public Integer getUserAnonymousNumber(PostAnonymous postAnonymous, ObjectId userId) {
        return postAnonymous.getAnonNumber(userId);
    }

    /**
     *
     * @param postId
     * @return validated PostEntity
     */
    public PostEntity findPostById(ObjectId postId) {
        return postRepository.findByIdAndNotDeleted(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
    }


    // [likeService] - 게시글 좋아요 수 조회
    public int getLikeCount(PostEntity post) {
        return likeService.getLikeCount(LikeType.POST, post.get_id().toString());
    }

    // [ScrapService] - 게시글 스크랩 수 조회
    public int getScrapCount(PostEntity post) {
        return scrapService.getScrapCount(post.get_id());
    }

    // [HitsService] - 게시글 조회수 조회
    public int getHitsCount(PostEntity post) {
        return hitsService.getHitsCount(post.get_id());
    }




}
