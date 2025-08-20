package inu.codin.codin.domain.post.domain.poll;

import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import inu.codin.codin.domain.post.domain.poll.exception.PollException;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollVoteRepository;
import inu.codin.codin.domain.post.domain.poll.service.PollQueryService;
import inu.codin.codin.domain.post.dto.response.PollInfoResponseDTO;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
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
class PollQueryServiceTest {
    
    @InjectMocks
    private PollQueryService pollQueryService;
    
    @Mock private PollRepository pollRepository;
    @Mock private PollVoteRepository pollVoteRepository;
    
    @Test
    void getPollInfo_정상조회_성공() {
        // Given
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        PollEntity poll = createActivePollEntity();
        PollVoteEntity userVote = createPollVoteEntity();
        long totalParticipants = 5L;
        
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(pollVoteRepository.countByPollId(poll.get_id())).willReturn(totalParticipants);
        given(pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)).willReturn(Optional.of(userVote));
        given(pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId)).willReturn(true);
        
        // When
        PollInfoResponseDTO result = pollQueryService.getPollInfo(post, userId);
        
        // Then
        assertThat(result).isNotNull();
        verify(pollRepository).findByPostId(post.get_id());
        verify(pollVoteRepository).countByPollId(poll.get_id());
        verify(pollVoteRepository).findByPollIdAndUserId(poll.get_id(), userId);
        verify(pollVoteRepository).existsByPollIdAndUserId(poll.get_id(), userId);
    }
    
    @Test
    void getPollInfo_투표없음_예외() {
        // Given
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> pollQueryService.getPollInfo(post, userId))
                .isInstanceOf(PollException.class);
        verify(pollRepository).findByPostId(post.get_id());
    }
    
    @Test
    void getPollInfo_사용자투표안함_빈리스트반환() {
        // Given
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        PollEntity poll = createActivePollEntity();
        long totalParticipants = 3L;
        
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(pollVoteRepository.countByPollId(poll.get_id())).willReturn(totalParticipants);
        given(pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)).willReturn(Optional.empty());
        given(pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId)).willReturn(false);
        
        // When
        PollInfoResponseDTO result = pollQueryService.getPollInfo(post, userId);
        
        // Then
        assertThat(result).isNotNull();
        verify(pollVoteRepository).findByPollIdAndUserId(poll.get_id(), userId);
        verify(pollVoteRepository).existsByPollIdAndUserId(poll.get_id(), userId);
    }
    
    @Test
    void getPollInfo_투표종료됨_종료상태반환() {
        // Given
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        PollEntity poll = createExpiredPollEntity();
        long totalParticipants = 10L;
        
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(pollVoteRepository.countByPollId(poll.get_id())).willReturn(totalParticipants);
        given(pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)).willReturn(Optional.empty());
        given(pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId)).willReturn(false);
        
        // When
        PollInfoResponseDTO result = pollQueryService.getPollInfo(post, userId);
        
        // Then
        assertThat(result).isNotNull();
        verify(pollRepository).findByPostId(post.get_id());
    }
    
    @Test
    void getPollInfo_복수선택투표_정상조회() {
        // Given
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        PollEntity poll = createMultipleChoicePollEntity();
        PollVoteEntity userVote = createMultipleVoteEntity();
        long totalParticipants = 8L;
        
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(pollVoteRepository.countByPollId(poll.get_id())).willReturn(totalParticipants);
        given(pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)).willReturn(Optional.of(userVote));
        given(pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId)).willReturn(true);
        
        // When
        PollInfoResponseDTO result = pollQueryService.getPollInfo(post, userId);
        
        // Then
        assertThat(result).isNotNull();
        verify(pollRepository).findByPostId(post.get_id());
        verify(pollVoteRepository).countByPollId(poll.get_id());
    }
    
    @Test
    void getPollInfo_투표참가자없음_0반환() {
        // Given
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        PollEntity poll = createActivePollEntity();
        long totalParticipants = 0L;
        
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(pollVoteRepository.countByPollId(poll.get_id())).willReturn(totalParticipants);
        given(pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)).willReturn(Optional.empty());
        given(pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId)).willReturn(false);
        
        // When
        PollInfoResponseDTO result = pollQueryService.getPollInfo(post, userId);
        
        // Then
        assertThat(result).isNotNull();
        verify(pollVoteRepository).countByPollId(poll.get_id());
    }
    
    @Test
    void getPollInfo_종료시간없음_종료안됨처리() {
        // Given
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        PollEntity poll = createPollEntityWithoutEndTime();
        long totalParticipants = 2L;
        
        given(pollRepository.findByPostId(post.get_id())).willReturn(Optional.of(poll));
        given(pollVoteRepository.countByPollId(poll.get_id())).willReturn(totalParticipants);
        given(pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)).willReturn(Optional.empty());
        given(pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId)).willReturn(false);
        
        // When
        PollInfoResponseDTO result = pollQueryService.getPollInfo(post, userId);
        
        // Then
        assertThat(result).isNotNull();
        verify(pollRepository).findByPostId(post.get_id());
    }
    
    // Helper methods
    private PostEntity createPostEntity() {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.POLL)
                .build();
        setIdFieldSafely(post, new ObjectId());
        return post;
    }
    
    private PollEntity createActivePollEntity() {
        PollEntity poll = PollEntity.builder()
                .postId(new ObjectId())
                .pollOptions(Arrays.asList("선택지1", "선택지2", "선택지3"))
                .pollEndTime(LocalDateTime.now().plusDays(1))
                .multipleChoice(false)
                .build();
        setIdFieldSafely(poll, new ObjectId());
        
        // 행위로 상태 만들기 - vote() 메서드로 투표 수 쌓기
        poll.vote(0); // 선택지1에 2표
        poll.vote(0);
        poll.vote(1); // 선택지2에 1표
        poll.vote(2); // 선택지3에 2표
        poll.vote(2);
        
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
        
        // 행위로 상태 만들기 - vote() 메서드로 투표 수 쌓기
        for (int i = 0; i < 5; i++) {
            poll.vote(0); // 선택지1에 5표
            poll.vote(1); // 선택지2에 5표
        }
        
        return poll;
    }
    
    private PollEntity createMultipleChoicePollEntity() {
        PollEntity poll = PollEntity.builder()
                .postId(new ObjectId())
                .pollOptions(Arrays.asList("옵션1", "옵션2", "옵션3", "옵션4"))
                .pollEndTime(LocalDateTime.now().plusHours(12))
                .multipleChoice(true) // 복수 선택 허용
                .build();
        setIdFieldSafely(poll, new ObjectId());
        
        // 행위로 상태 만들기 - vote() 메서드로 투표 수 쌓기
        poll.vote(0); poll.vote(0); poll.vote(0); // 옵션1에 3표
        poll.vote(1); poll.vote(1);               // 옵션2에 2표
        poll.vote(2); poll.vote(2); poll.vote(2); poll.vote(2); // 옵션3에 4표
        poll.vote(3);                             // 옵션4에 1표
        
        return poll;
    }
    
    private PollEntity createPollEntityWithoutEndTime() {
        PollEntity poll = PollEntity.builder()
                .postId(new ObjectId())
                .pollOptions(Arrays.asList("A", "B"))
                .pollEndTime(null) // 종료 시간 없음
                .multipleChoice(false)
                .build();
        setIdFieldSafely(poll, new ObjectId());
        
        // 행위로 상태 만들기 - vote() 메서드로 투표 수 쌓기
        poll.vote(0); // A에 1표
        poll.vote(1); // B에 1표
        
        return poll;
    }
    
    private PollVoteEntity createPollVoteEntity() {
        PollVoteEntity vote = PollVoteEntity.builder()
                .pollId(new ObjectId())
                .userId(new ObjectId())
                .selectedOptions(Arrays.asList(0)) // 첫 번째 선택지 선택
                .build();
        setIdFieldSafely(vote, new ObjectId());
        return vote;
    }
    
    private PollVoteEntity createMultipleVoteEntity() {
        PollVoteEntity vote = PollVoteEntity.builder()
                .pollId(new ObjectId())
                .userId(new ObjectId())
                .selectedOptions(Arrays.asList(0, 2)) // 복수 선택
                .build();
        setIdFieldSafely(vote, new ObjectId());
        return vote;
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