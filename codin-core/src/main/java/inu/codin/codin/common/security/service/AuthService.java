package inu.codin.codin.common.security.service;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.dto.SignUpAndLoginRequestDto;
import inu.codin.codin.common.security.enums.AuthResultStatus;
import inu.codin.codin.domain.user.dto.request.UserProfileRequestDto;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.entity.UserStatus;
import inu.codin.codin.domain.user.exception.UserCreateFailException;
import inu.codin.codin.domain.user.exception.UserNicknameDuplicateException;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Google OAuth2를 통해 사용자 정보를 등록하고 로그인 처리하는 메소드
     * - 기존 회원이면, userStatus가 ACTIVE인 경우에만 JWT 토큰을 발급하여 정식 로그인 처리
     * - 첫 로그인 시: 신규 회원이면 DB에 등록할 때 userStatus를 DISABLED로 설정 (프로필 미완료)
     *   userStatus가 DISABLED이면 프로필 설정이 필요하다는 상태로 처리
     * - userStatus가 SUSPEND이면 정지된 회원
     */
    public AuthResultStatus oauthLogin(OAuth2User oAuth2User, HttpServletResponse response) {

        InfoFromOAuth2User info = getInfoFromOAuth2User(oAuth2User);

        // 회원 존재 여부 판단: email 기준
        Optional<UserEntity> optionalUser = userRepository.findByEmailAndStatusAll(info.email());
        if (optionalUser.isPresent()) {
            UserEntity existingUser = optionalUser.get();
            log.info("기존 회원 로그인: {}", info.email());

            switch(existingUser.getStatus()){
                case ACTIVE -> {
                    // 프로필 설정 완료된 회원: 정상 로그인 처리
                    issueJwtToken(info.email(), response);
                    log.info("정상 로그인 완료: {}", info.email());
                    return AuthResultStatus.LOGIN_SUCCESS;
                }
                case DISABLED -> {
                    // 프로필 설정 미완료: JWT 토큰 미발급, 프론트엔드에서 프로필 설정 페이지로 유도
                    log.info("회원 프로필 설정 미완료 (userStatus={}) : {}", existingUser.getStatus(), info.email());
                    return AuthResultStatus.PROFILE_INCOMPLETE;
                } case SUSPENDED -> {
                    //정지된 유저
                    log.info("정지된 유저 : email - {} ", info.email());
                    return AuthResultStatus.SUSPENDED_USER;
                } default -> {
                    log.error("유저의 상태가 ACTIVE, DISABLED, SUSPENDED 외의 값을 가지고 있습니다. UserStatus : {}", existingUser.getStatus() );
                    throw new NotFoundException("유저의 상태(Status)를 알 수 없습니다. _id : "+ existingUser.get_id().toString() + ", status : " + existingUser.getStatus());
                }
            }

        } else {
            log.info("신규 회원 등록: {}", info.email());
            String deptDesc = (info.department() != null) ? info.department().replace("/", "").trim() : "";
            Department dept = Department.fromDescription(deptDesc);

            // 신규 회원 등록 시, userStatus를 DISABLED(비활성)으로 설정하여 프로필 설정 미완료 상태로 처리
            UserEntity newUser = UserEntity.builder()
                    .email(info.email())
                    .name(info.name())
                    .department(dept)
                    .profileImageUrl(s3Service.getDefaultProfileImageUrl()) // 기본 프로필 이미지 사용
                    .status(UserStatus.DISABLED)
                    .role(UserRole.USER)
                    .build();
            userRepository.save(newUser);
            log.info("신규 회원 등록 완료 (프로필 미완료): {}", newUser);
            return AuthResultStatus.NEW_USER_REGISTERED;
            // 신규 회원은 프로필 설정 완료 전까지는 JWT 토큰을 발급 안 함.
        }
    }

    private static InfoFromOAuth2User getInfoFromOAuth2User(OAuth2User oAuth2User) {
        // OAuth2 제공자로부터 받은 모든 속성을 로그 출력 (디버깅용)
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 기본 속성 추출
        String email = (String) attributes.get("email");
        String sub = (String) attributes.get("sub");  // 고유 식별자 (필요시 저장)
        String name = (String) attributes.get("family_name");  // 예: "김기수"
        String department = (String) attributes.get("given_name"); // 예: "/컴퓨터공학부"

        log.info("OAuth2 login: email={}, sub={}, name={}, department={}",
                email, sub, name, department);
        InfoFromOAuth2User info = new InfoFromOAuth2User(email, name, department);
        return info;
    }

    private record InfoFromOAuth2User(String email, String name, String department) {
    }

    /**
     * 최초 로그인 후, 사용자에게 닉네임과 프로필 이미지를 설정받아 DB를 업데이트하는 메소드.
     * 프로필 설정 완료 후 userStatus를 ACTIVE로 업데이트하여 정식 회원가입을 완료.
     */
    public void completeUserProfile(UserProfileRequestDto userProfileRequestDto, MultipartFile userImage, HttpServletResponse response) {
        // 중복 닉네임 검사
        Optional<UserEntity> nickNameDuplicate = userRepository.findByNicknameAndDeletedAtIsNull(userProfileRequestDto.getNickname());
        if (nickNameDuplicate.isPresent()){
            throw new UserNicknameDuplicateException("이미 사용중인 닉네임입니다.");
        }

        // DB에서 해당 사용자를 이메일로 조회
        Optional<UserEntity> userOpt = userRepository.findByEmailAndDisabled(userProfileRequestDto.getEmail());
        if (userOpt.isEmpty()){
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        UserEntity user = userOpt.get();
        log.info("[completeUserProfile] 사용자 조회 성공: {}", userProfileRequestDto.getEmail());

        // 업로드된 이미지가 없으면 기본 프로필 이미지 URL 사용
        String imageUrl = null;
        if (userImage == null && userImage.isEmpty()) {
            imageUrl = s3Service.getDefaultProfileImageUrl();
        } else {
            imageUrl = s3Service.handleImageUpload(List.of(userImage)).get(0);
            log.info("[프로필 설정] 프로필 이미지 업로드 완료: {}", imageUrl);
        }
        
        // 사용자 정보 업데이트: 닉네임, 프로필 이미지 URL 업데이트 및 userStatus를 ACTIVE(활성)으로 변경
        user.updateNickname(userProfileRequestDto.getNickname());
        user.updateProfileImageUrl(imageUrl);
        user.activation();
        userRepository.save(user);

        log.info("[completeUserProfile] 프로필 설정 완료: {}", user.getEmail());
        // 프로필 설정 완료 후 정식 회원으로 간주하여 JWT 토큰 재발급
        issueJwtToken(user.getEmail(), response);
    }


    public void login(SignUpAndLoginRequestDto signUpAndLoginRequestDto, HttpServletResponse response) {
        Optional<UserEntity> user = userRepository.findByEmail(signUpAndLoginRequestDto.getEmail());
        if (user.isPresent()) {
            issueJwtToken(signUpAndLoginRequestDto.getEmail(), response);
        } else throw new UserCreateFailException("아이디 혹은 비밀번호를 잘못 입력하였습니다.");
    }

    public void issueJwtToken(String email, HttpServletResponse response) {
        jwtService.deleteToken(response);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        jwtService.createToken(response);
    }
    
    public LocalDateTime getSuspensionEndDate(OAuth2User oAuth2User){
        InfoFromOAuth2User info = getInfoFromOAuth2User(oAuth2User);

        Optional<UserEntity> optionalUser = userRepository.findByEmailAndStatusAll(info.email());
        if (optionalUser.isPresent()){
            UserEntity user = optionalUser.get();
            return user.getTotalSuspensionEndDate();
        } else throw new NotFoundException("유저를 찾을 수 없습니다. _id: " + optionalUser.get().get_id().toString());
    }
}
