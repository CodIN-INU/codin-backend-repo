package inu.codin.codin.domain.user.service;

import inu.codin.codin.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.like.entity.LikeType;
import inu.codin.codin.domain.like.repository.LikeRepository;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.service.PostDtoAssembler;
import inu.codin.codin.domain.scrap.entity.ScrapEntity;
import inu.codin.codin.domain.scrap.repository.ScrapRepository;
import inu.codin.codin.domain.user.dto.request.*;
import inu.codin.codin.domain.user.dto.response.UserInfoResponseDto;
import inu.codin.codin.domain.user.dto.response.UserTicketingParticipationInfoResponse;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.exception.AdminInfoUpdateNotAllowedException;
import inu.codin.codin.domain.user.exception.UserNicknameDuplicateException;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import inu.codin.common.exception.NotFoundException;
import inu.codin.common.util.CookieUtil;
import inu.codin.security.entity.UserRole;
import inu.codin.security.service.JwtService;
import inu.codin.security.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static inu.codin.common.util.ObjectIdUtil.toObjectId;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final ScrapRepository scrapRepository;
    private final CommentRepository commentRepository;

    private final PostDtoAssembler postDtoAssembler;
    private final S3Service s3Service;
    private final JwtService jwtService;

    @Value("${server.domain}")
    private String BASEURL;

    //해당 유저가 작성한 모든 글 반환 :: 게시글 내용 + 댓글+대댓글의 수 + 좋아요,스크랩 count 수 반환
    public PostPageResponse getAllUserPosts(int pageNumber) {
        ObjectId userId = toObjectId(SecurityUtil.getCurrentUserId());
        log.info("[게시글 조회] 유저 ID: {}, 페이지 번호: {}", userId, pageNumber);

        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page = postRepository.findAllByUserIdOrderByCreatedAt(userId, pageRequest);

        log.info("[게시글 조회 성공] 조회된 게시글 수: {}, 총 페이지 수: {}", page.getContent().size(), page.getTotalPages());
        return PostPageResponse.of(
                postDtoAssembler.toPageItemList(page.getContent()),
                page.getTotalPages() - 1,
                page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1
        );
    }

    public PostPageResponse getPostUserInteraction(int pageNumber, InteractionType interactionType) {
        ObjectId userId = toObjectId(SecurityUtil.getCurrentUserId());
        log.info("[유저 상호작용 조회] 유저 ID: {}, 타입: {}, 페이지 번호: {}", userId, interactionType, pageNumber);

        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        switch (interactionType) {
            case LIKE -> {
                log.info("[좋아요 조회 시작] 유저 ID: {}, 타입: {}", userId, interactionType);
                Page<LikeEntity> likePage = likeRepository.findAllByUserIdAndLikeTypeAndDeletedAtIsNullOrderByCreatedAt(userId, LikeType.valueOf("POST"), pageRequest);

                List<ObjectId> postIds = likePage.getContent().stream()
                        .map(likeEntity -> new ObjectId(likeEntity.getLikeTypeId()))
                        .toList();
                List<PostEntity> postUserLike = postRepository.findBy_idInAndDeletedAtIsNull(postIds);

                log.info("[좋아요 조회 완료] 총 페이지 수: {}, 다음 페이지 여부: {}", likePage.getTotalPages(), likePage.hasNext());
                return PostPageResponse.of(postDtoAssembler.toPageItemList(postUserLike), likePage.getTotalPages() - 1, likePage.hasNext() ? likePage.getPageable().getPageNumber() + 1 : -1);
            }
            case SCRAP -> {
                log.info("[스크랩 조회 시작] 유저 ID: {}, 타입: {}", userId, interactionType);
                Page<ScrapEntity> scrapPage = scrapRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAt(userId, pageRequest);

                List<ObjectId> postIds = scrapPage.getContent().stream()
                        .map(ScrapEntity::getPostId)
                        .toList();
                List<PostEntity> postUserScrap = postRepository.findBy_idInAndDeletedAtIsNull(postIds);

                log.info("[스크랩 조회 완료] 총 페이지 수: {}, 다음 페이지 여부: {}", scrapPage.getTotalPages(), scrapPage.hasNext());
                return PostPageResponse.of(postDtoAssembler.toPageItemList(postUserScrap), scrapPage.getTotalPages() - 1, scrapPage.hasNext() ? scrapPage.getPageable().getPageNumber() + 1 : -1);
            }
            case COMMENT -> {
                log.info("[댓글 조회 시작] 유저 ID: {}, 타입: {}", userId, interactionType);
                Page<CommentEntity> commentPage = commentRepository.findAllByUserIdOrderByCreatedAt(userId, pageRequest);

                List<ObjectId> commentedPostIds = commentPage.getContent().stream()
                        .map(CommentEntity::getPostId)
                        .distinct() // 한 게시글에 여러 댓글을 달았을 경우 중복 제거
                        .toList();
                List<PostEntity> postUserComment = postRepository.findBy_idInAndDeletedAtIsNull(commentedPostIds);

                log.info("[댓글 조회 완료] 총 페이지 수: {}, 다음 페이지 여부: {}", commentPage.getTotalPages(), commentPage.hasNext());
                return PostPageResponse.of(postDtoAssembler.toPageItemList(postUserComment), commentPage.getTotalPages() - 1, commentPage.hasNext() ? commentPage.getPageable().getPageNumber() + 1 : -1);
            }
            default -> {
                log.warn("[유효하지 않은 상호작용 타입] 유저 ID: {}, 상호작용 타입: {}", userId, interactionType);
                throw new IllegalArgumentException("지원하지 않는 타입입니다.");
            }
        }
    }

    public void deleteUser(HttpServletRequest request, HttpServletResponse response) {
        ObjectId userId = toObjectId(SecurityUtil.getCurrentUserId());

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("[회원 탈퇴 실패] 유저 정보 없음: {}", userId);
                    return new NotFoundException("해당 id에 대한 유저 정보를 찾을 수 없습니다.");
                });
        user.delete();
        user.updateNickname("탈퇴한 사용자");
        user.updateProfileImageUrl(s3Service.getDefaultProfileImageUrl());
        userRepository.save(user);

        String domain = BASEURL.replaceFirst("https?://", "").split(":")[0];
        CookieUtil.expireCookie(response, "x-access-token", domain);
        CookieUtil.expireCookie(response, "x-refresh-token", domain);

        log.info("[회원 탈퇴 성공] _id: {}", userId);
    }

    public UserInfoResponseDto getUserInfo() {
        ObjectId userId = toObjectId(SecurityUtil.getCurrentUserId());
        log.info("[유저 정보 조회] 유저 ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[유저 정보 조회 실패] 유저 정보 없음: {}", userId);
                    return new NotFoundException("유저 정보를 찾을 수 없습니다.");
                });

        log.info("[유저 정보 조회 성공] 닉네임: {}", user.getNickname());
        return UserInfoResponseDto.of(user);
    }

    @Transactional
    public void setUserInfo(SetUserInfoRequestDto setUserInfoRequestDto) {
        ObjectId userId = toObjectId(SecurityUtil.getCurrentUserId());
        log.info("[유저 정보 조회] 유저 ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[유저 정보 조회 실패] 유저 정보 없음: {}", userId);
                    return new NotFoundException("유저 정보를 찾을 수 없습니다.");
                });

        user.setUserInfo(setUserInfoRequestDto);
        userRepository.save(user);
    }

    @Transactional
    public void updateUserInfo(UpdateUserInfoRequestDto updateUserInfoRequestDto) {
        ObjectId userId = toObjectId(SecurityUtil.getCurrentUserId());
        log.info("[유저 정보 업데이트] 현재 사용자 ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[유저 정보 찾기 실패] 유저 정보를 찾을 수 없음. 사용자 ID: {}", userId);
                    return new NotFoundException("유저 정보를 찾을 수 없습니다.");
                });

        if (user.getRole() == UserRole.MANAGER) {
            log.warn("[유저 정보 업데이트 실패] 관리자 계정은 정보 수정이 불가능합니다. 사용자 ID: {}", userId);
            throw new AdminInfoUpdateNotAllowedException("관리자 계정은 정보 수정이 불가능합니다.");
        }

        if (updateUserInfoRequestDto.nickname() != null && !updateUserInfoRequestDto.nickname().equals(user.getNickname())) {
            Optional<UserEntity> nickNameDuplicate = userRepository.findByNicknameAndDeletedAtIsNull(updateUserInfoRequestDto.nickname());
            if (nickNameDuplicate.isPresent()){
                throw new UserNicknameDuplicateException("이미 사용중인 닉네임입니다.");
            }

            user.updateNickname(updateUserInfoRequestDto.nickname());
        }

        if (updateUserInfoRequestDto.name() != null) {
            String newName = updateUserInfoRequestDto.name().trim();
            if (newName.length() > 10) {
                throw new IllegalArgumentException("이름은 10자 이하여야 합니다.");
            }
            if (!newName.matches("^[가-힣a-zA-Z]+$")) {
                throw new IllegalArgumentException("이름은 한글 또는 영어만 입력 가능합니다.");
            }

            user.updateName(newName);
        }

        if (updateUserInfoRequestDto.college() != null) {
            user.updateCollege(updateUserInfoRequestDto.college());
        }
        if (updateUserInfoRequestDto.department() != null) {
            user.updateDepartment(updateUserInfoRequestDto.department());
        }
        if (updateUserInfoRequestDto.studentId() != null) {
            user.updateStudentId(updateUserInfoRequestDto.studentId());
        }

        userRepository.save(user);
        log.info("[유저 정보 업데이트 성공] 사용자 ID: {}, 업데이트된 정보: {}", userId, updateUserInfoRequestDto);
    }

    public void updateUserNickname(@Valid UserNicknameRequestDto userNicknameRequestDto) {

        Optional<UserEntity> nickNameDuplicate = userRepository.findByNicknameAndDeletedAtIsNull(userNicknameRequestDto.getNickname());
        if (nickNameDuplicate.isPresent()){
            throw new UserNicknameDuplicateException("이미 사용중인 닉네임입니다.");
        }

        ObjectId userId = toObjectId(SecurityUtil.getCurrentUserId());
        log.info("[유저 정보 업데이트] 현재 사용자 ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[유저 정보 찾기 실패] 유저 정보를 찾을 수 없음. 사용자 ID: {}", userId);
                    return new NotFoundException("유저 정보를 찾을 수 없습니다.");
                });

        user.updateNickname(userNicknameRequestDto.getNickname());
        userRepository.save(user);
        log.info("[유저 정보 업데이트 성공] 사용자 ID: {}, 업데이트된 정보: {}", userId, userNicknameRequestDto);
    }

    public void updateUserProfile(MultipartFile profileImage) {
        ObjectId userId = toObjectId(SecurityUtil.getCurrentUserId());
        log.info("[프로필 이미지 업데이트] 현재 사용자 ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[유저 정보 찾기 실패] 유저 정보를 찾을 수 없음. 사용자 ID: {}", userId);
                    return new NotFoundException("유저 정보를 찾을 수 없습니다.");
                });

        String profileImageUrl = s3Service.handleImageUpload(List.of(profileImage)).get(0);
        user.updateProfileImageUrl(profileImageUrl);
        userRepository.save(user);
        log.info("[프로필 이미지 업데이트 성공] 사용자 ID: {}, 프로필 이미지 URL: {}", userId, profileImageUrl);
    }

    public void updateUserName(@Valid UserNameUpdateRequestDto request){
        ObjectId userId = toObjectId(SecurityUtil.getCurrentUserId());
        log.info("[유저 실명 수정] 현재 사용자 ID: {}, 요청 이름: {}", userId, request.getName());

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("[유저 실명 수정 실패] 유저 정보를 찾을 수 없음. 사용자 ID: {}", userId);
                    return new NotFoundException("유저 정보를 찾을 수 없습니다.");
                });

        String newName = request.getName().trim();

            if (newName.isEmpty()) {
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }
            if (newName.length() > 10) {
            throw new IllegalArgumentException("이름은 10자 이하여야 합니다.");
        }
            if (!newName.matches("^[가-힣a-zA-Z]+$")) {
            throw new IllegalArgumentException("이름은 한글 또는 영어만 입력 가능합니다.");
        }

            if (newName.equals(user.getName())) {
            log.info("[유저 실명 수정] 변경 사항 없음. 사용자 ID: {}", userId);
            return;
        }

            user.updateName(newName);
            userRepository.save(user);

            log.info("[유저 실명 수정 성공] 사용자 ID: {}, 변경 이름: {}", userId, newName);
    }
    /**
     * 유저 티켓팅 수령 정보 반환
     * @return UserTicketingParticipationInfoResponse 유저의 학번, 이름, 소속 Dto 반환
     */
    public UserTicketingParticipationInfoResponse getUserTicketingParticipationInfo() {
        return UserTicketingParticipationInfoResponse.of(
                userRepository.findByUserId(toObjectId(SecurityUtil.getCurrentUserId()))
                        .orElseThrow(() -> new NotFoundException("유저 정보를 찾을 수 없습니다.")));
    }

    /**
     * 유저 티켓팅 수령 정보 수정 (생성)
     * @return UserTicketingParticipationInfoResponse 유저의 학번, 이름, 소속 Dto 반환
     */
    public UserTicketingParticipationInfoResponse updateUserTicketingParticipationInfo(UserTicketingParticipationInfoUpdateRequest updateRequest) {
        UserEntity userEntity = userRepository.findByUserId(toObjectId(SecurityUtil.getCurrentUserId()))
                .orElseThrow(() -> new NotFoundException("유저 정보를 찾을 수 없습니다."));

        userEntity.updateParticipationInfo(updateRequest);
        userEntity = userRepository.save(userEntity);
        log.info("User Ticketing Participation Update: Email: {}, Name:{}", userEntity.getEmail(),userEntity.getName());
        return UserTicketingParticipationInfoResponse.of(userEntity);
    }

    /**
     * 유저 도메인 상호작용 조회 Enum Class
     */
    public enum InteractionType {
        LIKE, SCRAP, COMMENT
    }
}
