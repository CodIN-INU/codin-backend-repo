package inu.codin.codin.domain.post;

import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.exception.PostException;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.service.PostInteractionService;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.infra.s3.exception.ImageRemoveException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostInteractionServiceTest {
    
    @InjectMocks
    private PostInteractionService postInteractionService;
    
    @Mock private S3Service s3Service;
    @Mock private PostRepository postRepository;
    @Mock private HitsService hitsService;
    
    @Test
    void handleImageUpload_이미지업로드_성공() {
        // Given
        List<MultipartFile> images = Arrays.asList(
            mock(MultipartFile.class),
            mock(MultipartFile.class)
        );
        List<String> expectedUrls = Arrays.asList("image1.jpg", "image2.jpg");
        
        given(s3Service.handleImageUpload(images)).willReturn(expectedUrls);
        
        // When
        List<String> result = postInteractionService.handleImageUpload(images);
        
        // Then
        assertThat(result).isEqualTo(expectedUrls);
        verify(s3Service).handleImageUpload(images);
    }
    
    @Test
    void handleImageUpload_빈리스트_빈리스트반환() {
        // Given
        List<MultipartFile> emptyImages = new ArrayList<>();
        List<String> emptyUrls = new ArrayList<>();
        
        given(s3Service.handleImageUpload(emptyImages)).willReturn(emptyUrls);
        
        // When
        List<String> result = postInteractionService.handleImageUpload(emptyImages);
        
        // Then
        assertThat(result).isEmpty();
        verify(s3Service).handleImageUpload(emptyImages);
    }
    
    @Test
    void deletePostImageInternal_정상삭제_성공() {
        // Given
        String imageUrl = "test-image.jpg";
        PostEntity post = createPostEntityWithImages(Arrays.asList(imageUrl, "other-image.jpg"));
        
        doNothing().when(s3Service).deleteFile(imageUrl);
        given(postRepository.save(any())).willReturn(post);
        
        // When
        assertThatCode(() -> postInteractionService.deletePostImageInternal(post, imageUrl))
                .doesNotThrowAnyException();
        
        // Then
        verify(s3Service).deleteFile(imageUrl);
        verify(postRepository).save(post);
    }
    
    @Test
    void deletePostImageInternal_이미지없음_예외() {
        // Given
        String nonExistentImageUrl = "non-existent.jpg";
        PostEntity post = createPostEntityWithImages(Arrays.asList("image1.jpg", "image2.jpg"));
        
        // When & Then
        assertThatThrownBy(() -> postInteractionService.deletePostImageInternal(post, nonExistentImageUrl))
                .isInstanceOf(PostException.class);
        
        verify(s3Service, never()).deleteFile(any());
        verify(postRepository, never()).save(any());
    }
    
    @Test
    void deletePostImageInternal_S3삭제실패_예외() {
        // Given
        String imageUrl = "test-image.jpg";
        PostEntity post = createPostEntityWithImages(Arrays.asList(imageUrl));
        
        doThrow(new RuntimeException("S3 삭제 실패")).when(s3Service).deleteFile(imageUrl);
        
        // When & Then
        assertThatThrownBy(() -> postInteractionService.deletePostImageInternal(post, imageUrl))
                .isInstanceOf(ImageRemoveException.class)
                .hasMessageContaining("이미지 삭제 중 오류 발생");
        
        verify(s3Service).deleteFile(imageUrl);
        verify(postRepository, never()).save(any());
    }
    
    @Test
    void increaseHits_조회수증가_성공() {
        // Given
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        
        given(hitsService.validateHits(post.get_id(), userId)).willReturn(false);
        doNothing().when(hitsService).addHits(post.get_id(), userId);
        
        // When
        postInteractionService.increaseHits(post, userId);
        
        // Then
        verify(hitsService).validateHits(post.get_id(), userId);
        verify(hitsService).addHits(post.get_id(), userId);
    }
    
    @Test
    void increaseHits_이미조회함_조회수증가안함() {
        // Given
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        
        given(hitsService.validateHits(post.get_id(), userId)).willReturn(true);
        
        // When
        postInteractionService.increaseHits(post, userId);
        
        // Then
        verify(hitsService).validateHits(post.get_id(), userId);
        verify(hitsService, never()).addHits(any(), any());
    }
    
    @Test
    void increaseHits_동일사용자_중복조회방지() {
        // Given
        PostEntity post = createPostEntity();
        ObjectId userId = new ObjectId();
        
        // 첫 번째 호출: 조회수 증가
        given(hitsService.validateHits(post.get_id(), userId))
            .willReturn(false)  // 첫 번째는 증가
            .willReturn(true);  // 두 번째는 증가하지 않음
        doNothing().when(hitsService).addHits(post.get_id(), userId);
        
        // When
        postInteractionService.increaseHits(post, userId);  // 첫 번째 호출
        postInteractionService.increaseHits(post, userId);  // 두 번째 호출
        
        // Then
        verify(hitsService, times(2)).validateHits(post.get_id(), userId);
        verify(hitsService, times(1)).addHits(post.get_id(), userId);  // 한 번만 호출
    }
    
    @Test
    void increaseHits_다른사용자_각각증가() {
        // Given
        PostEntity post = createPostEntity();
        ObjectId userId1 = new ObjectId();
        ObjectId userId2 = new ObjectId();
        
        given(hitsService.validateHits(any(), any())).willReturn(false);
        doNothing().when(hitsService).addHits(any(), any());
        
        // When
        postInteractionService.increaseHits(post, userId1);
        postInteractionService.increaseHits(post, userId2);
        
        // Then
        verify(hitsService).validateHits(post.get_id(), userId1);
        verify(hitsService).validateHits(post.get_id(), userId2);
        verify(hitsService).addHits(post.get_id(), userId1);
        verify(hitsService).addHits(post.get_id(), userId2);
    }
    
    // Helper methods
    private PostEntity createPostEntity() {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .build();
        setIdField(post, new ObjectId());
        return post;
    }
    
    private PostEntity createPostEntityWithImages(List<String> imageUrls) {
        PostEntity post = PostEntity.builder()
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .postImageUrls(new ArrayList<>(imageUrls))
                .build();
        setIdField(post, new ObjectId());
        return post;
    }
    
    private void setIdField(PostEntity entity, ObjectId id) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("_id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID field", e);
        }
    }
}