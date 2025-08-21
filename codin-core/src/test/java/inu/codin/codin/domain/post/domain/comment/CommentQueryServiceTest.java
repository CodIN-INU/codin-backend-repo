package inu.codin.codin.domain.post.domain.comment;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.exception.CommentException;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.comment.service.CommentQueryService;
import inu.codin.codin.domain.post.domain.comment.reply.service.ReplyQueryService;
import inu.codin.codin.domain.post.dto.UserInfo;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
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
class CommentQueryServiceTest {
    
    @InjectMocks
    private CommentQueryService commentQueryService;
    
    @Mock private CommentRepository commentRepository;
    @Mock private UserRepository userRepository;
    @Mock private LikeService likeService;
    @Mock private PostQueryService postQueryService;
    @Mock private S3Service s3Service;
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
    void getCommentsByPostId_정상조회_성공() {
        // Given
        String postId = new ObjectId().toString();
        PostEntity post = createPostEntityWithAnonymous();
        
        ObjectId userId1 = new ObjectId();
        ObjectId userId2 = new ObjectId();
        List<CommentEntity> comments = Arrays.asList(
            createCommentEntityWithUser(userId1),
            createCommentEntityWithUser(userId2)
        );
        List<UserEntity> users = Arrays.asList(
            createUserEntityWithId(userId1, "사용자1"),
            createUserEntityWithId(userId2, "사용자2")
        );
        List<CommentResponseDTO> replies = new ArrayList<>();
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(commentRepository.findByPostId(any())).willReturn(comments);
        given(userRepository.findAllById(anyList())).willReturn(users);
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        given(postQueryService.getUserAnonymousNumber(any(), any())).willReturn(1);
        given(replyQueryService.getRepliesByCommentId(any(), any())).willReturn(replies);
        given(likeService.getLikeCount(eq(LikeType.COMMENT), any())).willReturn(5);
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(likeService.isLiked(eq(LikeType.COMMENT), any(), (String) any())).willReturn(false);
        
        // When
        List<CommentResponseDTO> result = commentQueryService.getCommentsByPostId(postId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(postQueryService).findPostById(any());
        verify(commentRepository).findByPostId(any());
    }
    
    @Test
    void getCommentsByPostId_댓글없음_빈리스트반환() {
        // Given
        String postId = new ObjectId().toString();
        PostEntity post = createPostEntity();
        List<CommentEntity> emptyComments = new ArrayList<>();
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(commentRepository.findByPostId(any())).willReturn(emptyComments);
        
        // When
        List<CommentResponseDTO> result = commentQueryService.getCommentsByPostId(postId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(postQueryService).findPostById(any());
        verify(commentRepository).findByPostId(any());
        verify(userRepository, never()).findAllById(anyList());
    }
    
    @Test
    void getCommentsByPostId_익명게시물_익명번호할당() {
        // Given
        String postId = new ObjectId().toString();
        PostEntity post = createPostEntityWithAnonymous();
        ObjectId userId = new ObjectId();
        CommentEntity comment = createCommentEntityWithUser(userId);
        List<CommentEntity> comments = Arrays.asList(comment);
        UserEntity user = createUserEntityWithId(userId, "테스트사용자");
        List<UserEntity> users = Arrays.asList(user);
        List<CommentResponseDTO> replies = new ArrayList<>();
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(commentRepository.findByPostId(any())).willReturn(comments);
        given(userRepository.findAllById(anyList())).willReturn(users);
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        given(postQueryService.getUserAnonymousNumber(any(), any())).willReturn(2); // 익명 번호
        given(replyQueryService.getRepliesByCommentId(any(), any())).willReturn(replies);
        given(likeService.getLikeCount(eq(LikeType.COMMENT), any())).willReturn(3);
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(likeService.isLiked(eq(LikeType.COMMENT), any(), (String) any())).willReturn(true);
        
        // When
        List<CommentResponseDTO> result = commentQueryService.getCommentsByPostId(postId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(postQueryService).getUserAnonymousNumber(post.getAnonymous(), comment.getUserId());
    }
    
    @Test
    void findCommentById_정상조회_성공() {
        // Given
        ObjectId commentId = new ObjectId();
        CommentEntity comment = createCommentEntity();
        
        given(commentRepository.findByIdAndNotDeleted(commentId)).willReturn(Optional.of(comment));
        
        // When
        CommentEntity result = commentQueryService.findCommentById(commentId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(comment);
        verify(commentRepository).findByIdAndNotDeleted(commentId);
    }
    
    @Test
    void findCommentById_댓글없음_예외() {
        // Given
        ObjectId commentId = new ObjectId();
        
        given(commentRepository.findByIdAndNotDeleted(commentId)).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> commentQueryService.findCommentById(commentId))
                .isInstanceOf(CommentException.class);
        verify(commentRepository).findByIdAndNotDeleted(commentId);
    }
    
    @Test
    void getUserInfoAboutComment_좋아요한댓글_정상반환() {
        // Given
        ObjectId commentId = new ObjectId();
        ObjectId userId = new ObjectId();
        
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(likeService.isLiked(LikeType.COMMENT, commentId.toString(), userId)).willReturn(true);
        
        // When
        UserInfo result = commentQueryService.getUserInfoAboutComment(commentId);
        
        // Then
        assertThat(result).isNotNull();
        verify(likeService).isLiked(LikeType.COMMENT, commentId.toString(), userId);
    }
    
    @Test
    void getUserInfoAboutComment_좋아요안한댓글_정상반환() {
        // Given
        ObjectId commentId = new ObjectId();
        ObjectId userId = new ObjectId();
        
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(likeService.isLiked(LikeType.COMMENT, commentId.toString(), userId)).willReturn(false);
        
        // When
        UserInfo result = commentQueryService.getUserInfoAboutComment(commentId);
        
        // Then
        assertThat(result).isNotNull();
        verify(likeService).isLiked(LikeType.COMMENT, commentId.toString(), userId);
    }
    
    @Test
    void createUserMap_중복사용자_한번만조회() {
        // Given
        ObjectId userId1 = new ObjectId();
        ObjectId userId2 = new ObjectId();
        List<CommentEntity> comments = Arrays.asList(
            createCommentEntityWithUser(userId1),
            createCommentEntityWithUser(userId1), // 동일 사용자
            createCommentEntityWithUser(userId2)
        );
        List<UserEntity> users = Arrays.asList(
            createUserEntityWithId(userId1, "사용자1"),
            createUserEntityWithId(userId2, "사용자2")
        );
        PostEntity post = createPostEntityWithAnonymous();
        List<CommentResponseDTO> replies = new ArrayList<>();
        
        given(postQueryService.findPostById(any())).willReturn(post);
        given(commentRepository.findByPostId(any())).willReturn(comments);
        given(userRepository.findAllById(anyList())).willReturn(users);
        given(s3Service.getDefaultProfileImageUrl()).willReturn("default.jpg");
        given(postQueryService.getUserAnonymousNumber(any(), any())).willReturn(1);
        given(replyQueryService.getRepliesByCommentId(any(), any())).willReturn(replies);
        given(likeService.getLikeCount(eq(LikeType.COMMENT), any())).willReturn(0);
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(likeService.isLiked(eq(LikeType.COMMENT), any(), (String) any())).willReturn(false);
        
        // When
        List<CommentResponseDTO> result = commentQueryService.getCommentsByPostId(new ObjectId().toString());
        
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
    private PostEntity createPostEntity() {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .isAnonymous(false)
                .build();
        setIdFieldSafely(post, new ObjectId());
        return post;
    }
    
    private PostEntity createPostEntityWithAnonymous() {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .isAnonymous(true)
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
    
    private CommentEntity createCommentEntityWithUser(ObjectId userId) {
        CommentEntity comment = CommentEntity.builder()
                .postId(new ObjectId())
                .userId(userId)
                .content("테스트 댓글")
                .anonymous(false)
                .build();
        setIdFieldSafely(comment, new ObjectId());
        return comment;
    }
    
    private UserEntity createUserEntity(String nickname) {
        return UserEntity.builder()
                .nickname(nickname)
                .profileImageUrl("profile.jpg")
                .build();
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