package inu.codin.codin.domain.post;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.dto.request.*;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.service.PostCommandService;
import inu.codin.codin.domain.post.service.PostInteractionService;
import inu.codin.codin.domain.post.service.PostQueryService;
import inu.codin.codin.domain.user.entity.UserRole;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {
    
    @InjectMocks
    private PostCommandService postCommandService;
    
    @Mock private PostRepository postRepository;
    @Mock private PostInteractionService postInteractionService;
    @Mock private PostQueryService postQueryService;
    
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
    void createPost_정상등록_성공() throws Exception {
        // Given
        PostCreateRequestDTO dto = createPostCreateRequestDTO("제목", "내용", true, PostCategory.COMMUNICATION);
        List<MultipartFile> images = new ArrayList<>();
        ObjectId userId = new ObjectId();
        
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        given(postInteractionService.handleImageUpload(any())).willReturn(new ArrayList<>());
        given(postRepository.save(any())).willAnswer(inv -> {
            PostEntity entity = inv.getArgument(0);
            setIdField(entity, new ObjectId());
            return entity;
        });
        
        // When & Then
        assertThatCode(() -> postCommandService.createPost(dto, images)).doesNotThrowAnyException();
        verify(postRepository).save(any(PostEntity.class));
    }
    
    @Test
    void createPost_비교과_권한없음_예외() throws Exception {
        // Given
        PostCreateRequestDTO dto = createPostCreateRequestDTO("제목", "내용", true, PostCategory.EXTRACURRICULAR);
        List<MultipartFile> images = new ArrayList<>();
        ObjectId userId = new ObjectId();
        
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.USER);
        
        // When & Then
        assertThatThrownBy(() -> postCommandService.createPost(dto, images))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("비교과 게시글에 대한 권한이 없습니다.");
    }
    
    @Test
    void updatePostContent_정상수정_성공() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PostContentUpdateRequestDTO dto = createPostContentUpdateRequestDTO("수정된 내용");
        List<MultipartFile> images = new ArrayList<>();
        PostEntity post = createPostEntity();
        List<String> imageUrls = Arrays.asList("image1.jpg", "image2.jpg");
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        given(postInteractionService.handleImageUpload(any())).willReturn(imageUrls);
        given(postRepository.save(any())).willReturn(post);
        
        // When & Then
        assertThatCode(() -> postCommandService.updatePostContent(postId, dto, images)).doesNotThrowAnyException();
        verify(postRepository).save(post);
    }
    
    @Test
    void updatePostAnonymous_정상수정_성공() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PostAnonymousUpdateRequestDTO dto = createPostAnonymousUpdateRequestDTO(true);
        PostEntity post = createPostEntity();
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        given(postRepository.save(any())).willReturn(post);
        
        // When & Then
        assertThatCode(() -> postCommandService.updatePostAnonymous(postId, dto)).doesNotThrowAnyException();
        verify(postRepository).save(post);
    }
    
    @Test
    void updatePostStatus_정상수정_성공() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PostStatusUpdateRequestDTO dto = createPostStatusUpdateRequestDTO(PostStatus.ACTIVE);
        PostEntity post = createPostEntity();
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        given(postRepository.save(any())).willReturn(post);
        
        // When & Then
        assertThatCode(() -> postCommandService.updatePostStatus(postId, dto)).doesNotThrowAnyException();
        verify(postRepository).save(post);
    }
    
    @Test
    void softDeletePost_정상삭제_성공() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PostEntity post = createPostEntity();
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        given(postRepository.save(any())).willReturn(post);
        
        // When & Then
        assertThatCode(() -> postCommandService.softDeletePost(postId)).doesNotThrowAnyException();
        verify(postRepository).save(post);
    }
    
    @Test
    void deletePostImage_정상삭제_성공() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        String imageUrl = "test-image.jpg";
        PostEntity post = createPostEntity();
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        doNothing().when(postInteractionService).deletePostImageInternal(any(), any());
        
        // When & Then
        assertThatCode(() -> postCommandService.deletePostImage(postId, imageUrl)).doesNotThrowAnyException();
        verify(postInteractionService).deletePostImageInternal(post, imageUrl);
    }
    
    @Test
    void handleCommentCreation_정상처리_성공() throws Exception {
        // Given
        PostEntity post = createPostEntityWithAnonymous();
        ObjectId userId = new ObjectId();
        
        given(postRepository.save(any())).willReturn(post);
        
        // When
        postCommandService.handleCommentCreation(post, userId);
        
        // Then
        verify(postRepository).save(post);
    }
    
    @Test
    void increaseCommentCount_댓글수증가_성공() throws Exception {
        // Given
        PostEntity post = createPostEntity();
        int initialCount = post.getCommentCount();
        
        // When
        postCommandService.increaseCommentCount(post);
        
        // Then
        assertThat(post.getCommentCount()).isEqualTo(initialCount + 1);
    }
    
    @Test
    void decreaseCommentCount_댓글수감소_성공() throws Exception {
        // Given
        PostEntity post = createPostEntityWithComments();
        int initialCount = post.getCommentCount();
        
        given(postRepository.save(any())).willReturn(post);
        
        // When
        postCommandService.decreaseCommentCount(post);
        
        // Then
        assertThat(post.getCommentCount()).isEqualTo(initialCount - 1);
        verify(postRepository).save(post);
    }
    
    @Test
    void assignAnonymousNumber_익명아님_처리안함() throws Exception {
        // Given
        PostEntity post = createPostEntity(); // anonymous = false
        ObjectId userId = new ObjectId();
        
        // When
        postCommandService.assignAnonymousNumber(post, userId);
        
        // Then
        verify(postRepository, never()).save(any());
    }
    
    @Test
    void assignAnonymousNumber_이미할당됨_처리안함() throws Exception {
        // Given
        PostEntity post = createPostEntityWithAnonymous();
        ObjectId userId = new ObjectId();
        PostAnonymous anonymous = post.getAnonymous();
        anonymous.setAnonNumber(userId); // 이미 할당된 상태로 설정
        
        // When
        postCommandService.assignAnonymousNumber(post, userId);
        
        // Then
        verify(postRepository, never()).save(any());
    }
    
    @Test
    void assignAnonymousNumber_작성자_익명할당_성공() throws Exception {
        // Given
        ObjectId userId = new ObjectId();
        PostEntity post = PostEntity.builder()
                .userId(userId)
                .postCategory(PostCategory.COMMUNICATION)
                .isAnonymous(true)
                .build();

        // When
        postCommandService.assignAnonymousNumber(post, userId);
        
        // Then
        assertThat(post.getAnonymous().hasAnonNumber(userId)).isEqualTo(true);
    }
    
    @Test
    void assignAnonymousNumber_일반사용자_익명번호할당_성공() throws Exception {
        // Given
        PostEntity post = createPostEntityWithAnonymous();
        ObjectId userId = new ObjectId();

        // When
        postCommandService.assignAnonymousNumber(post, userId);
        
        // Then
        assertThat(post.getAnonymous().hasAnonNumber(userId)).isEqualTo(true);

    }
    
    // Helper methods
    private PostCreateRequestDTO createPostCreateRequestDTO(String title, String content, boolean anonymous, PostCategory category) throws Exception {
        PostCreateRequestDTO dto = PostCreateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "title", title);
        setField(dto, "content", content);
        setField(dto, "anonymous", anonymous);
        setField(dto, "postCategory", category);
        return dto;
    }
    
    private PostContentUpdateRequestDTO createPostContentUpdateRequestDTO(String content) throws Exception {
        PostContentUpdateRequestDTO dto = PostContentUpdateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "content", content);
        return dto;
    }
    
    private PostAnonymousUpdateRequestDTO createPostAnonymousUpdateRequestDTO(boolean anonymous) throws Exception {
        PostAnonymousUpdateRequestDTO dto = PostAnonymousUpdateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "anonymous", anonymous);
        return dto;
    }
    
    private PostStatusUpdateRequestDTO createPostStatusUpdateRequestDTO(PostStatus status) throws Exception {
        PostStatusUpdateRequestDTO dto = PostStatusUpdateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "postStatus", status);
        return dto;
    }
    
    private void setField(Object target, String field, Object value) throws Exception {
        java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
    
    private void setIdField(PostEntity entity, ObjectId id) throws Exception {
        java.lang.reflect.Field idField = entity.getClass().getDeclaredField("_id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
    
    private PostEntity createPostEntity() {
        return PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .isAnonymous(false)
                .build();
    }
    
    private PostEntity createPostEntityWithAnonymous() {
        return PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .isAnonymous(true)
                .build();
    }
    
    private PostEntity createPostEntityWithComments() {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .build();
        post.plusCommentCount(); // 댓글 수를 1로 설정
        return post;
    }
}