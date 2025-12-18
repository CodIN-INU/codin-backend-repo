package inu.codin.auth.service.oauth2;

import inu.codin.auth.jwt.JwtTokenIssuer;
import inu.codin.common.dto.Department;
import inu.codin.common.exception.NotFoundException;
import inu.codin.auth.enums.AuthResultStatus;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.entity.UserStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class AppleAuthService extends AbstractAuthService implements Oauth2AuthService {

    public AppleAuthService(JwtTokenIssuer jwtTokenIssuer, UserDetailsService userDetailsService) {
        super(jwtTokenIssuer, userDetailsService);
    }

    @Override
    public AuthResultStatus oauthLogin(OAuth2User oAuth2User, HttpServletResponse response) {
        // AUTH: OAuth2User -> 내부 표준 형태로 정규화
        InfoFromOAuth2User info = authParseAppleOAuthUser(oAuth2User);

        // AUTH: 식별자 결정 (email 우선, 없으면 sub)
        String identifier = authResolveIdentifier(info);

        // USER: 기존/신규 + 상태 판단을 한 번에 받기
        UserAuthDecision decision = userDecideAppleOauth(identifier, info);

        // AUTH: 토큰 발급
        if (decision.shouldIssueToken()) {
            issueJwtToken(decision.tokenSubject(), response);
        }

        // AUTH: 최종 응답 상태 반환
        return decision.resultStatus();
    }


    // AUTH 책임: OAuth2 입력 파싱/정규화, 식별자 결정
    // Apple에서는 email이 없을 수 있으므로, email이 없으면 고유 식별자(sub)를 사용.
    private InfoFromOAuth2User authParseAppleOAuthUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String sub = (String) attributes.get("sub");
        String name = (String) attributes.get("name");

        if (name == null || name.isEmpty()) {
            name = (String) attributes.get("family_name");
        }

        // Apple은 부서 정보 제공하지 않음.
        String department = "";

        return new InfoFromOAuth2User(email, sub, name, department);
    }

    private String authResolveIdentifier(InfoFromOAuth2User info) {
        return (info.email() != null && !info.email().isEmpty()) ? info.email() : info.sub();
    }

    //

    //USER 책임(통채로 분리): 조회/상태판단/신규저장
    private UserAuthDecision userDecideAppleOauth(String identifier, InfoFromOAuth2User info) {
        return userRepository.findByEmailAndStatusAll(identifier)
                .map(existing -> {
                    log.info("기존 Apple 회원 로그인: {}", identifier);

                    return switch (existing.getStatus()) {
                        case ACTIVE -> UserAuthDecision.issueToken(AuthResultStatus.LOGIN_SUCCESS, identifier);
                        case DISABLED -> UserAuthDecision.noToken(AuthResultStatus.PROFILE_INCOMPLETE);
                        case SUSPENDED -> UserAuthDecision.noToken(AuthResultStatus.SUSPENDED_USER);
                        default -> throw new NotFoundException("유저의 상태를 알 수 없습니다. _id: " + existing.get_id());
                    };
                })
                .orElseGet(() -> {
                    log.info("신규 Apple 회원 등록: {}", identifier);

                    UserEntity newUser = UserEntity.builder()
                            .email(identifier)
                            .name(info.name() != null ? info.name() : identifier)
                            .department(Department.OTHERS)
                            .profileImageUrl(s3Service.getDefaultProfileImageUrl())
                            .status(UserStatus.DISABLED)
                            .role(UserRole.USER)
                            .build();

                    userRepository.save(newUser);

                    // 신규는 프로필 미완료 상태로 시작하니 NEW_USER_REGISTERED
                    return UserAuthDecision.noToken(AuthResultStatus.NEW_USER_REGISTERED);
                });
    }

    //내부 DTOs (추후 user-service request/response DTO

    private record InfoFromOAuth2User(String email, String sub, String name, String department) { }


    private record UserAuthDecision(AuthResultStatus resultStatus,
                                    boolean shouldIssueToken,
                                    String tokenSubject) {
        static UserAuthDecision issueToken(AuthResultStatus status, String tokenSubject) {
            return new UserAuthDecision(status, true, tokenSubject);
        }
        static UserAuthDecision noToken(AuthResultStatus status) {
            return new UserAuthDecision(status, false, null);
        }
    }

}
