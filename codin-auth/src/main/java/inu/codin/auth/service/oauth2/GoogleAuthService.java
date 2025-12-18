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
import java.util.Optional;

@Service
@Slf4j
public class GoogleAuthService extends AbstractAuthService implements Oauth2AuthService {


    public GoogleAuthService(JwtTokenIssuer jwtTokenIssuer, UserDetailsService userDetailsService) {
        super(jwtTokenIssuer, userDetailsService);
    }

    @Override
    public AuthResultStatus oauthLogin(OAuth2User oAuth2User, HttpServletResponse response) {
        // AUTH: OAuth2User -> 표준 Info로 파싱/정규화
        InfoFromOAuth2User info = authParseGoogleOAuthUser(oAuth2User);

        // USER: 상태판단/신규등록 포함 한번에 받아오기 (추후 auth -> user API 호출로 교체)
        UserOAuthDecision decision = userDecideGoogleOauthLogin(info);

        // AUTH: 토큰 발급은 Auth 책임
        if (decision.shouldIssueToken()) {
            issueJwtToken(decision.tokenSubject(), response);
            log.info("정상 로그인 완료: {}", decision.tokenSubject());
        }
        // AUTH: 최종 응답 상태 반환
        return decision.resultStatus();


    }

    //AUTH 책임: OAuth2User 파싱/정규화

    private InfoFromOAuth2User authParseGoogleOAuthUser(OAuth2User oAuth2User) {
        // Google에서는 email, family_name, given_name 등 제공됨.
        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.info("OAuth2User attributes: {}", attributes);

        String email = (String) attributes.get("email");
        String familyName = (String) attributes.get("family_name");
        String givenName = (String) attributes.get("given_name");
        String fullName = (String) attributes.get("name");

        String name = (familyName != null && !familyName.isEmpty())
                ? familyName
                : (fullName != null ? fullName : "");

        String department = (givenName != null) ? givenName : "";

        log.info("OAuth2 login parsed values -> email={}, family_name={}, given_name={}, name={}, department={}",
                email, familyName, givenName, fullName, department);

        log.info("OAuth2 login: email={}, name={}, department={}", email, name, department);

        return new InfoFromOAuth2User(email, name, department);
    }

    //USER 책임(통채로 분리): 조회/상태판단/신규등록/결정

    private UserOAuthDecision userDecideGoogleOauthLogin(InfoFromOAuth2User info) {
        // Google은 email이 식별자
        String identifier = info.email();

        Optional<UserEntity> optionalUser = userRepository.findByEmailAndStatusAll(identifier);

        if (optionalUser.isPresent()) {
            UserEntity existingUser = optionalUser.get();
            log.info("기존 Google 회원 로그인: {}", identifier);

            return switch (existingUser.getStatus()) {
                case ACTIVE -> UserOAuthDecision.issueToken(AuthResultStatus.LOGIN_SUCCESS, identifier);

                case DISABLED -> {
                    log.info("회원 프로필 설정 미완료 (userStatus={}): {}", existingUser.getStatus(), identifier);
                    yield UserOAuthDecision.noToken(AuthResultStatus.PROFILE_INCOMPLETE);
                }

                case SUSPENDED -> {
                    log.info("정지된 유저: {}", identifier);
                    yield UserOAuthDecision.noToken(AuthResultStatus.SUSPENDED_USER);
                }

                default -> {
                    log.error("알 수 없는 유저 상태: {}", existingUser.getStatus());
                    throw new NotFoundException("유저의 상태를 알 수 없습니다. _id: " + existingUser.get_id());
                }
            };
        }

        log.info("신규 Google 회원 등록: {}", identifier);

        String deptDesc = (info.department() != null) ? info.department().replace("/", "").trim() : "";
        Department dept = Department.fromDescription(deptDesc);

        UserEntity newUser = UserEntity.builder()
                .email(identifier)
                .name(info.name())
                .department(dept)
                .profileImageUrl(s3Service.getDefaultProfileImageUrl()) // (현재 구조에선 user 책임으로 묶여있음)
                .status(UserStatus.DISABLED)
                .role(UserRole.USER)
                .build();

        userRepository.save(newUser);
        log.info("신규 회원 등록 완료 (프로필 미완료): {}", newUser);

        return UserOAuthDecision.noToken(AuthResultStatus.NEW_USER_REGISTERED);
    }

    //내부 DTOs (추후 user-service request/response DTO

    private record InfoFromOAuth2User(String email, String name, String department) { }

    /**
     * USER 처리 결과(결정)를 Auth가 받아서,
     * - shouldIssueToken이면 토큰 발급
     * - status를 그대로 응답
     */
    private record UserOAuthDecision(AuthResultStatus resultStatus,
                                     boolean shouldIssueToken,
                                     String tokenSubject) {

        static UserOAuthDecision issueToken(AuthResultStatus resultStatus, String tokenSubject) {
            return new UserOAuthDecision(resultStatus, true, tokenSubject);
        }

        static UserOAuthDecision noToken(AuthResultStatus resultStatus) {
            return new UserOAuthDecision(resultStatus, false, null);
        }
    }
}