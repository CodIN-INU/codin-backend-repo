package inu.codin.codin.common.security.service.oauth2;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.enums.AuthResultStatus;
import inu.codin.codin.common.security.service.JwtService;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.entity.UserStatus;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.codin.infra.s3.S3Service;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class GoogleAuthService extends AbstractAuthService implements Oauth2AuthService {


    public GoogleAuthService(UserRepository userRepository, S3Service s3Service, JwtService jwtService, UserDetailsService userDetailsService) {
        super(userRepository, s3Service, jwtService, userDetailsService);
    }

    @Override
    public AuthResultStatus oauthLogin(OAuth2User oAuth2User, HttpServletResponse response) {
        // Google에서는 email, family_name, given_name 등 제공됨.
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("family_name");
        if (name == null || name.isEmpty()) {
            name = (String) attributes.get("name");
        }
        String department = (String) attributes.get("given_name");
        if (department == null) {
            department = "";
        }

        log.info("OAuth2 login: email={}, name={}, department={}",
                email, name, department);

        InfoFromOAuth2User info = new InfoFromOAuth2User(email, name, department);

        // Google은 email이 식별자
        Optional<UserEntity> optionalUser = userRepository.findByEmailAndStatusAll(info.email());
        if (optionalUser.isPresent()) {
            UserEntity existingUser = optionalUser.get();
            log.info("기존 Google 회원 로그인: {}", info.email());
            switch(existingUser.getStatus()){
                case ACTIVE -> {
                    issueJwtToken(info.email(), response);
                    log.info("정상 로그인 완료: {}", info.email());
                    return AuthResultStatus.LOGIN_SUCCESS;
                }
                case DISABLED -> {
                    log.info("회원 프로필 설정 미완료 (userStatus={}): {}", existingUser.getStatus(), info.email());
                    return AuthResultStatus.PROFILE_INCOMPLETE;
                }
                case SUSPENDED -> {
                    log.info("정지된 유저: {}", info.email());
                    return AuthResultStatus.SUSPENDED_USER;
                }
                default -> {
                    log.error("알 수 없는 유저 상태: {}", existingUser.getStatus());
                    throw new NotFoundException("유저의 상태를 알 수 없습니다. _id: " + existingUser.get_id());
                }
            }
        } else {
            log.info("신규 Google 회원 등록: {}", info.email());
            String deptDesc = (info.department() != null) ? info.department().replace("/", "").trim() : "";
            Department dept = Department.fromDescription(deptDesc);
            UserEntity newUser = UserEntity.builder()
                    .email(info.email())
                    .name(info.name())
                    .department(dept)
                    .profileImageUrl(s3Service.getDefaultProfileImageUrl())
                    .status(UserStatus.DISABLED)
                    .role(UserRole.USER)
                    .build();
            userRepository.save(newUser);
            log.info("신규 회원 등록 완료 (프로필 미완료): {}", newUser);
            return AuthResultStatus.NEW_USER_REGISTERED;
        }
    }

    private record InfoFromOAuth2User(String email, String name, String department) { }
}
