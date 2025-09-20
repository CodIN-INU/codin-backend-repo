package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.post.domain.best.BestEntity;
import inu.codin.codin.domain.post.domain.best.BestService;
import inu.codin.codin.domain.post.dto.response.PostPageItemResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.exception.PostException;
import inu.codin.codin.domain.post.exception.PostErrorCode;
import inu.codin.codin.domain.post.repository.PostRepository;
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
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryService
{
    private final PostRepository postRepository;
    private final BlockService blockService;
    private final PostInteractionService postInteractionService;
    private final BestService bestService;
    private final PostDtoAssembler postDtoAssembler;
    /**
     * 카테고리별 삭제되지 않은 게시물 목록 조회
     * @return PostPageResponse (불변 리스트)
     */
    public PostPageResponse getAllPosts(PostCategory postCategory, int pageNumber) {
        List<ObjectId> blockedUsersId = blockService.getBlockedUsers();
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page = postRepository.getPostsByCategoryWithBlockedUsers(postCategory.toString(), blockedUsersId, pageRequest);
        log.info("모든 글 반환 성공 Category: {}, Page: {}", postCategory, pageNumber);
        return PostPageResponse.of(postDtoAssembler.toPageItemList(page.getContent()), page.getTotalPages() - 1, page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1);
    }


    /**
     * 게시물 상세 조회
     */
    public PostPageItemResponseDTO getPostWithDetail(String postId) {
        PostEntity post = findPostById(ObjectIdUtil.toObjectId(postId));
        ObjectId userId = SecurityUtils.getCurrentUserId();
        postInteractionService.increaseHits(post, userId);
        return postDtoAssembler.toPageItem(post, userId);
    }

    /**
     * Optional로 게시물 상세 조회 (null-safe)
     */
    public Optional<PostPageItemResponseDTO> getPostDetailById(ObjectId postId) {
        return postRepository.findByIdAndNotDeleted(postId)
                .map(post -> {
                    ObjectId userId = SecurityUtils.getCurrentUserId();
                    postInteractionService.increaseHits(post, userId);
                    return postDtoAssembler.toPageItem(post, userId);
                });
    }

    /**
     * 키워드 기반 게시물 검색
     */
    public PostPageResponse searchPosts(String keyword, int pageNumber) {
        List<ObjectId> blockedUsersId = blockService.getBlockedUsers();
        log.info("blockedUsersId: {}", blockedUsersId.size());

        String pattern = Pattern.quote(keyword);

        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page = postRepository.findAllByKeywordAndDeletedAtIsNull(pattern, blockedUsersId, pageRequest);
        log.info("키워드 기반 게시물 검색: {}, Page: {}", pattern, pageNumber);
        return PostPageResponse.of(postDtoAssembler.toPageItemList(page.getContent()), page.getTotalPages() - 1, page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1);
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
        return postDtoAssembler.toPageItemList(validPosts);
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
                postDtoAssembler.toPageItemList(validPosts),
                bestEntities.getTotalPages() - 1,
                bestEntities.hasNext() ? bestEntities.getPageable().getPageNumber() + 1 : -1
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


}
