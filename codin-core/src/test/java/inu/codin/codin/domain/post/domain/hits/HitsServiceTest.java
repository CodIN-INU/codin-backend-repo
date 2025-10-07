package inu.codin.codin.domain.post.domain.hits;

import inu.codin.codin.domain.post.domain.hits.entity.HitsEntity;
import inu.codin.codin.domain.post.domain.hits.repository.HitsRepository;
import inu.codin.codin.domain.post.domain.hits.service.HitsService;
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
    void addHits_Redis사용가능_Redis와DB모두저장() {
        // Given
        ObjectId postId = new ObjectId();
        ObjectId userId = new ObjectId();
        
        given(redisHealthChecker.isRedisAvailable()).willReturn(true);
        doNothing().when(redisHitsService).addHits(postId);
        given(hitsRepository.save(any(HitsEntity.class))).willReturn(createHitsEntity());
        
        // When & Then
        assertThatCode(() -> hitsService.addHits(postId, userId)).doesNotThrowAnyException();
        verify(redisHitsService).addHits(postId);
        verify(hitsRepository).save(any(HitsEntity.class));
    }
    
    @Test
    void addHits_Redis사용불가_DB만저장() {
        // Given
        ObjectId postId = new ObjectId();
        ObjectId userId = new ObjectId();
        
        given(redisHealthChecker.isRedisAvailable()).willReturn(false);
        given(hitsRepository.save(any(HitsEntity.class))).willReturn(createHitsEntity());
        
        // When & Then
        assertThatCode(() -> hitsService.addHits(postId, userId)).doesNotThrowAnyException();
        verify(redisHitsService, never()).addHits(any());
        verify(hitsRepository).save(any(HitsEntity.class));
    }
    
    @Test
    void addHits_다른사용자_각각저장() {
        // Given
        ObjectId postId = new ObjectId();
        ObjectId userId1 = new ObjectId();
        ObjectId userId2 = new ObjectId();
        
        given(redisHealthChecker.isRedisAvailable()).willReturn(true);
        doNothing().when(redisHitsService).addHits(postId);
        given(hitsRepository.save(any(HitsEntity.class))).willReturn(createHitsEntity());
        
        // When
        hitsService.addHits(postId, userId1);
        hitsService.addHits(postId, userId2);
        
        // Then
        verify(redisHitsService, times(2)).addHits(postId);
        verify(hitsRepository, times(2)).save(any(HitsEntity.class));
    }
    
    @Test
    void validateHits_이미조회함_true반환() {
        // Given
        ObjectId postId = new ObjectId();
        ObjectId userId = new ObjectId();
        
        given(hitsRepository.existsByPostIdAndUserId(postId, userId)).willReturn(true);
        
        // When
        boolean result = hitsService.validateHits(postId, userId);
        
        // Then
        assertThat(result).isTrue();
        verify(hitsRepository).existsByPostIdAndUserId(postId, userId);
    }
    
    @Test
    void validateHits_조회안함_false반환() {
        // Given
        ObjectId postId = new ObjectId();
        ObjectId userId = new ObjectId();
        
        given(hitsRepository.existsByPostIdAndUserId(postId, userId)).willReturn(false);
        
        // When
        boolean result = hitsService.validateHits(postId, userId);
        
        // Then
        assertThat(result).isFalse();
        verify(hitsRepository).existsByPostIdAndUserId(postId, userId);
    }
    
    @Test
    void validateHits_다른사용자_각각검증() {
        // Given
        ObjectId postId = new ObjectId();
        ObjectId userId1 = new ObjectId();
        ObjectId userId2 = new ObjectId();
        
        given(hitsRepository.existsByPostIdAndUserId(postId, userId1)).willReturn(true);
        given(hitsRepository.existsByPostIdAndUserId(postId, userId2)).willReturn(false);
        
        // When
        boolean result1 = hitsService.validateHits(postId, userId1);
        boolean result2 = hitsService.validateHits(postId, userId2);
        
        // Then
        assertThat(result1).isTrue();
        assertThat(result2).isFalse();
        verify(hitsRepository).existsByPostIdAndUserId(postId, userId1);
        verify(hitsRepository).existsByPostIdAndUserId(postId, userId2);
    }
    
    @Test
    void getHitsCount_Redis캐시히트_Redis값반환() {
        // Given
        ObjectId postId = new ObjectId();
        String cachedHits = "10";
        
        given(redisHealthChecker.isRedisAvailable()).willReturn(true);
        given(redisHitsService.getHitsCount(postId)).willReturn(cachedHits);
        
        // When
        int result = hitsService.getHitsCount(postId);
        
        // Then
        assertThat(result).isEqualTo(10);
        verify(redisHitsService).getHitsCount(postId);
        verify(hitsRepository, never()).countAllByPostId(any());
    }
    
    @Test
    void getHitsCount_Redis캐시미스_DB조회후복구() {
        // Given
        ObjectId postId = new ObjectId();
        int dbHitsCount = 15;
        
        given(redisHealthChecker.isRedisAvailable()).willReturn(true);
        given(redisHitsService.getHitsCount(postId)).willReturn(null); // 캐시 미스
        given(hitsRepository.countAllByPostId(postId)).willReturn(dbHitsCount);
        doNothing().when(redisHitsService).recoveryHits(postId, dbHitsCount);
        
        // When
        int result = hitsService.getHitsCount(postId);
        
        // Then
        assertThat(result).isEqualTo(15);
        verify(redisHitsService).getHitsCount(postId);
        verify(hitsRepository, atLeast(1)).countAllByPostId(postId); // @Async recoveryHits도 호출하므로 최소 1번
        // recoveryHits는 @Async이므로 직접 검증하기 어려움
    }
    
    @Test
    void getHitsCount_Redis사용불가_DB조회() {
        // Given
        ObjectId postId = new ObjectId();
        int dbHitsCount = 7;
        
        given(redisHealthChecker.isRedisAvailable()).willReturn(false);
        given(hitsRepository.countAllByPostId(postId)).willReturn(dbHitsCount);
        
        // When
        int result = hitsService.getHitsCount(postId);
        
        // Then
        assertThat(result).isEqualTo(7);
        verify(redisHitsService, never()).getHitsCount(any());
        verify(hitsRepository, atLeast(1)).countAllByPostId(postId); // @Async recoveryHits도 호출하므로 최소 1번
        // recoveryHits는 @Async이므로 직접 검증하기 어려움
    }
    
    @Test
    void getHitsCount_Redis캐시0_정상반환() {
        // Given
        ObjectId postId = new ObjectId();
        String cachedHits = "0";
        
        given(redisHealthChecker.isRedisAvailable()).willReturn(true);
        given(redisHitsService.getHitsCount(postId)).willReturn(cachedHits);
        
        // When
        int result = hitsService.getHitsCount(postId);
        
        // Then
        assertThat(result).isEqualTo(0);
        verify(redisHitsService).getHitsCount(postId);
        verify(hitsRepository, never()).countAllByPostId(any());
    }
    
    @Test
    void getHitsCount_DB조회0_정상반환() {
        // Given
        ObjectId postId = new ObjectId();
        
        given(redisHealthChecker.isRedisAvailable()).willReturn(true);
        given(redisHitsService.getHitsCount(postId)).willReturn(null);
        given(hitsRepository.countAllByPostId(postId)).willReturn(0);
        
        // When
        int result = hitsService.getHitsCount(postId);
        
        // Then
        assertThat(result).isEqualTo(0);
        verify(hitsRepository, atLeast(1)).countAllByPostId(postId); // @Async recoveryHits도 호출하므로 최소 1번
    }
    
    @Test
    void getHitsCount_여러포스트_각각조회() {
        // Given
        ObjectId postId1 = new ObjectId();
        ObjectId postId2 = new ObjectId();
        ObjectId postId3 = new ObjectId();
        
        given(redisHealthChecker.isRedisAvailable()).willReturn(true);
        given(redisHitsService.getHitsCount(postId1)).willReturn("5");
        given(redisHitsService.getHitsCount(postId2)).willReturn("12");
        given(redisHitsService.getHitsCount(postId3)).willReturn("0");
        
        // When
        int result1 = hitsService.getHitsCount(postId1);
        int result2 = hitsService.getHitsCount(postId2);
        int result3 = hitsService.getHitsCount(postId3);
        
        // Then
        assertThat(result1).isEqualTo(5);
        assertThat(result2).isEqualTo(12);
        assertThat(result3).isEqualTo(0);
        verify(redisHitsService).getHitsCount(postId1);
        verify(redisHitsService).getHitsCount(postId2);
        verify(redisHitsService).getHitsCount(postId3);
    }
    
    // Helper methods
    private HitsEntity createHitsEntity() {
        HitsEntity hitsEntity = HitsEntity.builder()
                .postId(new ObjectId())
                .userId(new ObjectId())
                .build();
        setIdFieldSafely(hitsEntity, new ObjectId());
        return hitsEntity;
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