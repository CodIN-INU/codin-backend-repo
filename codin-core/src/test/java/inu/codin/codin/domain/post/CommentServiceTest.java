package inu.codin.codin.domain.post;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentCreateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.dto.request.CommentUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.comment.service.CommentService;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.reply.service.ReplyCommentService;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.service.RedisBestService;
import inu.codin.codin.infra.s3.S3Service;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @InjectMocks
    private CommentService commentService;
    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private UserRepository userRepository;
    @Mock private LikeService likeService;
    @Mock private NotificationService notificationService;
    @Mock private RedisBestService redisBestService;
    @Mock private S3Service s3Service;
    @Mock private ReplyCommentService replyCommentService;
    private static MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
    }
    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void 댓글_정상등록_성공() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        CommentCreateRequestDTO dto = createCommentCreateRequestDTO("내용", true);
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(commentRepository.save(any())).willAnswer(inv -> {
            CommentEntity entity = inv.getArgument(0);
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("_id");
            idField.setAccessible(true);
            idField.set(entity, new ObjectId());
            return entity;
        });
        given(postRepository.save(any())).willReturn(post);
        willDoNothing().given(redisBestService).applyBestScore(anyInt(), any());
        willDoNothing().given(notificationService).sendNotificationMessageByComment(any(), any(), any(), any());
        // When
        commentService.addComment(postId, dto);
        // Then
        ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentRepository).save(captor.capture());
        CommentEntity saved = captor.getValue();
        assertThat(saved.getContent()).isEqualTo("내용");
        assertThat(saved.isAnonymous()).isTrue();
        assertThat(saved.getPostId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(userId);
    }

    @Test
    void 댓글_등록_게시글없음_예외() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        CommentCreateRequestDTO dto = createCommentCreateRequestDTO("내용", true);
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> commentService.addComment(postId, dto)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 댓글_소프트삭제_성공() throws Exception {
        // Given
        String commentId = new ObjectId().toString();
        CommentEntity comment = createCommentEntity();
        given(commentRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(comment));
        securityUtilsMock.when(() -> SecurityUtils.validateUser(any())).thenAnswer(invocation -> null);
        given(commentRepository.save(any())).willReturn(comment);
        // When
        commentService.softDeleteComment(commentId);
        // Then
        ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentRepository).save(captor.capture());
        CommentEntity deleted = captor.getValue();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    void 댓글_소프트삭제_댓글없음_예외() {
        // Given
        String commentId = new ObjectId().toString();
        given(commentRepository.findByIdAndNotDeleted(any())).willReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> commentService.softDeleteComment(commentId)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 게시글별_댓글목록_조회_성공() {
        // Given
        String postId = new ObjectId().toString();
        ObjectId userId = new ObjectId();
        PostEntity post = createPostEntity();
        CommentEntity comment = CommentEntity.builder()
            ._id(new ObjectId())
            .userId(userId)
            .postId(new ObjectId())
            .content("내용")
            .anonymous(true)
            .build();
        List<CommentEntity> comments = List.of(comment);
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(commentRepository.findByPostId(any())).willReturn(comments);
        given(s3Service.getDefaultProfileImageUrl()).willReturn("defaultUrl");
        UserEntity user = UserEntity.builder()
            .nickname("닉네임")
            .profileImageUrl("url")
            .build();
        try {
            setField(user, "_id", userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        given(userRepository.findAllById(any())).willReturn(List.of(user));
        // When
        List<CommentResponseDTO> result = commentService.getCommentsByPostId(postId);
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("내용");
    }

    @Test
    void 게시글별_댓글목록_조회_게시글없음_예외() {
        // Given
        String postId = new ObjectId().toString();
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> commentService.getCommentsByPostId(postId)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 댓글_수정_성공() throws Exception {
        // Given
        String commentId = new ObjectId().toString();
        CommentUpdateRequestDTO dto = createCommentUpdateRequestDTO("수정내용");
        CommentEntity comment = createCommentEntity();
        given(commentRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(comment));
        given(commentRepository.save(any())).willReturn(comment);
        // When
        commentService.updateComment(commentId, dto);
        // Then
        ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentRepository).save(captor.capture());
        CommentEntity updated = captor.getValue();
        assertThat(updated.getContent()).isEqualTo("수정내용");
    }

    @Test
    void 댓글_수정_댓글없음_예외() throws Exception {
        // Given
        String commentId = new ObjectId().toString();
        CommentUpdateRequestDTO dto = createCommentUpdateRequestDTO("수정내용");
        given(commentRepository.findByIdAndNotDeleted(any())).willReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(commentId, dto)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 댓글_유저정보_조회_성공() {
        // Given
        ObjectId commentId = new ObjectId();
        ObjectId userId = new ObjectId();
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(likeService.isLiked(any(), any(), any())).willReturn(true);
        // When
        CommentResponseDTO.UserInfo info = commentService.getUserInfoAboutComment(commentId);
        // Then
        assertThat(info).isNotNull();
        assertThat(info.isLike()).isTrue();
    }

    // --- 리플렉션 기반 DTO/Entity 생성 유틸리티 ---
    private PostEntity createPostEntity() {
        return PostEntity.builder()._id(new ObjectId()).userId(new ObjectId()).postCategory(PostCategory.COMMUNICATION).build();
    }
    private CommentEntity createCommentEntity() {
        return CommentEntity.builder()._id(new ObjectId()).userId(new ObjectId()).postId(new ObjectId()).content("내용").anonymous(true).build();
    }
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
} 