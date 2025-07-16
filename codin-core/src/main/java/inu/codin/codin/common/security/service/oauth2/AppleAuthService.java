package inu.codin.codin.common.security.service.oauth2;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.enums.AuthResultStatus;
import inu.codin.codin.common.security.service.AbstractAuthService;
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
public class AppleAuthService extends AbstractAuthService implements Oauth2AuthService {

    public AppleAuthService(UserRepository userRepository, S3Service s3Service, JwtService jwtService, UserDetailsService userDetailsService) {
        super(userRepository, s3Service, jwtService, userDetailsService);
    }

    @Override
    public AuthResultStatus oauthLogin(OAuth2User oAuth2User, HttpServletResponse response) {
        // Apple에서는 email이 없을 수 있으므로, email이 없으면 고유 식별자(sub)를 사용.
        log.info("AppleAuthService oauthLogin");
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String sub = (String) attributes.get("sub");
        String name = (String) attributes.get("name");
        if (name == null || name.isEmpty()) {
            name = (String) attributes.get("family_name");
        }
        // Apple은 부서 정보 제공하지 않음.
        String department = "";
        InfoFromOAuth2User info = new InfoFromOAuth2User(email, sub, name, department);

        // 식별자: email이 존재하면 email, 아니면 sub 사용
        String identifier = (info.email() != null && !info.email().isEmpty()) ? info.email() : info.sub();

        Optional<UserEntity> optionalUser = userRepository.findByEmailAndStatusAll(identifier);
        if (optionalUser.isPresent()) {
            UserEntity existingUser = optionalUser.get();
            log.info("기존 Apple 회원 로그인: {}", identifier);
            switch(existingUser.getStatus()){
                case ACTIVE -> {
                    issueJwtToken(identifier, response);
                    return AuthResultStatus.LOGIN_SUCCESS;
                }
                case DISABLED -> {
                    return AuthResultStatus.PROFILE_INCOMPLETE;
                }
                case SUSPENDED -> {
                    return AuthResultStatus.SUSPENDED_USER;
                }
                default -> {
                    throw new NotFoundException("유저의 상태를 알 수 없습니다. _id: " + existingUser.get_id());
                }
            }
        } else {
            log.info("신규 Apple 회원 등록: {}", identifier);
            UserEntity newUser = UserEntity.builder()
                    .email(identifier)
                    .name(info.name() != null ? info.name() : identifier)
                    .department(Department.OTHERS)  // Apple은 부서 정보 없음,,
                    .profileImageUrl(s3Service.getDefaultProfileImageUrl())
                    .status(UserStatus.DISABLED)
                    .role(UserRole.USER)
                    .build();
            userRepository.save(newUser);
            return AuthResultStatus.NEW_USER_REGISTERED;
        }
    }

    private record InfoFromOAuth2User(String email, String sub, String name, String department) { }
}
