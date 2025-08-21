package inu.codin.codin.domain.post;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.best.BestEntity;
import inu.codin.codin.domain.post.domain.best.BestService;
import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import inu.codin.codin.domain.post.domain.poll.service.PollQueryService;
import inu.codin.codin.domain.post.dto.response.PostPageItemResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.dto.response.PollInfoResponseDTO;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.exception.PostException;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.service.PostInteractionService;
import inu.codin.codin.domain.post.service.PostQueryService;
import inu.codin.codin.domain.scrap.service.ScrapService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {
    
    @InjectMocks
    private PostQueryService postQueryService;
    
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private PollQueryService pollQueryService;
    @Mock private BlockService blockService;
    @Mock private PostInteractionService postInteractionService;
    @Mock private BestService bestService;
    @Mock private ScrapService scrapService;
    @Mock private LikeService likeService;
    @Mock private S3Service s3Service;
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
    void getAllPosts_카테고리별조회_성공() {
        // Given
        List<ObjectId> blockedUsers = Arrays.asList(new ObjectId(), new ObjectId());
        List<PostEntity> posts = Arrays.asList(createPostEntity(), createPostEntity());
        Page<PostEntity> page = new PageImpl<>(posts, PageRequest.of(0, 20), 2);
        
        given(blockService.getBlockedUsers()).willReturn(blockedUsers);
        given(postRepository.getPostsByCategoryWithBlockedUsers(anyString(), anyList(), any(PageRequest.class))).willReturn(page);
        given(userRepository.findById(any())).willReturn(Optional.of(createUserEntity()));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        mockUserInteractionServices();
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        
        // When
        PostPageResponse response = postQueryService.getAllPosts(PostCategory.COMMUNICATION, 0);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContents()).hasSize(2);
        verify(postRepository).getPostsByCategoryWithBlockedUsers(eq("COMMUNICATION"), eq(blockedUsers), any(PageRequest.class));
    }
    
    @Test
    void getAllPosts_빈결과_빈리스트반환() {
        // Given
        List<ObjectId> blockedUsers = new ArrayList<>();
        Page<PostEntity> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20), 0);
        
        given(blockService.getBlockedUsers()).willReturn(blockedUsers);
        given(postRepository.getPostsByCategoryWithBlockedUsers(anyString(), anyList(), any(PageRequest.class))).willReturn(emptyPage);
        
        // When
        PostPageResponse response = postQueryService.getAllPosts(PostCategory.COMMUNICATION, 0);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContents()).isEmpty();
    }
    
    @Test
    void getPostWithDetail_정상조회_성공() {
        // Given
        String postId = new ObjectId().toString();
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(userRepository.findById(any())).willReturn(Optional.of(createUserEntity()));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        mockUserInteractionServices();
        doNothing().when(postInteractionService).increaseHits(any(), any());
        
        // When
        PostPageItemResponseDTO response = postQueryService.getPostWithDetail(postId);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPost()).isNotNull();
        verify(postInteractionService).increaseHits(post, userId);
    }
    
    @Test
    void getPostWithDetail_게시물없음_예외() {
        // Given
        String postId = new ObjectId().toString();
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> postQueryService.getPostWithDetail(postId))
                .isInstanceOf(PostException.class);
    }
    
    @Test
    void getPostDetailById_정상조회_성공() {
        // Given
        ObjectId postId = new ObjectId();
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        
        given(postRepository.findByIdAndNotDeleted(postId)).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(userRepository.findById(any())).willReturn(Optional.of(createUserEntity()));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        mockUserInteractionServices();
        doNothing().when(postInteractionService).increaseHits(any(), any());
        
        // When
        Optional<PostPageItemResponseDTO> response = postQueryService.getPostDetailById(postId);
        
        // Then
        assertThat(response).isPresent();
        verify(postInteractionService).increaseHits(post, userId);
    }
    
    @Test
    void getPostDetailById_게시물없음_빈Optional() {
        // Given
        ObjectId postId = new ObjectId();
        
        given(postRepository.findByIdAndNotDeleted(postId)).willReturn(Optional.empty());
        
        // When
        Optional<PostPageItemResponseDTO> response = postQueryService.getPostDetailById(postId);
        
        // Then
        assertThat(response).isEmpty();
        verify(postInteractionService, never()).increaseHits(any(), any());
    }
    
    @Test
    void searchPosts_키워드검색_성공() {
        // Given
        String keyword = "테스트";
        List<ObjectId> blockedUsers = new ArrayList<>();
        List<PostEntity> posts = Arrays.asList(createPostEntity());
        Page<PostEntity> page = new PageImpl<>(posts, PageRequest.of(0, 20), 1);
        
        given(blockService.getBlockedUsers()).willReturn(blockedUsers);
        given(postRepository.findAllByKeywordAndDeletedAtIsNull(anyString(), anyList(), any(PageRequest.class))).willReturn(page);
        given(userRepository.findById(any())).willReturn(Optional.of(createUserEntity()));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        mockUserInteractionServices();
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        
        // When
        PostPageResponse response = postQueryService.searchPosts(keyword, 0);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContents()).hasSize(1);
        verify(postRepository).findAllByKeywordAndDeletedAtIsNull(eq(keyword), eq(blockedUsers), any(PageRequest.class));
    }
    
    @Test
    void getTop3BestPosts_정상조회_성공() {
        // Given
        List<String> bestPostIds = Arrays.asList(
            new ObjectId().toString(),
            new ObjectId().toString(),
            new ObjectId().toString()
        );
        
        given(bestService.getTop3BestPostIds()).willReturn(bestPostIds);
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(createPostEntity()));
        given(userRepository.findById(any())).willReturn(Optional.of(createUserEntity()));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        mockUserInteractionServices();
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        
        // When
        List<PostPageItemResponseDTO> response = postQueryService.getTop3BestPosts();
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(3);
    }
    
    @Test
    void getTop3BestPosts_존재하지않는게시물_자동삭제() {
        // Given
        String validPostId = new ObjectId().toString();
        String invalidPostId = new ObjectId().toString();
        List<String> bestPostIds = Arrays.asList(validPostId, invalidPostId);
        
        given(bestService.getTop3BestPostIds()).willReturn(bestPostIds);
        given(postRepository.findByIdAndNotDeleted(any()))
            .willReturn(Optional.of(createPostEntity()))  // 첫 번째 호출은 성공
            .willReturn(Optional.empty());               // 두 번째 호출은 실패
        given(userRepository.findById(any())).willReturn(Optional.of(createUserEntity()));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        mockUserInteractionServices();
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        doNothing().when(bestService).deleteBestPost(invalidPostId);
        
        // When
        List<PostPageItemResponseDTO> response = postQueryService.getTop3BestPosts();
        
        // Then
        assertThat(response).hasSize(1);
        verify(bestService).deleteBestPost(invalidPostId);
    }
    
    @Test
    void getBestPosts_페이징조회_성공() {
        // Given
        int pageNumber = 0;
        List<BestEntity> bestEntities = Arrays.asList(createBestEntity(), createBestEntity());
        Page<BestEntity> page = new PageImpl<>(bestEntities, PageRequest.of(0, 20), 2);
        
        given(bestService.getBestEntities(pageNumber)).willReturn(page);
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(createPostEntity()));
        given(userRepository.findById(any())).willReturn(Optional.of(createUserEntity()));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        mockUserInteractionServices();
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        
        // When
        PostPageResponse response = postQueryService.getBestPosts(pageNumber);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContents()).hasSize(2);
        verify(bestService).getBestEntities(pageNumber);
    }
    
    @Test
    void findPostById_정상조회_성공() {
        // Given
        ObjectId postId = new ObjectId();
        PostEntity post = createPostEntity();
        
        given(postRepository.findByIdAndNotDeleted(postId)).willReturn(Optional.of(post));
        
        // When
        PostEntity result = postQueryService.findPostById(postId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(post);
    }
    
    @Test
    void findPostById_게시물없음_예외() {
        // Given
        ObjectId postId = new ObjectId();
        
        given(postRepository.findByIdAndNotDeleted(postId)).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> postQueryService.findPostById(postId))
                .isInstanceOf(PostException.class);
    }
    
    @Test
    void getLikeCount_좋아요수조회_성공() {
        // Given
        PostEntity post = createPostEntity();
        int expectedCount = 5;
        
        given(likeService.getLikeCount(LikeType.POST, post.get_id().toString())).willReturn(expectedCount);
        
        // When
        int result = postQueryService.getLikeCount(post);
        
        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(likeService).getLikeCount(LikeType.POST, post.get_id().toString());
    }
    
    @Test
    void getScrapCount_스크랩수조회_성공() {
        // Given
        PostEntity post = createPostEntity();
        int expectedCount = 3;
        
        given(scrapService.getScrapCount(post.get_id())).willReturn(expectedCount);
        
        // When
        int result = postQueryService.getScrapCount(post);
        
        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(scrapService).getScrapCount(post.get_id());
    }
    
    @Test
    void getHitsCount_조회수조회_성공() {
        // Given
        PostEntity post = createPostEntity();
        int expectedCount = 10;
        
        given(hitsService.getHitsCount(post.get_id())).willReturn(expectedCount);
        
        // When
        int result = postQueryService.getHitsCount(post);
        
        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(hitsService).getHitsCount(post.get_id());
    }
    
    @Test
    void getUserAnonymousNumber_익명번호조회_성공() {
        // Given
        ObjectId userId = new ObjectId();
        PostAnonymous postAnonymous = mock(PostAnonymous.class);
        Integer expectedNumber = 1;
        
        given(postAnonymous.getAnonNumber(userId)).willReturn(expectedNumber);
        
        // When
        Integer result = postQueryService.getUserAnonymousNumber(postAnonymous, userId);
        
        // Then
        assertThat(result).isEqualTo(expectedNumber);
        verify(postAnonymous).getAnonNumber(userId);
    }
    
    @Test
    void toPageItemDTO_Poll게시물_PollInfo포함() {
        // Given
        PostEntity pollPost = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.POLL)
                .build();
        setIdField(pollPost, new ObjectId());
        ObjectId userId = new ObjectId();
        PollInfoResponseDTO pollInfo = mock(PollInfoResponseDTO.class);
        
        given(userRepository.findById(any())).willReturn(Optional.of(createUserEntity()));
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(pollQueryService.getPollInfo(pollPost, userId)).willReturn(pollInfo);
        mockUserInteractionServices();
        
        // When
        PostPageItemResponseDTO result = postQueryService.getPostListResponseDtos(Arrays.asList(pollPost)).get(0);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPoll()).isEqualTo(pollInfo);
        verify(pollQueryService).getPollInfo(pollPost, userId);
    }
    
    // Helper methods
    private void mockUserInteractionServices() {
        given(likeService.getLikeCount(any(), any())).willReturn(0);
        given(scrapService.getScrapCount(any())).willReturn(0);
        given(hitsService.getHitsCount(any())).willReturn(0);
        given(likeService.isLiked(any(),any(), (ObjectId) any())).willReturn(false);
        given(scrapService.isPostScraped(any(), any())).willReturn(false);
    }
    
    private PostEntity createPostEntity() {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .isAnonymous(false)
                .build();
        setIdField(post, new ObjectId());
        return post;
    }
    
    private void setIdField(PostEntity entity, ObjectId id) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("_id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID field", e);
        }
    }
    
    private UserEntity createUserEntity() {
        return UserEntity.builder()
                .nickname("테스트유저")
                .profileImageUrl("profile.jpg")
                .build();
    }
    
    private BestEntity createBestEntity() {
        return BestEntity.builder()
                .postId(new ObjectId())
                .build();
    }
}