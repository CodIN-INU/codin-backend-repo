package inu.codin.codin.domain.post;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import inu.codin.codin.domain.post.domain.poll.service.PollQueryService;
import inu.codin.codin.domain.post.dto.response.PostPageItemResponseDTO;
import inu.codin.codin.domain.post.dto.response.PollInfoResponseDTO;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.service.PostDtoAssembler;
import inu.codin.codin.domain.scrap.service.ScrapService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * PostDtoAssembler 단위 테스트
 * DTO 조립 로직의 다양한 케이스를 검증
 */
@ExtendWith(MockitoExtension.class)
class PostDtoAssemblerTest {

    @InjectMocks
    private PostDtoAssembler postDtoAssembler;

    @Mock private UserRepository userRepository;
    @Mock private LikeService likeService;
    @Mock private ScrapService scrapService;
    @Mock private S3Service s3Service;
    @Mock private PollQueryService pollQueryService;
    @Mock private HitsService hitsService;

    private static AutoCloseable securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        securityUtilsMock.close();
    }

    @Test
    void toPageItem_일반게시물_정상변환() {
        // Given
        PostEntity post = createNormalPostEntity();
        ObjectId currentUserId = new ObjectId();
        UserEntity user = createUserEntity();

        given(userRepository.findById(post.getUserId())).willReturn(Optional.of(user));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        given(likeService.getLikeCount(LikeType.POST, post.get_id().toString())).willReturn(5);
        given(scrapService.getScrapCount(post.get_id())).willReturn(3);
        given(hitsService.getHitsCount(post.get_id())).willReturn(100);
        given(likeService.isLiked(LikeType.POST, post.get_id().toString(), currentUserId)).willReturn(true);
        given(scrapService.isPostScraped(post.get_id(), currentUserId)).willReturn(false);

        // When
        PostPageItemResponseDTO result = postDtoAssembler.toPageItem(post, currentUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPost()).isNotNull();
        assertThat(result.getPoll()).isNull(); // 일반 게시물은 Poll 정보 없음
        verify(userRepository).findById(post.getUserId());
        verify(likeService).getLikeCount(LikeType.POST, post.get_id().toString());
        verify(scrapService).getScrapCount(post.get_id());
        verify(hitsService).getHitsCount(post.get_id());
    }

    @Test
    void toPageItem_투표게시물_Poll정보포함() {
        // Given
        PostEntity pollPost = createPollPostEntity();
        ObjectId currentUserId = new ObjectId();
        UserEntity user = createUserEntity();
        PollInfoResponseDTO pollInfo = mock(PollInfoResponseDTO.class);

        given(userRepository.findById(pollPost.getUserId())).willReturn(Optional.of(user));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        given(likeService.getLikeCount(LikeType.POST, pollPost.get_id().toString())).willReturn(2);
        given(scrapService.getScrapCount(pollPost.get_id())).willReturn(1);
        given(hitsService.getHitsCount(pollPost.get_id())).willReturn(50);
        given(likeService.isLiked(LikeType.POST, pollPost.get_id().toString(), currentUserId)).willReturn(false);
        given(scrapService.isPostScraped(pollPost.get_id(), currentUserId)).willReturn(true);
        given(pollQueryService.getPollInfo(pollPost, currentUserId)).willReturn(pollInfo);

        // When
        PostPageItemResponseDTO result = postDtoAssembler.toPageItem(pollPost, currentUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPost()).isNotNull();
        assertThat(result.getPoll()).isEqualTo(pollInfo); // Poll 정보 포함
        verify(pollQueryService).getPollInfo(pollPost, currentUserId);
    }

    @Test
    void toPageItem_익명게시물_익명처리() {
        // Given
        PostEntity anonymousPost = createAnonymousPostEntity();
        ObjectId currentUserId = new ObjectId();
        UserEntity user = createUserEntity();

        given(userRepository.findById(anonymousPost.getUserId())).willReturn(Optional.of(user));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        given(likeService.getLikeCount(LikeType.POST, anonymousPost.get_id().toString())).willReturn(0);
        given(scrapService.getScrapCount(anonymousPost.get_id())).willReturn(0);
        given(hitsService.getHitsCount(anonymousPost.get_id())).willReturn(10);
        given(likeService.isLiked(LikeType.POST, anonymousPost.get_id().toString(), currentUserId)).willReturn(false);
        given(scrapService.isPostScraped(anonymousPost.get_id(), currentUserId)).willReturn(false);

        // When
        PostPageItemResponseDTO result = postDtoAssembler.toPageItem(anonymousPost, currentUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPost()).isNotNull();
        // UserDto.forPost에서 익명 처리 로직이 작동하는지는 UserDto 테스트에서 확인
        verify(userRepository).findById(anonymousPost.getUserId());
    }

    @Test
    void toPageItem_삭제된사용자_예외발생() {
        // Given
        PostEntity post = createNormalPostEntity();
        ObjectId currentUserId = new ObjectId();

        given(userRepository.findById(post.getUserId())).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postDtoAssembler.toPageItem(post, currentUserId))
                .isInstanceOf(RuntimeException.class); // PostException이 발생해야 함
        verify(userRepository).findById(post.getUserId());
    }

    @Test
    void toPageItemList_여러게시물_정상변환() {
        // Given
        PostEntity post1 = createNormalPostEntity();
        PostEntity post2 = createPollPostEntity();
        List<PostEntity> posts = Arrays.asList(post1, post2);
        UserEntity user = createUserEntity();

        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        given(likeService.getLikeCount(any(), any())).willReturn(0);
        given(scrapService.getScrapCount(any())).willReturn(0);
        given(hitsService.getHitsCount(any())).willReturn(0);
        given(likeService.isLiked(any(), any(), (ObjectId) any())).willReturn(false);
        given(scrapService.isPostScraped(any(), any())).willReturn(false);
        given(pollQueryService.getPollInfo(eq(post2), any())).willReturn(mock(PollInfoResponseDTO.class));

        // When
        List<PostPageItemResponseDTO> results = postDtoAssembler.toPageItemList(posts);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isNotNull();
        assertThat(results.get(1)).isNotNull();
        assertThat(results.get(0).getPoll()).isNull(); // 일반 게시물
        assertThat(results.get(1).getPoll()).isNotNull(); // 투표 게시물
    }

    // Helper methods
    private PostEntity createNormalPostEntity() {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .title("Test Post")
                .content("Test Content")
                .postStatus(PostStatus.ACTIVE)
                .isAnonymous(false)
                .build();
        setIdFieldSafely(post, new ObjectId());
        return post;
    }

    private PostEntity createPollPostEntity() {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.POLL)
                .title("Test Poll")
                .content("Test Poll Content")
                .postStatus(PostStatus.ACTIVE)
                .isAnonymous(false)
                .build();
        setIdFieldSafely(post, new ObjectId());
        return post;
    }

    private PostEntity createAnonymousPostEntity() {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .title("Anonymous Post")
                .content("Anonymous Content")
                .postStatus(PostStatus.ACTIVE)
                .isAnonymous(true)
                .build();
        setIdFieldSafely(post, new ObjectId());
        return post;
    }

    private UserEntity createUserEntity() {
        return UserEntity.builder()
                .nickname("testuser")
                .build();
    }

    private void setIdFieldSafely(Object entity, ObjectId id) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("_id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID field", e);
        }
    }
}