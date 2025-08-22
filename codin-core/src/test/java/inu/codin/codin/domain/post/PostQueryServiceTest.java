package inu.codin.codin.domain.post;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.post.domain.best.BestEntity;
import inu.codin.codin.domain.post.domain.best.BestService;
import inu.codin.codin.domain.post.dto.response.PostPageItemResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.exception.PostException;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.service.PostInteractionService;
import inu.codin.codin.domain.post.service.PostQueryService;
import inu.codin.codin.domain.post.service.PostDtoAssembler;
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
    @Mock private BlockService blockService;
    @Mock private PostInteractionService postInteractionService;
    @Mock private BestService bestService;
    @Mock private PostDtoAssembler postDtoAssembler;
    
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
        
        List<PostPageItemResponseDTO> mockDtoList = Arrays.asList(createMockPostPageItemResponseDTO(), createMockPostPageItemResponseDTO());
        
        given(blockService.getBlockedUsers()).willReturn(blockedUsers);
        given(postRepository.getPostsByCategoryWithBlockedUsers(anyString(), anyList(), any(PageRequest.class))).willReturn(page);
        given(postDtoAssembler.toPageItemList(posts)).willReturn(mockDtoList);
        
        // When
        PostPageResponse response = postQueryService.getAllPosts(PostCategory.COMMUNICATION, 0);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContents()).hasSize(2);
        verify(postRepository).getPostsByCategoryWithBlockedUsers(eq("COMMUNICATION"), eq(blockedUsers), any(PageRequest.class));
        verify(postDtoAssembler).toPageItemList(posts);
    }
    
    @Test
    void getAllPosts_빈결과_빈리스트반환() {
        // Given
        List<ObjectId> blockedUsers = new ArrayList<>();
        List<PostEntity> emptyPosts = new ArrayList<>();
        Page<PostEntity> emptyPage = new PageImpl<>(emptyPosts, PageRequest.of(0, 20), 0);
        
        given(blockService.getBlockedUsers()).willReturn(blockedUsers);
        given(postRepository.getPostsByCategoryWithBlockedUsers(anyString(), anyList(), any(PageRequest.class))).willReturn(emptyPage);
        given(postDtoAssembler.toPageItemList(emptyPosts)).willReturn(new ArrayList<>());
        
        // When
        PostPageResponse response = postQueryService.getAllPosts(PostCategory.COMMUNICATION, 0);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContents()).isEmpty();
        verify(postDtoAssembler).toPageItemList(emptyPosts);
    }
    
    @Test
    void getPostWithDetail_정상조회_성공() {
        // Given
        String postId = new ObjectId().toString();
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        PostPageItemResponseDTO mockDto = createMockPostPageItemResponseDTO();
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(postDtoAssembler.toPageItem(post, userId)).willReturn(mockDto);
        doNothing().when(postInteractionService).increaseHits(any(), any());
        
        // When
        PostPageItemResponseDTO response = postQueryService.getPostWithDetail(postId);
        
        // Then
        assertThat(response).isNotNull();
        verify(postInteractionService).increaseHits(post, userId);
        verify(postDtoAssembler).toPageItem(post, userId);
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
        PostPageItemResponseDTO mockDto = createMockPostPageItemResponseDTO();
        
        given(postRepository.findByIdAndNotDeleted(postId)).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(postDtoAssembler.toPageItem(post, userId)).willReturn(mockDto);
        doNothing().when(postInteractionService).increaseHits(any(), any());
        
        // When
        Optional<PostPageItemResponseDTO> response = postQueryService.getPostDetailById(postId);
        
        // Then
        assertThat(response).isPresent();
        verify(postInteractionService).increaseHits(post, userId);
        verify(postDtoAssembler).toPageItem(post, userId);
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
        List<PostPageItemResponseDTO> mockDtoList = Arrays.asList(createMockPostPageItemResponseDTO());
        
        given(blockService.getBlockedUsers()).willReturn(blockedUsers);
        given(postRepository.findAllByKeywordAndDeletedAtIsNull(anyString(), anyList(), any(PageRequest.class))).willReturn(page);
        given(postDtoAssembler.toPageItemList(posts)).willReturn(mockDtoList);
        
        // When
        PostPageResponse response = postQueryService.searchPosts(keyword, 0);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContents()).hasSize(1);
        verify(postRepository).findAllByKeywordAndDeletedAtIsNull(eq(keyword), eq(blockedUsers), any(PageRequest.class));
        verify(postDtoAssembler).toPageItemList(posts);
    }
    
    @Test
    void getTop3BestPosts_정상조회_성공() {
        // Given
        List<String> bestPostIds = Arrays.asList(
            new ObjectId().toString(),
            new ObjectId().toString(),
            new ObjectId().toString()
        );
        List<PostPageItemResponseDTO> mockDtoList = Arrays.asList(
            createMockPostPageItemResponseDTO(), 
            createMockPostPageItemResponseDTO(), 
            createMockPostPageItemResponseDTO()
        );
        
        given(bestService.getTop3BestPostIds()).willReturn(bestPostIds);
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(createPostEntity()));
        given(postDtoAssembler.toPageItemList(anyList())).willReturn(mockDtoList);
        
        // When
        List<PostPageItemResponseDTO> response = postQueryService.getTop3BestPosts();
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(3);
        verify(postDtoAssembler).toPageItemList(anyList());
    }
    
    @Test
    void getTop3BestPosts_존재하지않는게시물_자동삭제() {
        // Given
        String validPostId = new ObjectId().toString();
        String invalidPostId = new ObjectId().toString();
        List<String> bestPostIds = Arrays.asList(validPostId, invalidPostId);
        
        List<PostPageItemResponseDTO> mockDtoList = Arrays.asList(createMockPostPageItemResponseDTO());
        
        given(bestService.getTop3BestPostIds()).willReturn(bestPostIds);
        given(postRepository.findByIdAndNotDeleted(any()))
            .willReturn(Optional.of(createPostEntity()))  // 첫 번째 호출은 성공
            .willReturn(Optional.empty());               // 두 번째 호출은 실패
        given(postDtoAssembler.toPageItemList(anyList())).willReturn(mockDtoList);
        doNothing().when(bestService).deleteBestPost(invalidPostId);
        
        // When
        List<PostPageItemResponseDTO> response = postQueryService.getTop3BestPosts();
        
        // Then
        assertThat(response).hasSize(1);
        verify(bestService).deleteBestPost(invalidPostId);
        verify(postDtoAssembler).toPageItemList(anyList());
    }
    
    @Test
    void getBestPosts_페이징조회_성공() {
        // Given
        int pageNumber = 0;
        List<BestEntity> bestEntities = Arrays.asList(createBestEntity(), createBestEntity());
        Page<BestEntity> page = new PageImpl<>(bestEntities, PageRequest.of(0, 20), 2);
        
        List<PostPageItemResponseDTO> mockDtoList = Arrays.asList(createMockPostPageItemResponseDTO(), createMockPostPageItemResponseDTO());
        
        given(bestService.getBestEntities(pageNumber)).willReturn(page);
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(createPostEntity()));
        given(postDtoAssembler.toPageItemList(anyList())).willReturn(mockDtoList);
        
        // When
        PostPageResponse response = postQueryService.getBestPosts(pageNumber);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContents()).hasSize(2);
        verify(bestService).getBestEntities(pageNumber);
        verify(postDtoAssembler).toPageItemList(anyList());
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
    
    
    // Helper methods
    private PostPageItemResponseDTO createMockPostPageItemResponseDTO() {
        return PostPageItemResponseDTO.of(null, null);
    }
    
    private PostEntity createPostEntity() {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .title("Test Post")
                .content("Test Content")
                .postStatus(PostStatus.ACTIVE)
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