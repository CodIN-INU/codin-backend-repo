package inu.codin.codin.domain.post.domain.hits.service;

import inu.codin.codin.domain.post.domain.hits.entity.HitsEntity;
import inu.codin.codin.domain.post.domain.hits.repository.HitsRepository;
import inu.codin.codin.infra.redis.config.RedisHealthChecker;
import inu.codin.codin.infra.redis.service.RedisHitsService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class HitsServiceTest {
    @InjectMocks
    private HitsService hitsService;
    @Mock private RedisHitsService redisHitsService;
    @Mock private RedisHealthChecker redisHealthChecker;
    @Mock private HitsRepository hitsRepository;

    @Test
    void 게시글_조회수_추가_성공() {
        // Given
        ObjectId postId = new ObjectId();
        ObjectId userId = new ObjectId();
        given(redisHealthChecker.isRedisAvailable()).willReturn(true);
        willDoNothing().given(redisHitsService).addHits(postId);
        given(hitsRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        // When
        hitsService.addHits(postId, userId);
        // Then
        ArgumentCaptor<HitsEntity> captor = ArgumentCaptor.forClass(HitsEntity.class);
        verify(hitsRepository).save(captor.capture());
        HitsEntity saved = captor.getValue();
        assertThat(saved.getPostId()).isEqualTo(postId);
        assertThat(saved.getUserId()).isEqualTo(userId);
    }

    @Test
    void 게시글_조회여부_판단_성공() {
        // Given
        ObjectId postId = new ObjectId();
        ObjectId userId = new ObjectId();
        given(hitsRepository.existsByPostIdAndUserId(postId, userId)).willReturn(true);
        // When
        boolean result = hitsService.validateHits(postId, userId);
        // Then
        assertThat(result).isTrue();
    }

    @Test
    void 게시글_조회수_반환_캐시있음() {
        // Given
        ObjectId postId = new ObjectId();
        given(redisHealthChecker.isRedisAvailable()).willReturn(true);
        given(redisHitsService.getHitsCount(postId)).willReturn("5");
        // When
        int result = hitsService.getHitsCount(postId);
        // Then
        assertThat(result).isEqualTo(5);
    }

    @Test
    void 게시글_조회수_반환_캐시없음_DB조회() {
        // Given
        ObjectId postId = new ObjectId();
        given(redisHealthChecker.isRedisAvailable()).willReturn(true);
        given(redisHitsService.getHitsCount(postId)).willReturn(null);
        given(hitsRepository.countAllByPostId(postId)).willReturn(7);
        // When
        int result = hitsService.getHitsCount(postId);
        // Then
        assertThat(result).isEqualTo(7);
    }
} 