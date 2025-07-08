package inu.codin.codin.domain.post;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.notification.service.NotificationService;
import inu.codin.codin.domain.post.domain.comment.dto.response.CommentResponseDTO;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.reply.dto.request.ReplyCreateRequestDTO;
import inu.codin.codin.domain.post.domain.reply.dto.request.ReplyUpdateRequestDTO;
import inu.codin.codin.domain.post.domain.reply.entity.ReplyCommentEntity;
import inu.codin.codin.domain.post.domain.reply.repository.ReplyCommentRepository;
import inu.codin.codin.domain.post.domain.reply.service.ReplyCommentService;
import inu.codin.codin.domain.post.entity.PostAnonymous;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.report.repository.ReportRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.service.RedisBestService;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
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
class ReplyCommentServiceTest {
    @InjectMocks
    private ReplyCommentService replyCommentService;
    @Mock private PostRepository postRepository;
    @Mock private ReplyCommentRepository replyCommentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private LikeService likeService;
    @Mock private NotificationService notificationService;
    @Mock private RedisBestService redisBestService;
    @Mock private S3Service s3Service;
    @Mock private CommentRepository commentRepository;
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
    void 대댓글_정상등록_성공() throws Exception {
        // Given
        String commentId = new ObjectId().toString();
        ReplyCreateRequestDTO dto = createReplyCreateRequestDTO("내용", true);
        CommentEntity comment = createCommentEntity();
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        given(commentRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(comment));
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(replyCommentRepository.save(any())).willAnswer(inv -> {
            ReplyCommentEntity entity = inv.getArgument(0);
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("_id");
            idField.setAccessible(true);
            idField.set(entity, new ObjectId());
            return entity;
        });
        given(postRepository.save(any())).willReturn(post);
        willDoNothing().given(redisBestService).applyBestScore(anyInt(), any());
        willDoNothing().given(notificationService).sendNotificationMessageByReply(any(), any(), any(), any());
        // When
        replyCommentService.addReply(commentId, dto);
        // Then
        ArgumentCaptor<ReplyCommentEntity> captor = ArgumentCaptor.forClass(ReplyCommentEntity.class);
        verify(replyCommentRepository).save(captor.capture());
        ReplyCommentEntity saved = captor.getValue();
        assertThat(saved.getContent()).isEqualTo("내용");
        assertThat(saved.isAnonymous()).isTrue();
        assertThat(saved.getCommentId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(userId);
    }

    @Test
    void 대댓글_등록_댓글없음_예외() throws Exception {
        // Given
        String commentId = new ObjectId().toString();
        ReplyCreateRequestDTO dto = createReplyCreateRequestDTO("내용", true);
        given(commentRepository.findByIdAndNotDeleted(any())).willReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> replyCommentService.addReply(commentId, dto)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 대댓글_소프트삭제_성공() throws Exception {
        // Given
        String replyId = new ObjectId().toString();
        ReplyCommentEntity reply = createReplyCommentEntity();
        given(replyCommentRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(reply));
        securityUtilsMock.when(() -> SecurityUtils.validateUser(any())).thenAnswer(invocation -> null);
        given(replyCommentRepository.save(any())).willReturn(reply);
        // When
        replyCommentService.softDeleteReply(replyId);
        // Then
        ArgumentCaptor<ReplyCommentEntity> captor = ArgumentCaptor.forClass(ReplyCommentEntity.class);
        verify(replyCommentRepository).save(captor.capture());
        ReplyCommentEntity deleted = captor.getValue();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    void 대댓글_소프트삭제_댓글없음_예외() {
        // Given
        String replyId = new ObjectId().toString();
        given(replyCommentRepository.findByIdAndNotDeleted(any())).willReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> replyCommentService.softDeleteReply(replyId)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 댓글별_대댓글목록_조회_성공() {
        // Given
        PostAnonymous postAnonymous = Mockito.mock(PostAnonymous.class);
        ObjectId userId = new ObjectId();
        ObjectId commentId = new ObjectId();
        ReplyCommentEntity reply = ReplyCommentEntity.builder()
            ._id(new ObjectId())
            .userId(userId)
            .commentId(commentId)
            .content("내용")
            .anonymous(true)
            .build();
        List<ReplyCommentEntity> replies = List.of(reply);
        given(replyCommentRepository.findByCommentId(any())).willReturn(replies);
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
        List<CommentResponseDTO> result = replyCommentService.getRepliesByCommentId(postAnonymous, commentId);
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("내용");
    }

    @Test
    void 대댓글_수정_성공() throws Exception {
        // Given
        String replyId = new ObjectId().toString();
        ReplyUpdateRequestDTO dto = createReplyUpdateRequestDTO("수정내용");
        ReplyCommentEntity reply = createReplyCommentEntity();
        given(replyCommentRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(reply));
        given(replyCommentRepository.save(any())).willReturn(reply);
        // When
        replyCommentService.updateReply(replyId, dto);
        // Then
        ArgumentCaptor<ReplyCommentEntity> captor = ArgumentCaptor.forClass(ReplyCommentEntity.class);
        verify(replyCommentRepository).save(captor.capture());
        ReplyCommentEntity updated = captor.getValue();
        assertThat(updated.getContent()).isEqualTo("수정내용");
    }

    @Test
    void 대댓글_수정_댓글없음_예외() throws Exception {
        // Given
        String replyId = new ObjectId().toString();
        ReplyUpdateRequestDTO dto = createReplyUpdateRequestDTO("수정내용");
        given(replyCommentRepository.findByIdAndNotDeleted(any())).willReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> replyCommentService.updateReply(replyId, dto)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void 대댓글_유저정보_조회_성공() {
        // Given
        ObjectId replyId = new ObjectId();
        ObjectId userId = new ObjectId();
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(likeService.isLiked(any(), any(), any())).willReturn(true);
        // When
        CommentResponseDTO.UserInfo info = replyCommentService.getUserInfoAboutReply(replyId);
        // Then
        assertThat(info).isNotNull();
        assertThat(info.isLike()).isTrue();
    }

    // --- 리플렉션 기반 DTO/Entity 생성 유틸리티 ---
    private PostEntity createPostEntity() {
        return PostEntity.builder()._id(new ObjectId()).userId(new ObjectId()).postCategory(PostCategory.COMMUNICATION).build();
    }
    private ReplyCommentEntity createReplyCommentEntity() {
        return ReplyCommentEntity.builder()._id(new ObjectId()).userId(new ObjectId()).commentId(new ObjectId()).content("내용").anonymous(true).build();
    }
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
    private CommentEntity createCommentEntity() {
        return CommentEntity.builder()._id(new ObjectId()).userId(new ObjectId()).postId(new ObjectId()).content("내용").anonymous(true).build();
    }
} 