package inu.codin.codin.domain.post.domain.best;

import inu.codin.codin.infra.redis.service.RedisBestService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class BestServiceTest {
    
    @InjectMocks
    private BestService bestService;
    
    @Mock private RedisBestService redisBestService;
    @Mock private BestRepository bestRepository;
    
    @Test
    void getTop3BestPostIds_정상조회_성공() {
        // Given
        Map<String, Double> bestPosts = new LinkedHashMap<>();
        bestPosts.put("postId1", 100.0);
        bestPosts.put("postId2", 90.0);
        bestPosts.put("postId3", 80.0);
        
        given(redisBestService.getBests()).willReturn(bestPosts);
        
        // When
        List<String> result = bestService.getTop3BestPostIds();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("postId1", "postId2", "postId3");
        verify(redisBestService).getBests();
    }
    
    @Test
    void getTop3BestPostIds_빈맵_빈리스트반환() {
        // Given
        Map<String, Double> emptyBestPosts = new HashMap<>();
        
        given(redisBestService.getBests()).willReturn(emptyBestPosts);
        
        // When
        List<String> result = bestService.getTop3BestPostIds();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(redisBestService).getBests();
    }
    
    @Test
    void getTop3BestPostIds_일부데이터_정상반환() {
        // Given
        Map<String, Double> bestPosts = new LinkedHashMap<>();
        bestPosts.put("postId1", 50.0);
        
        given(redisBestService.getBests()).willReturn(bestPosts);
        
        // When
        List<String> result = bestService.getTop3BestPostIds();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly("postId1");
        verify(redisBestService).getBests();
    }
    
    @Test
    void getBestEntities_페이징조회_성공() {
        // Given
        int pageNumber = 0;
        List<BestEntity> bestEntities = Arrays.asList(
            createBestEntity(),
            createBestEntity(),
            createBestEntity()
        );
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<BestEntity> page = new PageImpl<>(bestEntities, pageRequest, 3);
        
        given(bestRepository.findAll(any(PageRequest.class))).willReturn(page);
        
        // When
        Page<BestEntity> result = bestService.getBestEntities(pageNumber);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(0);
        verify(bestRepository).findAll(any(PageRequest.class));
    }
    
    @Test
    void getBestEntities_빈페이지_빈결과반환() {
        // Given
        int pageNumber = 5; // 존재하지 않는 페이지
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<BestEntity> emptyPage = new PageImpl<>(new ArrayList<>(), pageRequest, 0);
        
        given(bestRepository.findAll(any(PageRequest.class))).willReturn(emptyPage);
        
        // When
        Page<BestEntity> result = bestService.getBestEntities(pageNumber);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(bestRepository).findAll(any(PageRequest.class));
    }
    
    @Test
    void deleteBestPost_정상삭제_성공() {
        // Given
        String postId = new ObjectId().toString();
        
        doNothing().when(redisBestService).deleteBest(postId);
        
        // When & Then
        assertThatCode(() -> bestService.deleteBestPost(postId)).doesNotThrowAnyException();
        verify(redisBestService).deleteBest(postId);
    }
    
    @Test
    void deleteBestPost_다른postId_정상호출() {
        // Given
        String postId1 = new ObjectId().toString();
        String postId2 = new ObjectId().toString();
        
        doNothing().when(redisBestService).deleteBest(anyString());
        
        // When
        bestService.deleteBestPost(postId1);
        bestService.deleteBestPost(postId2);
        
        // Then
        verify(redisBestService).deleteBest(postId1);
        verify(redisBestService).deleteBest(postId2);
        verify(redisBestService, times(2)).deleteBest(anyString());
    }
    
    @Test
    void applyBestScore_정상적용_성공() {
        // Given
        ObjectId postId = new ObjectId();
        
        doNothing().when(redisBestService).applyBestScore(anyInt(), any(ObjectId.class));
        
        // When & Then
        assertThatCode(() -> bestService.applyBestScore(postId)).doesNotThrowAnyException();
        verify(redisBestService).applyBestScore(1, postId);
    }
    
    @Test
    void applyBestScore_여러postId_각각적용() {
        // Given
        ObjectId postId1 = new ObjectId();
        ObjectId postId2 = new ObjectId();
        ObjectId postId3 = new ObjectId();
        
        doNothing().when(redisBestService).applyBestScore(anyInt(), any(ObjectId.class));
        
        // When
        bestService.applyBestScore(postId1);
        bestService.applyBestScore(postId2);
        bestService.applyBestScore(postId3);
        
        // Then
        verify(redisBestService).applyBestScore(1, postId1);
        verify(redisBestService).applyBestScore(1, postId2);
        verify(redisBestService).applyBestScore(1, postId3);
        verify(redisBestService, times(3)).applyBestScore(eq(1), any(ObjectId.class));
    }
    
    @Test
    void applyBestScore_스코어값확인_항상1() {
        // Given
        ObjectId postId = new ObjectId();
        
        doNothing().when(redisBestService).applyBestScore(anyInt(), any(ObjectId.class));
        
        // When
        bestService.applyBestScore(postId);
        
        // Then
        verify(redisBestService).applyBestScore(eq(1), eq(postId)); // 스코어는 항상 1
    }
    
    // Helper methods
    private BestEntity createBestEntity() {
        BestEntity bestEntity = BestEntity.builder()
                .postId(new ObjectId())
                .build();
        setIdFieldSafely(bestEntity, new ObjectId());
        return bestEntity;
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