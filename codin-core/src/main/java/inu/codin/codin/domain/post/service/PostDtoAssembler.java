package inu.codin.codin.domain.post.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import inu.codin.codin.domain.post.domain.poll.service.PollQueryService;
import inu.codin.codin.domain.post.dto.UserDto;
import inu.codin.codin.domain.post.dto.UserInfo;
import inu.codin.codin.domain.post.dto.response.PollInfoResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostPageItemResponseDTO;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.exception.PostErrorCode;
import inu.codin.codin.domain.post.exception.PostException;
import inu.codin.codin.domain.scrap.service.ScrapService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * PostEntity를 다양한 Response DTO로 변환하는 책임을 담당하는 어셈블러
 * CQRS의 Query 측면에서 DTO 조립 로직을 분리하여 단일 책임 원칙을 준수
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostDtoAssembler {

    private final UserRepository userRepository;
    private final LikeService likeService;
    private final ScrapService scrapService;
    private final S3Service s3Service;
    private final PollQueryService pollQueryService;
    private final HitsService hitsService;

    /**
     * PostEntity를 PostPageItemResponseDTO로 변환
     * 현재 사용자 기준으로 좋아요/스크랩 상태, Poll 정보 등을 포함한 완전한 DTO 생성
     */
    public PostPageItemResponseDTO toPageItem(PostEntity post, ObjectId currentUserId) {
        UserDto userDto = resolveUserProfile(post);
        int likeCount = getLikeCount(post);
        int scrapCount = getScrapCount(post);
        int hitsCount = getHitsCount(post);
        int commentCount = post.getCommentCount();
        UserInfo userInfo = getUserInfoAboutPost(currentUserId, post.getUserId(), post.get_id());
        
        PostDetailResponseDTO postDTO = PostDetailResponseDTO.of(
            post, userDto, likeCount, scrapCount, hitsCount, commentCount, userInfo
        );
        
        if (post.getPostCategory() == PostCategory.POLL) {
            PollInfoResponseDTO pollInfo = pollQueryService.getPollInfo(post, currentUserId);
            return PostPageItemResponseDTO.of(postDTO, pollInfo);
        } else {
            return PostPageItemResponseDTO.of(postDTO, null);
        }
    }

    /**
     * PostEntity 리스트를 PostPageItemResponseDTO 리스트로 변환
     */
    public List<PostPageItemResponseDTO> toPageItemList(List<PostEntity> posts) {
        ObjectId currentUserId = SecurityUtils.getCurrentUserIdOrNull();
        return posts.stream()
                .map(post -> toPageItem(post, currentUserId))
                .toList();
    }

    /**
     * 사용자 프로필 정보 결정 (익명/실명, 닉네임/이미지)
     */
    private UserDto resolveUserProfile(PostEntity post) {
        UserEntity user = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new PostException(PostErrorCode.USER_NOT_FOUND));
        return UserDto.forPost(post, user, s3Service.getDefaultProfileImageUrl());
    }

    /**
     * 현재 사용자의 게시물에 대한 상호작용 정보 조회 (좋아요, 스크랩, 작성자 여부)
     * - 익명(비로그인)인 경우: liked=false, scraped=false, isOwner=false
     */
    private UserInfo getUserInfoAboutPost(ObjectId currentUserId, ObjectId postUserId, ObjectId postId) {
        boolean liked   = (currentUserId != null) && likeService.isLiked(LikeType.POST, postId.toString(), currentUserId);
        boolean scraped = (currentUserId != null) && scrapService.isPostScraped(postId, currentUserId);
        boolean isOwner = Objects.equals(postUserId, currentUserId);
        return UserInfo.ofPost(liked, scraped, isOwner);
    }

    /**
     * 게시글 좋아요 수 조회
     */
    private int getLikeCount(PostEntity post) {
        return likeService.getLikeCount(LikeType.POST, post.get_id().toString());
    }

    /**
     * 게시글 스크랩 수 조회
     */
    private int getScrapCount(PostEntity post) {
        return scrapService.getScrapCount(post.get_id());
    }

    /**
     * 게시글 조회수 조회
     */
    private int getHitsCount(PostEntity post) {
        return hitsService.getHitsCount(post.get_id());
    }
}