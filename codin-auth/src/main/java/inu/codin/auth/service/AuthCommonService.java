package inu.codin.auth.service;

import inu.codin.auth.jwt.JwtTokenIssuer;
import inu.codin.common.exception.NotFoundException;
import inu.codin.auth.dto.SignUpAndLoginRequestDto;
import inu.codin.codin.domain.user.dto.request.UserProfileRequestDto;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.exception.UserCreateFailException;
import inu.codin.codin.domain.user.exception.UserNicknameDuplicateException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import inu.codin.auth.service.oauth2.AbstractAuthService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AuthCommonService extends AbstractAuthService {

    private final PasswordEncoder passwordEncoder;

    public AuthCommonService(JwtTokenIssuer jwtTokenIssuer, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        super(jwtTokenIssuer, userDetailsService);
        this.passwordEncoder = passwordEncoder;
    }

    public void completeUserProfile(UserProfileRequestDto userProfileRequestDto, MultipartFile userImage, HttpServletResponse response) {
        // User 에게 책임 분리 (auth -> user API 호출)
        String email = userCompleteUserProfile(userProfileRequestDto, userImage);
        issueJwtToken(email, response);
    }

    public LocalDateTime getSuspensionEndDate(OAuth2User oAuth2User){
        String email = (String) oAuth2User.getAttribute("email");
        return userGetSuspensionEndDate(email);

        //
        Optional<UserEntity> optionalUser = userRepository.findByEmailAndStatusAll(email);
        if (optionalUser.isPresent()){
            UserEntity user = optionalUser.get();
            return user.getTotalSuspensionEndDate();
        } else {
            throw new NotFoundException("유저를 찾을 수 없습니다.");
        }
    }

    public void login(SignUpAndLoginRequestDto signUpAndLoginRequestDto, HttpServletResponse response) {
        UserAdminLogin userAdminLogin = userGetLoginMaterial(signUpAndLoginRequestDto.getEmail());

        // auth 책임 : 비밀번호 검증
        if (passwordEncoder.matches(signUpAndLoginRequestDto.getPassword(), user.getPassword())) {
            issueJwtToken(userAdminLogin.getEmail(), response);
        } else {
            throw new UserCreateFailException("아이디 혹은 비밀번호를 잘못 입력하였습니다.");
        }
    }



    /**
     * USER 책임: 프로필 완성 전체 로직(현재는 로컬 호출)
     * 추후: user-service에 "프로필 완성" API 호출로 바꿀 자리
     */
    private String userCompleteUserProfile(UserProfileRequestDto dto, MultipartFile userImage) {

        // 닉네임 중복 체크
        Optional<UserEntity> nickNameDuplicate =
                userRepository.findByNicknameAndDeletedAtIsNull(dto.getNickname());
        if (nickNameDuplicate.isPresent()) {
            throw new UserNicknameDuplicateException("이미 사용중인 닉네임입니다.");
        }

        // 비활성 사용자 조회
        UserEntity user = userRepository.findByEmailAndDisabled(dto.getEmail())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        log.info("[completeUserProfile] 사용자 조회 성공: {}", dto.getEmail());

        // 프로필 이미지 처리(S3 포함) - 지금은 User 책임으로 같이 묶어둠 - 추후 재판단 필요할듯
        String imageUrl = null;
        if (userImage != null && !userImage.isEmpty()) {
            log.info("[프로필 설정] 프로필 이미지 업로드 중...");
            imageUrl = s3Service.handleImageUpload(List.of(userImage)).get(0);
            log.info("[프로필 설정] 프로필 이미지 업로드 완료: {}", imageUrl);
        }
        if (imageUrl == null) {
            imageUrl = s3Service.getDefaultProfileImageUrl();
        }

        // 업데이트 + 활성화 + 저장
        user.updateNickname(dto.getNickname());
        user.updateProfileImageUrl(imageUrl);
        user.activation();
        userRepository.save(user);

        log.info("[completeUserProfile] 프로필 설정 완료: {}", user.getEmail());

        return user.getEmail();
    }

    /**
     * USER 책임: 정지 종료일 조회 전체 로직(현재는 로컬 호출)
     * 추후: user-service에 "정지 종료일 조회" API 호출로 바꿀 자리
     */
    private LocalDateTime userGetSuspensionEndDate(String email) {
        UserEntity user = userRepository.findByEmailAndStatusAll(email)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        return user.getTotalSuspensionEndDate();
    }

    /**
     * USER 책임: 로그인에 필요한 최소 재료 조회/검증(현재는 로컬 호출)
     * 추후: user-service에 "로그인용 사용자 조회" API 호출로 바꿀 자리
     *
     * AUTH가 passwordEncoder.matches() 할 수 있게 "encodedPassword"만 넘김.
     */
    private AdminLoginMaterial userGetLoginMaterial(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserCreateFailException("아이디 혹은 비밀번호를 잘못 입력하였습니다."));

        if (user.getPassword() == null) {
            log.info("[login] 비밀번호가 존재하지 않는 유저 접근, email={}", user.getEmail());
            throw new UserCreateFailException("아이디 혹은 비밀번호를 잘못 입력하였습니다.");
        }

        return new AdminLoginMaterial(user.getEmail(), user.getPassword());
    }

    /**
     * Auth <- User 로 받을 "로그인 재료" (email, encodedPassword)
     * 추후엔 user-service 응답 DTO가 될 것.
     */
    private record AdminLoginMaterial(String email, String encodedPassword) {}
}
