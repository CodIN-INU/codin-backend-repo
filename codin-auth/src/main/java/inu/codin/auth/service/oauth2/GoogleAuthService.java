package inu.codin.auth.service.oauth2;

import inu.codin.auth.dto.user.UserOAuthDecision;
import inu.codin.auth.feign.UserInternalAuthClient;
import inu.codin.auth.jwt.JwtTokenIssuer;
import inu.codin.auth.enums.AuthResultStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class GoogleAuthService extends AbstractAuthService implements Oauth2AuthService {

    public GoogleAuthService(JwtTokenIssuer jwtTokenIssuer, UserDetailsService userDetailsService, UserInternalAuthClient userInternalAuthClient) {
        super(jwtTokenIssuer, userDetailsService, userInternalAuthClient);
    }

    @Override
    public AuthResultStatus oauthLogin(OAuth2User oAuth2User, HttpServletResponse response) {
        // AUTH: OAuth2User -> 표준 Info로 파싱/정규화
        InfoFromOAuth2User info = authParseGoogleOAuthUser(oAuth2User);

        // USER: 기존/신규 + 상태 판단을 user-service에서 받기
        UserOAuthDecision decision = callUserServiceForDecision("GOOGLE", info.email(), info.name(), info.department());

        // AUTH: 토큰 발급은 Auth 책임
        AuthResultStatus status = mapToAuthResultStatus(decision);

        if (shouldIssueToken(decision)) {
            issueJwtToken(decision.tokenSubject(), response);
        }

        return status;


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


    private record InfoFromOAuth2User(String email, String name, String department) { }
}