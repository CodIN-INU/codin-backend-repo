package inu.codin.codin.domain.user.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.email.entity.EmailAuthEntity;
import inu.codin.codin.domain.email.repository.EmailAuthRepository;
import inu.codin.codin.domain.post.domain.like.entity.LikeEntity;
import inu.codin.codin.domain.post.domain.like.entity.LikeType;
import inu.codin.codin.domain.post.domain.like.repository.LikeRepository;
import inu.codin.codin.domain.post.domain.scrap.entity.ScrapEntity;
import inu.codin.codin.domain.post.domain.scrap.repository.ScrapRepository;
import inu.codin.codin.domain.post.dto.response.PostPageResponse;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.service.PostService;
import inu.codin.codin.domain.user.dto.UserCreateRequestDto;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.entity.UserStatus;
import inu.codin.codin.domain.user.exception.UserCreateFailException;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final PasswordEncoder passwordEncoder;
    private final PostService postService;

    public void createUser(UserCreateRequestDto userCreateRequestDto) {

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
                .profileImageUrl(userCreateRequestDto.getProfileImageUrl())
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

    public PostPageResponse getPostUserLike(int pageNumber) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<LikeEntity> page = likeRepository.findAllByUserIdAndLikeTypeOrderByCreatedAt(userId, LikeType.valueOf("POST"), pageRequest);
        List<PostEntity> postUserLike = page.getContent().stream()
                .map(likeEntity -> postRepository.findByIdAndNotDeleted(likeEntity.getLikeTypeId())
                        .orElseThrow(() -> new NotFoundException("유저가 좋아요를 누른 게시글을 찾을 수 없습니다.")))
                .toList();
        return PostPageResponse.of(postService.getPostListResponseDtos(postUserLike), page.getTotalPages()-1, page.hasNext()? page.getPageable().getPageNumber() + 1 : -1);
    }

    public PostPageResponse getPostUserScrap(int pageNumber) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(pageNumber, 20, Sort.by("createdAt").descending());
        Page<ScrapEntity> page = scrapRepository.findAllByUserIdOrderByCreatedAt(userId, pageRequest);
        List<PostEntity> postUserScrap = page.getContent().stream()
                .map(scrapEntity -> postRepository.findByIdAndNotDeleted(scrapEntity.getPostId())
                        .orElseThrow(() -> new NotFoundException("유저가 스크랩한 게시글을 찾을 수 없습니다.")))
                .toList();
        return PostPageResponse.of(postService.getPostListResponseDtos(postUserScrap), page.getTotalPages()-1, page.hasNext()? page.getPageable().getPageNumber() + 1 : -1);
    }
}
