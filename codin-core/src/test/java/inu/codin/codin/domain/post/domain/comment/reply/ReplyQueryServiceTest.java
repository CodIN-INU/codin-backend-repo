package inu.codin.codin.domain.post.domain.comment.reply;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.comment.reply.exception.ReplyException;
import inu.codin.codin.domain.post.domain.comment.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.domain.comment.reply.service.ReplyQueryService;
import inu.codin.codin.domain.post.dto.UserInfo;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.service.PostQueryService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ReplyQueryServiceTest {
    
    @InjectMocks
    private ReplyQueryService replyQueryService;
    
    @Mock private ReplyCommentRepository replyCommentRepository;
    @Mock private UserRepository userRepository;
    @Mock private PostQueryService postQueryService;
    @Mock private LikeService likeService;
    @Mock private S3Service s3Service;
    
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
    void getRepliesByCommentId_정상조회_성공() {
        // Given
        ObjectId commentId = new ObjectId();
        PostAnonymous postAnonymous = mock(PostAnonymous.class);
        
        ObjectId userId1 = new ObjectId();
        ObjectId userId2 = new ObjectId();
        List<ReplyCommentEntity> replies = Arrays.asList(
            createReplyEntityWithUser(userId1),
            createReplyEntityWithUser(userId2)
        );
        List<UserEntity> users = Arrays.asList(
            createUserEntityWithId(userId1, "대댓글사용자1"),
            createUserEntityWithId(userId2, "대댓글사용자2")
        );
        
        given(replyCommentRepository.findByCommentId(commentId)).willReturn(replies);
        given(userRepository.findAllById(anyList())).willReturn(users);
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        given(postQueryService.getUserAnonymousNumber(any(), any())).willReturn(1);
        given(likeService.getLikeCount(eq(LikeType.REPLY), any())).willReturn(3);
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(likeService.isLiked(eq(LikeType.COMMENT), any(), any())).willReturn(false);
        
        // When
        List<CommentResponseDTO> result = replyQueryService.getRepliesByCommentId(postAnonymous, commentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(replyCommentRepository).findByCommentId(commentId);
        verify(userRepository).findAllById(anyList());
    }
    
    @Test
    void getRepliesByCommentId_대댓글없음_빈리스트반환() {
        // Given
        ObjectId commentId = new ObjectId();
        PostAnonymous postAnonymous = mock(PostAnonymous.class);
        List<ReplyCommentEntity> emptyReplies = new ArrayList<>();
        
        given(replyCommentRepository.findByCommentId(commentId)).willReturn(emptyReplies);
        
        // When
        List<CommentResponseDTO> result = replyQueryService.getRepliesByCommentId(postAnonymous, commentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(replyCommentRepository).findByCommentId(commentId);
        verify(userRepository, never()).findAllById(anyList());
    }
    
    @Test
    void getRepliesByCommentId_익명게시물_익명번호할당() {
        // Given
        ObjectId commentId = new ObjectId();
        PostAnonymous postAnonymous = mock(PostAnonymous.class);
        ObjectId userId = new ObjectId();
        ReplyCommentEntity reply = createReplyEntityWithUser(userId);
        List<ReplyCommentEntity> replies = Arrays.asList(reply);
        UserEntity user = createUserEntityWithId(userId, "익명사용자");
        List<UserEntity> users = Arrays.asList(user);
        
        given(replyCommentRepository.findByCommentId(commentId)).willReturn(replies);
        given(userRepository.findAllById(anyList())).willReturn(users);
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        given(postQueryService.getUserAnonymousNumber(postAnonymous, userId)).willReturn(3); // 익명 번호
        given(likeService.getLikeCount(eq(LikeType.REPLY), any())).willReturn(2);
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(likeService.isLiked(eq(LikeType.COMMENT), any(), any())).willReturn(true);
        
        // When
        List<CommentResponseDTO> result = replyQueryService.getRepliesByCommentId(postAnonymous, commentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(postQueryService).getUserAnonymousNumber(postAnonymous, userId);
    }
    
    @Test
    void findReplyById_정상조회_성공() {
        // Given
        ObjectId replyId = new ObjectId();
        ReplyCommentEntity reply = createReplyEntity();
        
        given(replyCommentRepository.findByIdAndNotDeleted(replyId)).willReturn(Optional.of(reply));
        
        // When
        ReplyCommentEntity result = replyQueryService.findReplyById(replyId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(reply);
        verify(replyCommentRepository).findByIdAndNotDeleted(replyId);
    }
    
    @Test
    void findReplyById_대댓글없음_예외() {
        // Given
        ObjectId replyId = new ObjectId();
        
        given(replyCommentRepository.findByIdAndNotDeleted(replyId)).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> replyQueryService.findReplyById(replyId))
                .isInstanceOf(ReplyException.class);
        verify(replyCommentRepository).findByIdAndNotDeleted(replyId);
    }
    
    @Test
    void getUserInfoAboutReply_좋아요한대댓글_정상반환() {
        // Given
        ObjectId replyId = new ObjectId();
        ObjectId userId = new ObjectId();
        
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(likeService.isLiked(LikeType.COMMENT, replyId, userId)).willReturn(true);
        
        // When
        UserInfo result = replyQueryService.getUserInfoAboutReply(replyId);
        
        // Then
        assertThat(result).isNotNull();
        verify(likeService).isLiked(LikeType.COMMENT, replyId, userId);
    }
    
    @Test
    void getUserInfoAboutReply_좋아요안한대댓글_정상반환() {
        // Given
        ObjectId replyId = new ObjectId();
        ObjectId userId = new ObjectId();
        
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(likeService.isLiked(LikeType.COMMENT, replyId, userId)).willReturn(false);
        
        // When
        UserInfo result = replyQueryService.getUserInfoAboutReply(replyId);
        
        // Then
        assertThat(result).isNotNull();
        verify(likeService).isLiked(LikeType.COMMENT, replyId, userId);
    }
    
    @Test
    void createUserMapFromReplies_중복사용자_한번만조회() {
        // Given
        ObjectId commentId = new ObjectId();
        PostAnonymous postAnonymous = mock(PostAnonymous.class);
        ObjectId userId1 = new ObjectId();
        ObjectId userId2 = new ObjectId();
        List<ReplyCommentEntity> replies = Arrays.asList(
            createReplyEntityWithUser(userId1),
            createReplyEntityWithUser(userId1), // 동일 사용자
            createReplyEntityWithUser(userId2)
        );
        List<UserEntity> users = Arrays.asList(
            createUserEntityWithId(userId1, "사용자1"),
            createUserEntityWithId(userId2, "사용자2")
        );
        
        given(replyCommentRepository.findByCommentId(commentId)).willReturn(replies);
        given(userRepository.findAllById(anyList())).willReturn(users);
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        given(postQueryService.getUserAnonymousNumber(any(), any())).willReturn(1);
        given(likeService.getLikeCount(eq(LikeType.REPLY), any())).willReturn(0);
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(likeService.isLiked(eq(LikeType.COMMENT), any(), any())).willReturn(false);
        
        // When
        List<CommentResponseDTO> result = replyQueryService.getRepliesByCommentId(postAnonymous, commentId);
        
        // Then
        assertThat(result).hasSize(3);
        // userId1과 userId2만 조회되어야 함 (중복 제거)
        verify(userRepository).findAllById(argThat(iterable -> {
            List<ObjectId> ids = StreamSupport.stream(iterable.spliterator(), false)
                    .toList();
            return ids.size() == 2 && ids.contains(userId1) && ids.contains(userId2);
        }));
    }
    
    // Helper methods
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
    
    private ReplyCommentEntity createReplyEntityWithUser(ObjectId userId) {
        ReplyCommentEntity reply = ReplyCommentEntity.builder()
                .commentId(new ObjectId())
                .userId(userId)
                .content("테스트 대댓글")
                .anonymous(false)
                .build();
        setIdFieldSafely(reply, new ObjectId());
        return reply;
    }
    
    private UserEntity createUserEntityWithId(ObjectId id, String nickname) {
        UserEntity user = UserEntity.builder()
                .nickname(nickname)
                .profileImageUrl("profile.jpg")
                .build();
        setIdFieldSafely(user, id);
        return user;
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