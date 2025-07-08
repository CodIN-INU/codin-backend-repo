package inu.codin.codin.domain.post.domain;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.poll.dto.PollCreateRequestDTO;
import inu.codin.codin.domain.post.domain.poll.dto.PollVotingRequestDTO;
import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import inu.codin.codin.domain.post.domain.poll.exception.PollDuplicateVoteException;
import inu.codin.codin.domain.post.domain.poll.exception.PollOptionChoiceException;
import inu.codin.codin.domain.post.domain.poll.exception.PollTimeFailException;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollVoteRepository;
import inu.codin.codin.domain.post.domain.poll.service.PollService;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.repository.PostRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PollServiceTest {
    @InjectMocks
    private PollService pollService;
    @Mock private PostRepository postRepository;
    @Mock private PollRepository pollRepository;
    @Mock private PollVoteRepository pollVoteRepository;
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
    void 투표_정상생성_성공() throws Exception {
        // Given
        PollCreateRequestDTO dto = createPollCreateRequestDTO("제목", "내용", List.of("A", "B"), false, LocalDateTime.now().plusDays(1), true, PostCategory.POLL);
        ObjectId userId = new ObjectId();
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(postRepository.save(any())).willAnswer(inv -> {
            PostEntity entity = inv.getArgument(0);
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("_id");
            idField.setAccessible(true);
            idField.set(entity, new ObjectId());
            return entity;
        });
        given(pollRepository.save(any())).willAnswer(inv -> {
            PollEntity entity = inv.getArgument(0);
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("_id");
            idField.setAccessible(true);
            idField.set(entity, new ObjectId());
            return entity;
        });
        // When
        pollService.createPoll(dto);
        // Then
        ArgumentCaptor<PollEntity> captor = ArgumentCaptor.forClass(PollEntity.class);
        verify(pollRepository).save(captor.capture());
        PollEntity saved = captor.getValue();
        assertThat(saved.getPollOptions()).containsExactly("A", "B");
        assertThat(saved.isMultipleChoice()).isFalse();
    }

    @Test
    void 투표_투표하기_정상() {
        // Given
        String postId = new ObjectId().toString();
        ObjectId postObjId = new ObjectId(postId);
        ObjectId pollId = new ObjectId();
        ObjectId userId = new ObjectId();
        PostEntity post = PostEntity.builder()._id(postObjId).userId(userId).postCategory(PostCategory.POLL).postStatus(PostStatus.ACTIVE).build();
        PollEntity poll = PollEntity.builder()._id(pollId).postId(postObjId).pollOptions(List.of("A", "B")).pollEndTime(LocalDateTime.now().plusDays(1)).multipleChoice(false).build();
        PollVotingRequestDTO dto = createPollVotingRequestDTO(List.of(0));
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(any())).willReturn(Optional.of(poll));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(pollVoteRepository.existsByPollIdAndUserId(any(), any())).willReturn(false);
        given(pollVoteRepository.save(any())).willReturn(PollVoteEntity.builder().pollId(pollId).userId(userId).selectedOptions(List.of(0)).build());
        given(pollRepository.save(any())).willReturn(poll);
        // When/Then
        assertThatCode(() -> pollService.votingPoll(postId, dto)).doesNotThrowAnyException();
    }

    @Test
    void 투표_투표하기_중복투표_예외() {
        // Given
        String postId = new ObjectId().toString();
        ObjectId postObjId = new ObjectId(postId);
        ObjectId pollId = new ObjectId();
        ObjectId userId = new ObjectId();
        PostEntity post = PostEntity.builder()._id(postObjId).userId(userId).postCategory(PostCategory.POLL).postStatus(PostStatus.ACTIVE).build();
        PollEntity poll = PollEntity.builder()._id(pollId).postId(postObjId).pollOptions(List.of("A", "B")).pollEndTime(LocalDateTime.now().plusDays(1)).multipleChoice(false).build();
        PollVotingRequestDTO dto = createPollVotingRequestDTO(List.of(0));
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(any())).willReturn(Optional.of(poll));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(pollVoteRepository.existsByPollIdAndUserId(any(), any())).willReturn(true);
        // When/Then
        assertThatThrownBy(() -> pollService.votingPoll(postId, dto)).isInstanceOf(PollDuplicateVoteException.class);
    }

    @Test
    void 투표_투표하기_종료된투표_예외() {
        // Given
        String postId = new ObjectId().toString();
        ObjectId postObjId = new ObjectId(postId);
        ObjectId pollId = new ObjectId();
        ObjectId userId = new ObjectId();
        PostEntity post = PostEntity.builder()._id(postObjId).userId(userId).postCategory(PostCategory.POLL).postStatus(PostStatus.ACTIVE).build();
        PollEntity poll = PollEntity.builder()._id(pollId).postId(postObjId).pollOptions(List.of("A", "B")).pollEndTime(LocalDateTime.now().minusDays(1)).multipleChoice(false).build();
        PollVotingRequestDTO dto = createPollVotingRequestDTO(List.of(0));
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(any())).willReturn(Optional.of(poll));
        // When/Then
        assertThatThrownBy(() -> pollService.votingPoll(postId, dto)).isInstanceOf(PollTimeFailException.class);
    }

    @Test
    void 투표_투표하기_복수선택불가_예외() {
        // Given
        String postId = new ObjectId().toString();
        ObjectId postObjId = new ObjectId(postId);
        ObjectId pollId = new ObjectId();
        ObjectId userId = new ObjectId();
        PostEntity post = PostEntity.builder()._id(postObjId).userId(userId).postCategory(PostCategory.POLL).postStatus(PostStatus.ACTIVE).build();
        PollEntity poll = PollEntity.builder()._id(pollId).postId(postObjId).pollOptions(List.of("A", "B")).pollEndTime(LocalDateTime.now().plusDays(1)).multipleChoice(false).build();
        PollVotingRequestDTO dto = createPollVotingRequestDTO(List.of(0, 1));
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(any())).willReturn(Optional.of(poll));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(pollVoteRepository.existsByPollIdAndUserId(any(), any())).willReturn(false);
        // When/Then
        assertThatThrownBy(() -> pollService.votingPoll(postId, dto)).isInstanceOf(PollOptionChoiceException.class);
    }

    @Test
    void 투표_투표취소_정상() {
        // Given
        String postId = new ObjectId().toString();
        ObjectId postObjId = new ObjectId(postId);
        ObjectId pollId = new ObjectId();
        ObjectId userId = new ObjectId();
        PostEntity post = PostEntity.builder()._id(postObjId).userId(userId).postCategory(PostCategory.POLL).postStatus(PostStatus.ACTIVE).build();
        PollEntity poll = PollEntity.builder()._id(pollId).postId(postObjId).pollOptions(List.of("A", "B")).pollEndTime(LocalDateTime.now().plusDays(1)).multipleChoice(false).build();
        // 투표가 이미 1회 들어간 상태로 세팅
        poll.getPollVotesCounts().set(0, 1);
        PollVoteEntity pollVote = PollVoteEntity.builder().pollId(pollId).userId(userId).selectedOptions(List.of(0)).build();
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(any())).willReturn(Optional.of(poll));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(pollVoteRepository.findByPollIdAndUserId(any(), any())).willReturn(Optional.of(pollVote));
        given(pollRepository.save(any())).willReturn(poll);
        // When/Then
        assertThatCode(() -> pollService.deleteVoting(postId)).doesNotThrowAnyException();
    }

    // --- 리플렉션 기반 DTO 생성 유틸리티 ---
    private PollCreateRequestDTO createPollCreateRequestDTO(String title, String content, List<String> options, boolean multipleChoice, LocalDateTime endTime, boolean anonymous, PostCategory category) throws Exception {
        PollCreateRequestDTO dto = PollCreateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "title", title);
        setField(dto, "content", content);
        setField(dto, "pollOptions", options);
        setField(dto, "multipleChoice", multipleChoice);
        setField(dto, "pollEndTime", endTime);
        setField(dto, "anonymous", anonymous);
        setField(dto, "postCategory", category);
        return dto;
    }
    private PollVotingRequestDTO createPollVotingRequestDTO(List<Integer> selectedOptions) {
        try {
            PollVotingRequestDTO dto = PollVotingRequestDTO.class.getDeclaredConstructor().newInstance();
            setField(dto, "selectedOptions", selectedOptions);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void setField(Object target, String field, Object value) throws Exception {
        java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
} 