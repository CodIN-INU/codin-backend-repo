package inu.codin.codin.domain.user.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.email.entity.EmailAuthEntity;
import inu.codin.codin.domain.email.exception.EmailAuthFailException;
import inu.codin.codin.domain.email.repository.EmailAuthRepository;
import inu.codin.codin.domain.post.domain.comment.entity.CommentEntity;
import inu.codin.codin.domain.post.domain.comment.repository.CommentRepository;
import inu.codin.codin.domain.post.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.domain.like.repository.LikeRepository;
import inu.codin.codin.domain.post.domain.scrap.entity.ScrapEntity;
import inu.codin.codin.domain.post.domain.scrap.repository.ScrapRepository;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.service.PostService;
import inu.codin.codin.domain.user.dto.request.UserCreateRequestDto;
import inu.codin.codin.domain.user.dto.request.UserDeleteRequestDto;
import inu.codin.codin.domain.user.dto.request.UserPasswordRequestDto;
import inu.codin.codin.domain.user.dto.request.UserUpdateRequestDto;
import inu.codin.codin.domain.user.dto.response.UserInfoResponseDto;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.entity.UserStatus;
import inu.codin.codin.domain.user.exception.UserCreateFailException;
import inu.codin.codin.domain.user.exception.UserPasswordChangeFailException;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.endpoints.internal.Not;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final EmailAuthRepository emailAuthRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final ScrapRepository scrapRepository;
    private final CommentRepository commentRepository;

    private final PasswordEncoder passwordEncoder;
    private final PostService postService;
    private final S3Service s3Service;

    public void createUser(UserCreateRequestDto userCreateRequestDto, MultipartFile userImage) {

        log.info("[회원가입] 요청 데이터: {}", userCreateRequestDto);

        String imageUrl = null;
        if (userImage != null) {
            log.info("[회원가입] 프로필 이미지 업로드 중...");
            imageUrl = s3Service.handleImageUpload(List.of(userImage)).get(0);
            log.info("[회원가입] 프로필 이미지 업로드 완료: {}", imageUrl);
        }

        // imageUrl이 null이면 기본 이미지로 설정
        if (imageUrl == null) {
            imageUrl = s3Service.getDefaultProfileImageUrl(); // S3Service에서 기본 이미지 URL 가져오기

        }

        String encodedPassword = passwordEncoder.encode(userCreateRequestDto.getPassword());

        validateUserCreateRequest(userCreateRequestDto);
        log.info("[signUpUser] UserCreateRequestDto : {}", userCreateRequestDto);

        // todo : 중복 이메일, 닉네임 체크, 유저 상태, 유저 역할 변경 기능 추가
        UserEntity user = UserEntity.builder()
                .email(userCreateRequestDto.getEmail())
                .password(encodedPassword)
                .studentId(userCreateRequestDto.getStudentId())
                .name(userCreateRequestDto.getName())
                .nickname(userCreateRequestDto.getNickname())
                .profileImageUrl(imageUrl)
                .department(userCreateRequestDto.getDepartment())
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
        log.info("[signUpUser] SIGN UP SUCCESS!! : {}", user.getEmail());
    }

    // todo : 정책적으로 보안 위반 사항 확인 -> 에러 메세지를 통해서 유추 금지
    private void validateUserCreateRequest(UserCreateRequestDto userCreateRequestDto) {
        log.info("[회원가입 검증] 이메일 인증 상태 확인: {}", userCreateRequestDto.getEmail());
        EmailAuthEntity emailAuth = emailAuthRepository.findByEmail(userCreateRequestDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("[회원가입 검증 실패] 이메일 인증 기록 없음: {}", userCreateRequestDto.getEmail());
                    return new UserCreateFailException("이메일 인증을 먼저 진행해주세요.");
                });
        if (!emailAuth.isVerified()) {
            log.warn("[회원가입 검증 실패] 이메일 인증 미완료: {}", userCreateRequestDto.getEmail());
            throw new UserCreateFailException("이메일 인증을 먼저 진행해주세요.");
        }
        if (userRepository.findByEmail(userCreateRequestDto.getEmail()).isPresent()) {
            log.warn("[회원가입 검증 실패] 이미 존재하는 이메일: {}", userCreateRequestDto.getEmail());
            throw new UserCreateFailException("이미 존재하는 이메일입니다.");
        }
        if (userRepository.findByStudentId(userCreateRequestDto.getStudentId()).isPresent()) {
            log.warn("[회원가입 검증 실패] 이미 존재하는 학번: {}", userCreateRequestDto.getStudentId());
            throw new UserCreateFailException("이미 존재하는 학번입니다.");
        }
        log.info("[회원가입 검증] 검증 성공: {}", userCreateRequestDto.getEmail());
    }


    //해당 유저가 작성한 모든 글 반환 :: 게시글 내용 + 댓글+대댓글의 수 + 좋아요,스크랩 count 수 반환
    public PostPageResponse getAllUserPosts(int pageNumber) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        log.info("[게시글 조회] 유저 ID: {}, 페이지 번호: {}", userId, pageNumber);

        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page = postRepository.findAllByUserIdOrderByCreatedAt(userId, pageRequest);

        log.info("[게시글 조회 성공] 조회된 게시글 수: {}, 총 페이지 수: {}", page.getContent().size(), page.getTotalPages());
        return PostPageResponse.of(
                postService.getPostListResponseDtos(page.getContent()),
                page.getTotalPages() - 1,
                page.hasNext() ? page.getPageable().getPageNumber() + 1 : -1
        );
    }

    public PostPageResponse getPostUserInteraction(int pageNumber, InteractionType interactionType) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        log.info("[유저 상호작용 조회] 유저 ID: {}, 타입: {}, 페이지 번호: {}", userId, interactionType, pageNumber);

        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        switch (interactionType) {
            case LIKE:
                log.info("[좋아요 조회 시작] 유저 ID: {}, 타입: {}", userId, interactionType);
                Page<LikeEntity> likePage = likeRepository.findAllByUserIdAndLikeTypeAndDeletedAtIsNullOrderByCreatedAt(userId, LikeType.valueOf("POST"), pageRequest);
                List<PostEntity> postUserLike = likePage.getContent().stream()
                        .map(likeEntity -> postRepository.findByIdAndNotDeleted(likeEntity.getLikeTypeId())
                                .orElseThrow(() -> new NotFoundException("유저가 좋아요를 누른 게시글을 찾을 수 없습니다.")))
                        .toList();
                log.info("[좋아요 조회 완료] 총 페이지 수: {}, 다음 페이지 여부: {}", likePage.getTotalPages(), likePage.hasNext());
                return PostPageResponse.of(postService.getPostListResponseDtos(postUserLike), likePage.getTotalPages()-1, likePage.hasNext()? likePage.getPageable().getPageNumber() + 1 : -1);

            case SCRAP:
                log.info("[스크랩 조회 시작] 유저 ID: {}, 타입: {}", userId, interactionType);
                Page<ScrapEntity> scrapPage = scrapRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAt(userId, pageRequest);
                List<PostEntity> postUserScrap = scrapPage.getContent().stream()
                        .map(scrapEntity -> postRepository.findByIdAndNotDeleted(scrapEntity.getPostId())
                                .orElseThrow(() -> new NotFoundException("유저가 스크랩한 게시글을 찾을 수 없습니다.")))
                        .toList();
                log.info("[스크랩 조회 완료] 총 페이지 수: {}, 다음 페이지 여부: {}", scrapPage.getTotalPages(), scrapPage.hasNext());
                return PostPageResponse.of(postService.getPostListResponseDtos(postUserScrap), scrapPage.getTotalPages()-1, scrapPage.hasNext()? scrapPage.getPageable().getPageNumber() + 1 : -1);

            case COMMENT:
                log.info("[댓글 조회 시작] 유저 ID: {}, 타입: {}", userId, interactionType);
                Page<CommentEntity> commentPage = commentRepository.findAllByUserIdOrderByCreatedAt(userId, pageRequest);
                List<PostEntity> postUserComment = commentPage.getContent().stream()
                        .map(commentEntity -> postRepository.findByIdAndNotDeleted(commentEntity.getPostId())
                                .orElseThrow(() -> new NotFoundException("유저가 작성한 댓글의 게시글을 찾을 수 없습니다.")))
                        // 중복 필터링 로직
                        .collect(Collectors.toMap(
                                PostEntity::get_id, // Key: postId
                                postEntity -> postEntity, // Value: PostEntity
                                (existing, replacement) -> existing // 중복 발생 시 기존 값 유지
                        ))
                        // 중복 제거된 후 Map에서 PostEntity 추출
                        .values()
                        .stream()
                        .toList();
                log.info("[댓글 조회 완료] 총 페이지 수: {}, 다음 페이지 여부: {}", commentPage.getTotalPages(), commentPage.hasNext());
                return PostPageResponse.of(postService.getPostListResponseDtos(postUserComment), commentPage.getTotalPages()-1, commentPage.hasNext()? commentPage.getPageable().getPageNumber() + 1 : -1);

            default:
                log.warn("[유효하지 않은 상호작용 타입] 유저 ID: {}, 상호작용 타입: {}", userId, interactionType);
                throw new IllegalArgumentException("지원하지 않는 타입입니다.");
        }
    }

    public void setUserPassword(@Valid UserPasswordRequestDto userPasswordRequestDto, String code) {
        if (code==null){
            throw new UserPasswordChangeFailException("코드가 비어있거나 유효하지 않습니다.");
        }
        log.info("[비밀번호 변경] 인증번호 확인 시작: {}", code);

        UserEntity user = checkPasswordAuthNum(code);
        if (user.isChangePassword()) {
            log.info("[비밀번호 변경] 비밀번호 변경 가능 확인 완료. 비밀번호 변경 진행 중...");

            String encodedPassword = passwordEncoder.encode(userPasswordRequestDto.getPassword());
            user.updatePassword(encodedPassword);
            user.canChangePassword();
            userRepository.save(user);

            log.info("[비밀번호 변경 성공] 이메일: {}", user.getEmail());
        } else {
            log.warn("[비밀번호 변경 실패] 비밀번호 변경 불가능. 이메일 인증 필요. 이메일: {}", user.getEmail());
            throw new UserPasswordChangeFailException("유저의 비밀번호를 변경할 수 없습니다. 이메일 인증을 먼저 진행해주세요.");
        }
    }

    public UserEntity checkPasswordAuthNum(String authNum) {
        log.info("[비밀번호 인증번호 확인] 인증번호: {}", authNum);

        EmailAuthEntity emailAuthEntity = checkEmailAndAuthNum(authNum);
        log.info("[checkAuthNumForPW] email : {}, authNum : {}", emailAuthEntity.getEmail(), authNum);

        UserEntity user = userRepository.findByEmail(emailAuthEntity.getEmail())
                .orElseThrow(() -> {
                    log.warn("[비밀번호 인증번호 확인 실패] 유저 정보를 찾을 수 없음. 이메일: {}", emailAuthEntity.getEmail());
                    return new NotFoundException("유저 정보를 찾을 수 없습니다.");
                });
        user.canChangePassword();
        userRepository.save(user);
        log.info("[비밀번호 변경 가능 상태 업데이트] 이메일: {}", user.getEmail());
        return user;
    }


    private EmailAuthEntity checkEmailAndAuthNum(String authNum) {
        log.info("[인증번호 확인] 인증번호: {}", authNum);

        EmailAuthEntity emailAuthEntity = emailAuthRepository.findByAuthNum(authNum)
                .orElseThrow(() -> {
                    log.warn("[인증번호 확인 실패] 인증번호가 일치하지 않음: {}", authNum);
                    return new EmailAuthFailException("인증번호가 일치하지 않습니다.", authNum);
                });

        // 인증번호 만료 확인
        if (emailAuthEntity.isExpired()) {
            log.warn("[인증번호 만료] 인증번호 만료됨. 이메일: {}", emailAuthEntity.getEmail());
            throw new EmailAuthFailException("인증번호가 만료되었습니다.", emailAuthEntity.getEmail());
        }

        emailAuthEntity.verifyEmail();
        emailAuthRepository.save(emailAuthEntity);
        log.info("[인증번호 확인 성공] 인증 완료. 이메일: {}", emailAuthEntity.getEmail());
        return emailAuthEntity;
    }

    public void deleteUser(UserDeleteRequestDto userDeleteRequestDto) {
        log.info("[회원 탈퇴 요청] 이메일: {}", userDeleteRequestDto.getEmail());

        UserEntity user = userRepository.findByEmail(userDeleteRequestDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("[회원 탈퇴 실패] 유저 정보 없음: {}", userDeleteRequestDto.getEmail());
                    return new NotFoundException("해당 이메일에 대한 유저 정보를 찾을 수 없습니다.");
                });

        SecurityUtils.validateUser(user.get_id());
        user.delete();
        userRepository.save(user);

        log.info("[회원 탈퇴 성공] 이메일: {}", userDeleteRequestDto.getEmail());
    }

    public UserInfoResponseDto getUserInfo() {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        log.info("[유저 정보 조회] 유저 ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[유저 정보 조회 실패] 유저 정보 없음: {}", userId);
                    return new NotFoundException("유저 정보를 찾을 수 없습니다.");
                });

        log.info("[유저 정보 조회 성공] 닉네임: {}", user.getNickname());
        return UserInfoResponseDto.of(user);
    }
    public void updateUserInfo(@Valid UserUpdateRequestDto userUpdateRequestDto) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        log.info("[유저 정보 업데이트] 현재 사용자 ID: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[유저 정보 찾기 실패] 유저 정보를 찾을 수 없음. 사용자 ID: {}", userId);
                    return new NotFoundException("유저 정보를 찾을 수 없습니다.");
                });

        user.updateUserInfo(userUpdateRequestDto);
        userRepository.save(user);
        log.info("[유저 정보 업데이트 성공] 사용자 ID: {}, 업데이트된 정보: {}", userId, userUpdateRequestDto);
    }

    public void updateUserProfile(MultipartFile profileImage) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
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

    public enum InteractionType {
        LIKE, SCRAP, COMMENT
    }
}
