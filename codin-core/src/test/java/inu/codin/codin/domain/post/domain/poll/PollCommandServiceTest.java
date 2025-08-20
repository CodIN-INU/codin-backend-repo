package inu.codin.codin.domain.post.domain.poll;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.poll.dto.PollCreateRequestDTO;
import inu.codin.codin.domain.post.domain.poll.dto.PollVotingRequestDTO;
import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import inu.codin.codin.domain.post.domain.poll.exception.PollException;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollVoteRepository;
import inu.codin.codin.domain.post.domain.poll.service.PollCommandService;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.exception.PostException;
import inu.codin.codin.domain.post.repository.PostRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PollCommandServiceTest {
    
    @InjectMocks
    private PollCommandService pollCommandService;
    
    @Mock private PollRepository pollRepository;
    @Mock private PollVoteRepository pollVoteRepository;
    @Mock private PostRepository postRepository;
    
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
    void createPoll_정상생성_성공() throws Exception {
        // Given
        PollCreateRequestDTO dto = createPollCreateRequestDTO(
            "투표 제목", 
            "투표 내용", 
            Arrays.asList("선택지1", "선택지2"), 
            LocalDateTime.now().plusDays(1),
            false,
            false
        );
        ObjectId userId = new ObjectId();
        PostEntity post = createPostEntity();
        PollEntity poll = createPollEntity();
        
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(postRepository.save(any())).willAnswer(inv -> {
            PostEntity entity = inv.getArgument(0);
            setIdField(entity, new ObjectId());
            return entity;
        });
        given(pollRepository.save(any())).willReturn(poll);
        
        // When & Then
        assertThatCode(() -> pollCommandService.createPoll(dto)).doesNotThrowAnyException();
        verify(postRepository).save(any(PostEntity.class));
        verify(pollRepository).save(any(PollEntity.class));
    }
    
    @Test
    void votingPoll_정상투표_성공() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PollVotingRequestDTO dto = createPollVotingRequestDTO(Arrays.asList(0));
        PostEntity post = createPostEntity();
        PollEntity poll = mock(PollEntity.class);
        ObjectId userId = new ObjectId();
        
        // Mock PollEntity 설정
        given(poll.get_id()).willReturn(new ObjectId());
        given(poll.getPollEndTime()).willReturn(LocalDateTime.now().plusDays(1));
        given(poll.isMultipleChoice()).willReturn(false);
        given(poll.getPollOptions()).willReturn(Arrays.asList("선택지1", "선택지2"));
        doNothing().when(poll).vote(anyInt());
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId)).willReturn(false);
        given(pollVoteRepository.save(any())).willReturn(createPollVoteEntity());
        given(pollRepository.save(any())).willReturn(poll);
        
        // When & Then
        assertThatCode(() -> pollCommandService.votingPoll(postId, dto)).doesNotThrowAnyException();
        verify(pollVoteRepository).save(any(PollVoteEntity.class));
        verify(pollRepository).save(poll);
        verify(poll).vote(0);
    }
    
    @Test
    void votingPoll_게시물없음_예외() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PollVotingRequestDTO dto = createPollVotingRequestDTO(Arrays.asList(0));
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> pollCommandService.votingPoll(postId, dto))
                .isInstanceOf(PostException.class);
    }
    
    @Test
    void votingPoll_투표없음_예외() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PollVotingRequestDTO dto = createPollVotingRequestDTO(Arrays.asList(0));
        PostEntity post = createPostEntity();
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> pollCommandService.votingPoll(postId, dto))
                .isInstanceOf(PollException.class);
    }
    
    @Test
    void votingPoll_투표종료됨_예외() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PollVotingRequestDTO dto = createPollVotingRequestDTO(Arrays.asList(0));
        PostEntity post = createPostEntity();
        PollEntity poll = createExpiredPollEntity();
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        
        // When & Then
        assertThatThrownBy(() -> pollCommandService.votingPoll(postId, dto))
                .isInstanceOf(PollException.class);
    }
    
    @Test
    void votingPoll_중복투표_예외() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PollVotingRequestDTO dto = createPollVotingRequestDTO(Arrays.asList(0));
        PostEntity post = createPostEntity();
        PollEntity poll = createPollEntityWithOptions();
        ObjectId userId = new ObjectId();
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId)).willReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> pollCommandService.votingPoll(postId, dto))
                .isInstanceOf(PollException.class);
    }
    
    @Test
    void votingPoll_복수선택불가_예외() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PollVotingRequestDTO dto = createPollVotingRequestDTO(Arrays.asList(0, 1)); // 복수 선택
        PostEntity post = createPostEntity();
        PollEntity poll = createSingleChoicePollEntity(); // 단일 선택만 허용
        ObjectId userId = new ObjectId();
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId)).willReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> pollCommandService.votingPoll(postId, dto))
                .isInstanceOf(PollException.class);
    }
    
    @Test
    void votingPoll_잘못된선택지_예외() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PollVotingRequestDTO dto = createPollVotingRequestDTO(Arrays.asList(5)); // 존재하지 않는 선택지
        PostEntity post = createPostEntity();
        PollEntity poll = createPollEntityWithOptions(); // 2개 선택지만 있음
        ObjectId userId = new ObjectId();
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId)).willReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> pollCommandService.votingPoll(postId, dto))
                .isInstanceOf(PollException.class);
    }
    
    @Test
    void deleteVoting_정상취소_성공() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PostEntity post = createPostEntity();
        PollEntity poll = mock(PollEntity.class);
        ObjectId userId = new ObjectId();
        PollVoteEntity vote = mock(PollVoteEntity.class);
        
        // Mock PollEntity 설정
        given(poll.get_id()).willReturn(new ObjectId());
        given(poll.getPollEndTime()).willReturn(LocalDateTime.now().plusDays(1));
        doNothing().when(poll).deleteVote(anyInt());
        
        // Mock PollVoteEntity 설정
        given(vote.getSelectedOptions()).willReturn(Arrays.asList(0));
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)).willReturn(Optional.of(vote));
        given(pollRepository.save(any())).willReturn(poll);
        doNothing().when(pollVoteRepository).delete(vote);
        
        // When & Then
        assertThatCode(() -> pollCommandService.deleteVoting(postId)).doesNotThrowAnyException();
        verify(pollRepository).save(poll);
        verify(pollVoteRepository).delete(vote);
        verify(poll).deleteVote(0);
    }
    
    @Test
    void deleteVoting_투표내역없음_예외() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PostEntity post = createPostEntity();
        PollEntity poll = createPollEntityWithOptions();
        ObjectId userId = new ObjectId();
        
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> pollCommandService.deleteVoting(postId))
                .isInstanceOf(PollException.class);
    }
    
    // Helper methods
    private PollCreateRequestDTO createPollCreateRequestDTO(String title, String content, List<String> options, 
                                                           LocalDateTime endTime, boolean multipleChoice, boolean anonymous) throws Exception {
        PollCreateRequestDTO dto = PollCreateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "title", title);
        setField(dto, "content", content);
        setField(dto, "pollOptions", options);
        setField(dto, "pollEndTime", endTime);
        setField(dto, "multipleChoice", multipleChoice);
        setField(dto, "anonymous", anonymous);
        setField(dto, "postCategory", PostCategory.POLL);
        return dto;
    }
    
    private PollVotingRequestDTO createPollVotingRequestDTO(List<Integer> selectedOptions) throws Exception {
        PollVotingRequestDTO dto = PollVotingRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "selectedOptions", selectedOptions);
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
                .postCategory(PostCategory.POLL)
                .postStatus(PostStatus.ACTIVE)
                .build();
        setIdFieldSafely(post, new ObjectId());
        return post;
    }
    
    private PollEntity createPollEntity() {
        PollEntity poll = PollEntity.builder()
                .postId(new ObjectId())
                .pollOptions(Arrays.asList("선택지1", "선택지2"))
                .pollEndTime(LocalDateTime.now().plusDays(1))
                .multipleChoice(false)
                .build();
        setIdFieldSafely(poll, new ObjectId());
        return poll;
    }
    
    private PollEntity createPollEntityWithOptions() {
        PollEntity poll = PollEntity.builder()
                .postId(new ObjectId())
                .pollOptions(Arrays.asList("선택지1", "선택지2"))
                .pollEndTime(LocalDateTime.now().plusDays(1))
                .multipleChoice(true)
                .build();
        setIdFieldSafely(poll, new ObjectId());
        return poll;
    }
    
    private PollEntity createExpiredPollEntity() {
        PollEntity poll = PollEntity.builder()
                .postId(new ObjectId())
                .pollOptions(Arrays.asList("선택지1", "선택지2"))
                .pollEndTime(LocalDateTime.now().minusDays(1)) // 과거 시간
                .multipleChoice(false)
                .build();
        setIdFieldSafely(poll, new ObjectId());
        return poll;
    }
    
    private PollEntity createSingleChoicePollEntity() {
        PollEntity poll = PollEntity.builder()
                .postId(new ObjectId())
                .pollOptions(Arrays.asList("선택지1", "선택지2"))
                .pollEndTime(LocalDateTime.now().plusDays(1))
                .multipleChoice(false) // 단일 선택만 허용
                .build();
        setIdFieldSafely(poll, new ObjectId());
        return poll;
    }
    
    private PollVoteEntity createPollVoteEntity() {
        PollVoteEntity vote = PollVoteEntity.builder()
                .pollId(new ObjectId())
                .userId(new ObjectId())
                .selectedOptions(Arrays.asList(0)) // 첫 번째 선택지 (유효한 인덱스)
                .build();
        setIdFieldSafely(vote, new ObjectId());
        return vote;
    }
    
    private void setIdFieldSafely(Object entity, ObjectId id) {
        try {
            setIdField(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID field", e);
        }
    }
}