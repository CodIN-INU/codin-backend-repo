package inu.codin.codin.domain.user.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.email.entity.EmailAuthEntity;
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

import java.util.List;

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

        String imageUrl = null;
        if (userImage != null) imageUrl = s3Service.handleImageUpload(List.of(userImage)).get(0);

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
        EmailAuthEntity emailAuth = emailAuthRepository.findByEmail(userCreateRequestDto.getEmail()).orElseThrow(() ->
                new UserCreateFailException("이메일 인증을 먼저 진행해주세요."));
        if (!emailAuth.isVerified())
            throw new UserCreateFailException("이메일 인증을 먼저 진행해주세요.");
        if (userRepository.findByEmail(userCreateRequestDto.getEmail()).isPresent())
            throw new UserCreateFailException("이미 존재하는 이메일입니다.");
        if (userRepository.findByStudentId(userCreateRequestDto.getStudentId()).isPresent())
            throw new UserCreateFailException("이미 존재하는 학번입니다.");
    }

    //해당 유저가 작성한 모든 글 반환 :: 게시글 내용 + 댓글+대댓글의 수 + 좋아요,스크랩 count 수 반환
    public PostPageResponse getAllUserPosts(int pageNumber) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<PostEntity> page = postRepository.findAllByUserIdOrderByCreatedAt(userId, pageRequest);
        return PostPageResponse.of(postService.getPostListResponseDtos(page.getContent()), page.getTotalPages()-1, page.hasNext()? page.getPageable().getPageNumber() + 1 : -1);
    }

    public PostPageResponse getPostUserInteraction(int pageNumber, InteractionType interactionType) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        switch (interactionType) {
            case LIKE:
                Page<LikeEntity> likePage = likeRepository.findAllByUserIdAndLikeTypeOrderByCreatedAt(userId, LikeType.valueOf("POST"), pageRequest);
                List<PostEntity> postUserLike = likePage.getContent().stream()
                        .map(likeEntity -> postRepository.findByIdAndNotDeleted(likeEntity.getLikeTypeId())
                                .orElseThrow(() -> new NotFoundException("유저가 좋아요를 누른 게시글을 찾을 수 없습니다.")))
                        .toList();
                return PostPageResponse.of(postService.getPostListResponseDtos(postUserLike), likePage.getTotalPages()-1, likePage.hasNext()? likePage.getPageable().getPageNumber() + 1 : -1);

            case SCRAP:
                Page<ScrapEntity> scrapPage = scrapRepository.findAllByUserIdOrderByCreatedAt(userId, pageRequest);
                List<PostEntity> postUserScrap = scrapPage.getContent().stream()
                        .map(scrapEntity -> postRepository.findByIdAndNotDeleted(scrapEntity.getPostId())
                                .orElseThrow(() -> new NotFoundException("유저가 스크랩한 게시글을 찾을 수 없습니다.")))
                        .toList();
                return PostPageResponse.of(postService.getPostListResponseDtos(postUserScrap), scrapPage.getTotalPages()-1, scrapPage.hasNext()? scrapPage.getPageable().getPageNumber() + 1 : -1);

            case COMMENT:
                Page<CommentEntity> commentPage = commentRepository.findAllByUserIdOrderByCreatedAt(userId, pageRequest);
                List<PostEntity> postUserComment = commentPage.getContent().stream()
                        .map(commentEntity -> postRepository.findByIdAndNotDeleted(commentEntity.getPostId())
                                .orElseThrow(() -> new NotFoundException("유저가 작성한 댓글의 게시글을 찾을 수 없습니다.")))
                        .toList();
                return PostPageResponse.of(postService.getPostListResponseDtos(postUserComment), commentPage.getTotalPages()-1, commentPage.hasNext()? commentPage.getPageable().getPageNumber() + 1 : -1);

                default:
                throw new IllegalArgumentException("지원하지 않는 타입입니다.");
        }
    }

    public void setUserPassword(@Valid UserPasswordRequestDto userPasswordRequestDto) {
        UserEntity user = userRepository.findByEmail(userPasswordRequestDto.getEmail())
                .orElseThrow(() -> new NotFoundException("해당 이메일에 대한 유저 정보를 찾을 수 없습니다."));
        if (user.isChangePassword()){
            String encodedPassword = passwordEncoder.encode(userPasswordRequestDto.getPassword());
            user.updatePassword(encodedPassword);
            user.changePassword();
            userRepository.save(user);
        } else {
            throw new UserPasswordChangeFailException("유저의 비밀번호를 변경할 수 없습니다. 이메일 인증을 먼저 진행해주세요.");
        }
    }

    public void deleteUser(UserDeleteRequestDto userDeleteRequestDto) {
        UserEntity user = userRepository.findByEmail(userDeleteRequestDto.getEmail())
                .orElseThrow(() -> new NotFoundException("해당 이메일에 대한 유저 정보를 찾을 수 없습니다."));
        SecurityUtils.validateUser(user.get_id());
        user.delete();
        userRepository.save(user);
    }

    public UserInfoResponseDto getUserInfo() {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저 정보를 찾을 수 없습니다."));
        return UserInfoResponseDto.of(user);
    }

    public enum InteractionType {
        LIKE, SCRAP, COMMENT
    }



}
