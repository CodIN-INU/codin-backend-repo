package inu.codin.codin.domain.post;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.block.service.BlockService;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.service.LikeService;
import inu.codin.codin.domain.post.domain.best.BestEntity;
import inu.codin.codin.domain.post.domain.best.BestRepository;
import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollVoteRepository;
import inu.codin.codin.domain.post.dto.request.*;
import inu.codin.codin.domain.post.dto.response.PostDetailResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostPageItemResponseDTO;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.service.PostService;
import inu.codin.codin.domain.post.service.SeperatedPostService;
import inu.codin.codin.domain.scrap.service.ScrapService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.redis.service.RedisBestService;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.codin.infra.s3.exception.ImageRemoveException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @InjectMocks
    private PostService postService;
    @Mock private PostRepository postRepository;
    @Mock private BestRepository bestRepository;
    @Mock private UserRepository userRepository;
    @Mock private S3Service s3Service;
    @Mock private LikeService likeService;
    @Mock private ScrapService scrapService;
    @Mock private HitsService hitsService;
    @Mock private RedisBestService redisBestService;
    @Mock private BlockService blockService;
    @Mock private SeperatedPostService seperatedPostService;
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
    void 게시글_정상등록_성공() throws Exception {
        // Given
        PostCreateRequestDTO dto = createPostCreateRequestDTO("제목", "내용", true, PostCategory.COMMUNICATION);
        List<MultipartFile> images = new ArrayList<>();
        ObjectId userId = new ObjectId();
        given(SecurityUtils.getCurrentUserId()).willReturn(userId);
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.USER);
        given(seperatedPostService.handleImageUpload(any())).willReturn(new ArrayList<>());
        given(postRepository.save(any())).willAnswer(inv -> {
            PostEntity entity = inv.getArgument(0);
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("_id");
            idField.setAccessible(true);
            idField.set(entity, new ObjectId());
            return entity;
        });
        // When
        Map<String, String> result = postService.createPost(dto, images);
        // Then
        assertThat(result).containsKey("postId");
    }

    @Test
    void 게시글_등록_권한없음_예외() throws Exception {
        // Given
        PostCreateRequestDTO dto = createPostCreateRequestDTO("제목", "내용", true, PostCategory.EXTRACURRICULAR);
        List<MultipartFile> images = new ArrayList<>();
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.USER);
        given(seperatedPostService.handleImageUpload(any())).willReturn(new ArrayList<>());
        // When & Then
        assertThatThrownBy(() -> postService.createPost(dto, images)).isInstanceOf(JwtException.class);
    }

    @Test
    void updatePostContent_success() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PostContentUpdateRequestDTO dto = createPostContentUpdateRequestDTO("수정내용");
        List<MultipartFile> images = new ArrayList<>();
        PostEntity post = createPostEntity();
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        given(seperatedPostService.handleImageUpload(any())).willReturn(new ArrayList<>());
        given(postRepository.save(any())).willReturn(post);
        // When/Then
        assertThatCode(() -> postService.updatePostContent(postId, dto, images)).doesNotThrowAnyException();
    }

    @Test
    void updatePostContent_notFound() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PostContentUpdateRequestDTO dto = createPostContentUpdateRequestDTO("수정내용");
        List<MultipartFile> images = List.of();
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.empty());
        // When/Then
        assertThatThrownBy(() -> postService.updatePostContent(postId, dto, images))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updatePostAnonymous_success() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PostAnonymousUpdateRequestDTO dto = createPostAnonymousUpdateRequestDTO(true);
        PostEntity post = createPostEntity();
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        given(postRepository.save(any())).willReturn(post);
        // When/Then
        assertThatCode(() -> postService.updatePostAnonymous(postId, dto)).doesNotThrowAnyException();
    }

    @Test
    void updatePostStatus_success() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        PostStatusUpdateRequestDTO dto = createPostStatusUpdateRequestDTO(PostStatus.ACTIVE);
        PostEntity post = createPostEntity();
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        given(postRepository.save(any())).willReturn(post);
        // When/Then
        assertThatCode(() -> postService.updatePostStatus(postId, dto)).doesNotThrowAnyException();
    }

    @Test
    void getAllPosts_success() {
        given(blockService.getBlockedUsers()).willReturn(new ArrayList<>());
        List<PostEntity> posts = new ArrayList<>();
        Page<PostEntity> page = new PageImpl<>(posts);
        given(postRepository.getPostsByCategoryWithBlockedUsers(anyString(), anyList(), any())).willReturn(page);
        var response = postService.getAllPosts(PostCategory.COMMUNICATION, 0);
        assertThat(response).isNotNull();
        assertThat(response.getContents()).isInstanceOf(List.class);
    }

    @Test
    void getPostWithDetail_success() {
        // Given
        String postId = new ObjectId().toString();
        PostEntity post = createPostEntity();
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserId()).willReturn(new ObjectId());
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        doNothing().when(seperatedPostService).increaseHitsIfNeeded(any(), any());
        given(seperatedPostService.getLikeCount(any())).willReturn(0);
        given(seperatedPostService.getScrapCount(any())).willReturn(0);
        given(seperatedPostService.getHitsCount(any())).willReturn(0);
        given(userRepository.findById(any())).willReturn(Optional.of(UserEntity.builder().nickname("닉네임").profileImageUrl("url").build()));
        // When
        PostPageItemResponseDTO response = postService.getPostWithDetail(postId);
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPost()).isNotNull();
    }

    @Test
    void softDeletePost_success() {
        // Given-
        String postId = new ObjectId().toString();
        PostEntity post = createPostEntity();
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        given(postRepository.save(any())).willReturn(post);
        // When/Then
        assertThatCode(() -> postService.softDeletePost(postId)).doesNotThrowAnyException();
    }

    @Test
    void deletePostImage_success() throws Exception {
        // Given
        String postId = new ObjectId().toString();
        String imageUrl = "img.jpg";
        List<String> imageList = new ArrayList<>(List.of(imageUrl));
        PostEntity post = PostEntity.builder()._id(new ObjectId()).userId(new ObjectId()).postCategory(PostCategory.COMMUNICATION).postImageUrls(imageList).build();
        given(postRepository.findByIdAndNotDeleted(any())).willReturn(Optional.of(post));
        given(SecurityUtils.getCurrentUserRole()).willReturn(UserRole.ADMIN);
        doNothing().when(seperatedPostService).deletePostImageInternal(any(), any());

        // When/Then
        assertThatCode(() -> postService.deletePostImage(postId, imageUrl)).doesNotThrowAnyException();
    }

    @Test
    void searchPosts_success() {
        given(blockService.getBlockedUsers()).willReturn(new ArrayList<>());
        List<PostEntity> posts = new ArrayList<>();
        Page<PostEntity> page = new PageImpl<>(posts);
        given(postRepository.findAllByKeywordAndDeletedAtIsNull(anyString(), anyList(), any())).willReturn(page);
        PostPageResponse response = postService.searchPosts("테스트", 0);
        assertThat(response).isNotNull();
    }

    @Test
    void getTop3BestPosts_success() {
        given(seperatedPostService.getTop3BestPostsInternal()).willReturn(new ArrayList<>());
        var result = postService.getTop3BestPosts();
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(List.class);
    }

    @Test
    void getBestPosts_success() {
        given(seperatedPostService.getBestPostsInternal(anyInt())).willReturn(new PageImpl<>(new ArrayList<>()));
        var result = postService.getBestPosts(0);
        assertThat(result).isNotNull();
        assertThat(result.getContents()).isInstanceOf(List.class);
    }

    // --- 리플렉션 기반 DTO 생성 유틸리티 ---
    private PostCreateRequestDTO createPostCreateRequestDTO(String title, String content, boolean anonymous, PostCategory category) throws Exception {
        PostCreateRequestDTO dto = PostCreateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "title", title);
        setField(dto, "content", content);
        setField(dto, "anonymous", anonymous);
        setField(dto, "postCategory", category);
        return dto;
    }
    private PostContentUpdateRequestDTO createPostContentUpdateRequestDTO(String content) throws Exception {
        PostContentUpdateRequestDTO dto = PostContentUpdateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "content", content);
        return dto;
    }
    private PostAnonymousUpdateRequestDTO createPostAnonymousUpdateRequestDTO(boolean anonymous) throws Exception {
        PostAnonymousUpdateRequestDTO dto = PostAnonymousUpdateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "anonymous", anonymous);
        return dto;
    }
    private PostStatusUpdateRequestDTO createPostStatusUpdateRequestDTO(PostStatus status) throws Exception {
        PostStatusUpdateRequestDTO dto = PostStatusUpdateRequestDTO.class.getDeclaredConstructor().newInstance();
        setField(dto, "postStatus", status);
        return dto;
    }
    private void setField(Object target, String field, Object value) throws Exception {
        java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    // 모든 PostEntity fixture에 _id 세팅
    private PostEntity createPostEntity() {
        return PostEntity.builder()
                ._id(new ObjectId())
                .userId(new ObjectId())
                .postCategory(PostCategory.COMMUNICATION)
                .build();
    }
} 