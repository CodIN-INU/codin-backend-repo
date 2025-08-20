package inu.codin.codin.domain.post.domain.comment;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.best.BestService;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentCreateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.comment.service.CommentCommandService;
import inu.codin.codin.domain.post.domain.comment.service.CommentQueryService;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.service.PostCommandService;
import inu.codin.codin.domain.post.service.PostQueryService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CommentCommandServiceTest {
    
    @InjectMocks
    private CommentCommandService commentCommandService;
    
    @Mock private CommentRepository commentRepository;
    @Mock private NotificationService notificationService;
    @Mock private PostCommandService postCommandService;
    @Mock private PostQueryService postQueryService;
    @Mock private CommentQueryService commentQueryService;
    @Mock private BestService bestService;
    
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
    void addComment_정상생성_성공() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        CommentCreateRequestDTO dto = createCommentCreateRequestDTO("댓글 내용", false);
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(commentRepository.save(any())).willAnswer(inv -> {
            CommentEntity entity = inv.getArgument(0);
            setIdField(entity, new ObjectId());
            return entity;
        });
        doNothing().when(postCommandService).handleCommentCreation(any(), any());
        doNothing().when(bestService).applyBestScore(any());
        doNothing().when(notificationService).sendNotificationMessageByComment(any(), any(), any(), any());
        
        // When & Then
        assertThatCode(() -> commentCommandService.addComment(postId, dto)).doesNotThrowAnyException();
        verify(commentRepository).save(any(CommentEntity.class));
        verify(postCommandService).handleCommentCreation(post, userId);
        verify(bestService).applyBestScore(any());
    }
    
    @Test
    void addComment_본인게시물_알림미발송() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        CommentCreateRequestDTO dto = createCommentCreateRequestDTO("댓글 내용", false);
        ObjectId userId = new ObjectId();
        PostEntity post = PostEntity.builder()
                .userId(userId) // 댓글 작성자와 동일한 userId
                .postCategory(PostCategory.COMMUNICATION)
                .build();
        setIdField(post, new ObjectId());
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(commentRepository.save(any())).willAnswer(inv -> {
            CommentEntity entity = inv.getArgument(0);
            setIdField(entity, new ObjectId());
            return entity;
        });
        doNothing().when(postCommandService).handleCommentCreation(any(), any());
        doNothing().when(bestService).applyBestScore(any());
        
        // When
        commentCommandService.addComment(postId, dto);
        
        // Then
        verify(notificationService, never()).sendNotificationMessageByComment(any(), any(), any(), any());
    }
    
    @Test
    void addComment_다른사용자게시물_알림발송() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        CommentCreateRequestDTO dto = createCommentCreateRequestDTO("댓글 내용", false);
        ObjectId userId = new ObjectId();
        ObjectId postOwner = new ObjectId(); // 다른 사용자
        PostEntity post = PostEntity.builder()
                .userId(postOwner)
                .postCategory(PostCategory.COMMUNICATION)
                .build();
        setIdField(post, new ObjectId());
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(commentRepository.save(any())).willAnswer(inv -> {
            CommentEntity entity = inv.getArgument(0);
            setIdField(entity, new ObjectId());
            return entity;
        });
        doNothing().when(postCommandService).handleCommentCreation(any(), any());
        doNothing().when(bestService).applyBestScore(any());
        doNothing().when(notificationService).sendNotificationMessageByComment(any(), any(), any(), any());
        
        // When
        commentCommandService.addComment(postId, dto);
        
        // Then
        verify(notificationService).sendNotificationMessageByComment(
                eq(post.getPostCategory()),
                eq(postOwner),
                eq(post.get_id().toString()),
                eq(dto.getContent())
        );
    }
    
    @Test
    void updateComment_정상수정_성공() throws Exception {
        // Given
        String commentId = new ObjectId().toString();
        CommentUpdateRequestDTO dto = createCommentUpdateRequestDTO("수정된 내용");
        CommentEntity comment = createCommentEntity();
        ObjectId userId = new ObjectId();
        
        given(commentQueryService.findCommentById(any())).willReturn(comment);
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        doNothing().when(SecurityUtils.class);
        SecurityUtils.validateUser(userId);
        given(commentRepository.save(any())).willReturn(comment);
        
        // When & Then
        assertThatCode(() -> commentCommandService.updateComment(commentId, dto)).doesNotThrowAnyException();
        verify(commentRepository).save(comment);
    }
    
    @Test
    void softDeleteComment_정상삭제_성공() throws Exception {
        // Given
        String commentId = new ObjectId().toString();
        CommentEntity comment = createCommentEntity();
        PostEntity post = createPostEntity();
        ObjectId userId = comment.getUserId();
        
        given(commentQueryService.findCommentById(any())).willReturn(comment);
        doNothing().when(SecurityUtils.class);
        SecurityUtils.validateUser(userId);
        given(postQueryService.findPostById(comment.getPostId())).willReturn(post);
        given(commentRepository.save(any())).willReturn(comment);
        doNothing().when(postCommandService).decreaseCommentCount(any());
        
        // When & Then
        assertThatCode(() -> commentCommandService.softDeleteComment(commentId)).doesNotThrowAnyException();
        verify(commentRepository).save(comment);
        verify(postCommandService).decreaseCommentCount(post);
    }
    
    // Helper methods
    private CommentCreateRequestDTO createCommentCreateRequestDTO(String content, boolean anonymous) throws Exception {
        CommentCreateRequestDTO dto = CommentCreateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "content", content);
        setField(dto, "anonymous", anonymous);
        return dto;
    }
    
    private CommentUpdateRequestDTO createCommentUpdateRequestDTO(String content) throws Exception {
        CommentUpdateRequestDTO dto = CommentUpdateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "content", content);
        return dto;
    }
    
    private void setField(Object target, String field, Object value) throws Exception {
        java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
    
    private void setIdField(Object entity, ObjectId id) throws Exception {
        java.lang.reflect.Field idField = entity.getClass().getDeclaredField("_id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
    
    private PostEntity createPostEntity() {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .build();
        setIdFieldSafely(post, new ObjectId());
        return post;
    }
    
    private CommentEntity createCommentEntity() {
        CommentEntity comment = CommentEntity.builder()
                .postId(new ObjectId())
                .userId(new ObjectId())
                .content("테스트 댓글")
                .anonymous(true)
                .build();
        setIdFieldSafely(comment, new ObjectId());
        return comment;
    }
    
    private void setIdFieldSafely(Object entity, ObjectId id) {
        try {
            setIdField(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID field", e);
        }
    }
}