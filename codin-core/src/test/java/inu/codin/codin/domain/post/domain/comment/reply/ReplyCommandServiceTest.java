package inu.codin.codin.domain.post.domain.comment.reply;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.best.BestService;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.service.CommentQueryService;
import inu.codin.codin.domain.post.domain.comment.reply.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.reply.dto.request.ReplyUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.comment.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.domain.comment.reply.service.ReplyCommandService;
import inu.codin.codin.domain.post.domain.comment.reply.service.ReplyQueryService;
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
class ReplyCommandServiceTest {
    
    @InjectMocks
    private ReplyCommandService replyCommandService;
    
    @Mock private ReplyCommentRepository replyCommentRepository;
    @Mock private PostCommandService postCommandService;
    @Mock private PostQueryService postQueryService;
    @Mock private NotificationService notificationService;
    @Mock private BestService bestService;
    @Mock private CommentQueryService commentQueryService;
    @Mock private ReplyQueryService replyQueryService;
    
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
    void addReply_정상생성_성공() throws Exception {
        // Given
        String commentId = new ObjectId().toString();
        ReplyCreateRequestDTO dto = createReplyCreateRequestDTO("대댓글 내용", false);
        CommentEntity comment = createCommentEntity();
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        ReplyCommentEntity reply = createReplyEntity();
        
        given(commentQueryService.findCommentById(any())).willReturn(comment);
        given(postQueryService.findPostById(comment.getPostId())).willReturn(post);
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(replyCommentRepository.save(any())).willAnswer(inv -> {
            ReplyCommentEntity entity = inv.getArgument(0);
            setIdField(entity, new ObjectId());
            return entity;
        });
        doNothing().when(postCommandService).handleCommentCreation(any(), any());
        doNothing().when(bestService).applyBestScore(any());
        doNothing().when(notificationService).sendNotificationMessageByReply(any(), any(), any(), any());
        
        // When & Then
        assertThatCode(() -> replyCommandService.addReply(commentId, dto)).doesNotThrowAnyException();
        verify(replyCommentRepository).save(any(ReplyCommentEntity.class));
        verify(postCommandService).handleCommentCreation(post, userId);
        verify(bestService).applyBestScore(post.get_id());
    }
    
    @Test
    void addReply_본인게시물_알림미발송() throws Exception {
        // Given
        String commentId = new ObjectId().toString();
        ReplyCreateRequestDTO dto = createReplyCreateRequestDTO("대댓글 내용", false);
        ObjectId userId = new ObjectId();
        CommentEntity comment = createCommentEntity();
        PostEntity post = PostEntity.builder()
                .userId(userId) // 대댓글 작성자와 동일한 userId
                .postCategory(PostCategory.COMMUNICATION)
                .build();
        setIdField(post, new ObjectId());
        
        given(commentQueryService.findCommentById(any())).willReturn(comment);
        given(postQueryService.findPostById(comment.getPostId())).willReturn(post);
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(replyCommentRepository.save(any())).willAnswer(inv -> {
            ReplyCommentEntity entity = inv.getArgument(0);
            setIdField(entity, new ObjectId());
            return entity;
        });
        doNothing().when(postCommandService).handleCommentCreation(any(), any());
        doNothing().when(bestService).applyBestScore(any());
        
        // When
        replyCommandService.addReply(commentId, dto);
        
        // Then
        verify(notificationService, never()).sendNotificationMessageByReply(any(), any(), any(), any());
    }
    
    @Test
    void addReply_다른사용자게시물_알림발송() throws Exception {
        // Given
        String commentId = new ObjectId().toString();
        ReplyCreateRequestDTO dto = createReplyCreateRequestDTO("대댓글 내용", false);
        ObjectId userId = new ObjectId();
        ObjectId postOwner = new ObjectId(); // 다른 사용자
        ObjectId commentOwner = new ObjectId(); // 댓글 작성자
        CommentEntity comment = CommentEntity.builder()
                .postId(new ObjectId())
                .userId(commentOwner)
                .content("원댓글")
                .anonymous(false)
                .build();
        setIdField(comment, new ObjectId());
        PostEntity post = PostEntity.builder()
                .userId(postOwner)
                .postCategory(PostCategory.COMMUNICATION)
                .build();
        setIdField(post, new ObjectId());
        
        given(commentQueryService.findCommentById(any())).willReturn(comment);
        given(postQueryService.findPostById(comment.getPostId())).willReturn(post);
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(replyCommentRepository.save(any())).willAnswer(inv -> {
            ReplyCommentEntity entity = inv.getArgument(0);
            setIdField(entity, new ObjectId());
            return entity;
        });
        doNothing().when(postCommandService).handleCommentCreation(any(), any());
        doNothing().when(bestService).applyBestScore(any());
        doNothing().when(notificationService).sendNotificationMessageByReply(any(), any(), any(), any());
        
        // When
        replyCommandService.addReply(commentId, dto);
        
        // Then
        verify(notificationService).sendNotificationMessageByReply(
                eq(post.getPostCategory()),
                eq(commentOwner),
                eq(post.get_id().toString()),
                eq(dto.getContent())
        );
    }
    
    @Test
    void updateReply_정상수정_성공() throws Exception {
        // Given
        String replyId = new ObjectId().toString();
        ReplyUpdateRequestDTO dto = createReplyUpdateRequestDTO("수정된 대댓글");
        ReplyCommentEntity reply = createReplyEntity();
        
        given(replyQueryService.findReplyById(any())).willReturn(reply);
        given(replyCommentRepository.save(any())).willReturn(reply);
        
        // When & Then
        assertThatCode(() -> replyCommandService.updateReply(replyId, dto)).doesNotThrowAnyException();
        verify(replyCommentRepository).save(reply);
    }
    
    @Test
    void softDeleteReply_정상삭제_성공() throws Exception {
        // Given
        String replyId = new ObjectId().toString();
        ReplyCommentEntity reply = createReplyEntity();
        CommentEntity comment = createCommentEntity();
        PostEntity post = createPostEntity();
        ObjectId userId = reply.getUserId();
        
        given(replyQueryService.findReplyById(any())).willReturn(reply);
        doNothing().when(SecurityUtils.class);
        SecurityUtils.validateUser(userId);
        given(commentQueryService.findCommentById(reply.getCommentId())).willReturn(comment);
        given(postQueryService.findPostById(comment.getPostId())).willReturn(post);
        given(replyCommentRepository.save(any())).willReturn(reply);
        doNothing().when(postCommandService).decreaseCommentCount(any());
        
        // When & Then
        assertThatCode(() -> replyCommandService.softDeleteReply(replyId)).doesNotThrowAnyException();
        verify(replyCommentRepository).save(reply);
        verify(postCommandService).decreaseCommentCount(post);
    }
    
    // Helper methods
    private ReplyCreateRequestDTO createReplyCreateRequestDTO(String content, boolean anonymous) throws Exception {
        ReplyCreateRequestDTO dto = ReplyCreateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "content", content);
        setField(dto, "anonymous", anonymous);
        return dto;
    }
    
    private ReplyUpdateRequestDTO createReplyUpdateRequestDTO(String content) throws Exception {
        ReplyUpdateRequestDTO dto = ReplyUpdateRequestDTO.class.getDeclaredConstructor().newInstance();
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
                .anonymous(false)
                .build();
        setIdFieldSafely(comment, new ObjectId());
        return comment;
    }
    
    private ReplyCommentEntity createReplyEntity() {
        ReplyCommentEntity reply = ReplyCommentEntity.builder()
                .commentId(new ObjectId())
                .userId(new ObjectId())
                .content("테스트 대댓글")
                .anonymous(false)
                .build();
        setIdFieldSafely(reply, new ObjectId());
        return reply;
    }
    
    private void setIdFieldSafely(Object entity, ObjectId id) {
        try {
            setIdField(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID field", e);
        }
    }
}