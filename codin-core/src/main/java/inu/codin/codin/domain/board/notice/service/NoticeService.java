package inu.codin.codin.domain.board.notice.service;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.board.notice.dto.request.NoticeCreateUpdateRequestDTO;
import inu.codin.codin.domain.board.notice.dto.response.NoticeDetailResponseDto;
import inu.codin.codin.domain.board.notice.dto.response.NoticePageResponse;
import inu.codin.codin.domain.board.notice.exception.NoticeErrorCode;
import inu.codin.codin.domain.board.notice.exception.NoticeException;
import inu.codin.codin.domain.board.notice.repository.NoticeRepository;
import inu.codin.codin.domain.post.domain.hits.service.HitsService;
import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.scrap.service.ScrapService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    private final ScrapService scrapService;
    private final HitsService hitsService;
    private final S3Service s3Service;

    /**
     * 공지사항 페이징 조회
     * @param department 학과
     * @param pageNumber 페이지 번호 (0부터 시작)
     * @return NoticePageResponse 공지사항 페이지 응답
     */
    public NoticePageResponse getAllNotices(Department department, int pageNumber) {
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        List<String> postCategories = List.of(
                PostCategory.EXTRACURRICULAR_INNER.name(),
                PostCategory.DEPARTMENT_NOTICE.name()
        );
        String regex = "^\\[" + Pattern.quote(department.getAbbreviation()) + "\\]";
        Page<PostEntity> notices = noticeRepository.getNoticesByCategory(regex, postCategories, pageRequest);
        return NoticePageResponse.of(getNoticeDetailResponse(notices.getContent()), notices.getTotalPages() - 1, notices.hasNext() ? notices.getPageable().getPageNumber() + 1 : -1);
    }

    /**
     * 공지사항 상세 조회
     * @param postId 공지사항 ID
     * @return NoticeDetailResponseDto 공지사항 상세 정보
     */
    public NoticeDetailResponseDto getNoticesWithDetail(String postId) {
        PostEntity notice = noticeRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND));
        return getNoticeWithDetail(notice);
    }

    /**
     * 공지사항 작성
     * @param noticeCreateUpdateRequestDTO 공지사항 작성 요청 DTO
     * @param noticeImages 공지사항 이미지 파일 리스트
     * @return Map<String, String> 공지사항 ID를 포함한 응답
     */
    public Map<String, String> createNotice(NoticeCreateUpdateRequestDTO noticeCreateUpdateRequestDTO, List<MultipartFile> noticeImages) {
        List<String> imageUrls = s3Service.handleImageUpload(noticeImages);
        ObjectId userId = SecurityUtils.getCurrentUserId();

        validateUserAndPost(userId);
        UserEntity user = getUserEntity(userId);
        String prefixOfTitle = "[" + user.getDepartment().getAbbreviation() + "]"; //작성자의 학과에 따라 title의 prefix를 붙여줌

        PostEntity noticeEntity = PostEntity.builder()
                    .userId(userId)
                    .title(prefixOfTitle + noticeCreateUpdateRequestDTO.getTitle())
                    .content(noticeCreateUpdateRequestDTO.getContent())
                    .postImageUrls(imageUrls)
                    .isAnonymous(false) //공지사항은 항상 실명으로 업로드
                    .postCategory(PostCategory.DEPARTMENT_NOTICE)
                    .postStatus(PostStatus.ACTIVE)
                    .build();
        noticeRepository.save(noticeEntity);
        Map<String, String> response = new HashMap<>();
        response.put("postId", noticeEntity.get_id().toString());
        return response;
    }

    /**
     * 공지사항 내용 수정
     * @param postId 공지사항 ID
     * @param noticeCreateUpdateRequestDTO 공지사항 수정 요청 DTO
     * @param noticeImages 공지사항 이미지 파일 리스트
     */
    public void updateNoticeContent(String postId, NoticeCreateUpdateRequestDTO noticeCreateUpdateRequestDTO, List<MultipartFile> noticeImages) {
        PostEntity post = noticeRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(()-> new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND));

        validateUserAndPost(post.getUserId());

        List<String> imageUrls = s3Service.handleImageUpload(noticeImages);
        post.updateNotice(noticeCreateUpdateRequestDTO.getTitle(), noticeCreateUpdateRequestDTO.getContent(), imageUrls);
        noticeRepository.save(post);
    }

    private List<NoticeDetailResponseDto> getNoticeDetailResponse(List<PostEntity> content) {
        return content.stream()
                .map(this::getNoticeWithDetail)
                .toList();
    }

    private NoticeDetailResponseDto getNoticeWithDetail(PostEntity post) {
        UserEntity user = getUserEntity(post.getUserId());
        int scrapCount = scrapService.getScrapCount(post.get_id());
        int hitsCount = hitsService.getHitsCount(post.get_id());
        NoticeDetailResponseDto.UserInfo userInfo = getUserInfoAboutNotice(post.getUserId(), post.get_id());

        return NoticeDetailResponseDto.of(post, user.getNickname(), user.getProfileImageUrl(), scrapCount, hitsCount, userInfo);
    }

    private UserEntity getUserEntity(ObjectId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    }

    private NoticeDetailResponseDto.UserInfo getUserInfoAboutNotice(ObjectId postUserId, ObjectId postId){
        ObjectId userId = SecurityUtils.getCurrentUserId();
        return NoticeDetailResponseDto.UserInfo.builder()
                .isScrap(scrapService.isPostScraped(postId, userId))
                .isMine(postUserId.equals(userId))
                .build();
    }

    private void validateUserAndPost(ObjectId postUserId) {
        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER)) {
            throw new NoticeException(NoticeErrorCode.NOTICE_ACCESS_DENIED);
        }
        SecurityUtils.validateUser(postUserId);
    }
}
