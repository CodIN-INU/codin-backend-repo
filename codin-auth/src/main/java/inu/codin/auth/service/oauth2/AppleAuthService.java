package inu.codin.auth.service.oauth2;

import inu.codin.auth.dto.user.UserOAuthDecision;
import inu.codin.auth.feign.UserInternalAuthClient;
import inu.codin.auth.jwt.JwtTokenIssuer;
import inu.codin.auth.enums.AuthResultStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class AppleAuthService extends AbstractAuthService implements Oauth2AuthService {

    public AppleAuthService(JwtTokenIssuer jwtTokenIssuer, UserInternalAuthClient userInternalAuthClient) {
        super(jwtTokenIssuer, userInternalAuthClient);
    }

    @Override
    public AuthResultStatus oauthLogin(OAuth2User oAuth2User, HttpServletResponse response) {
        // AUTH: OAuth2User -> 내부 표준 형태로 정규화
        InfoFromOAuth2User info = authParseAppleOAuthUser(oAuth2User);

        // AUTH: 식별자 결정 (email 우선, 없으면 sub)
        String identifier = authResolveIdentifier(info);

        // USER: 기존/신규 + 상태 판단을 user-service에서 받기
        UserOAuthDecision decision = callUserServiceForDecision("APPLE", identifier, info.name(), "");

        // AUTH: 토큰 발급
        AuthResultStatus status = mapToAuthResultStatus(decision);

        if (shouldIssueToken(decision)) {
            issueJwtToken(decision.toTokenDecision(), response);
        }
        // AUTH: 최종 응답 상태 반환
        return status;
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


    //내부 DTOs (추후 user-service request/response DTO

    private record InfoFromOAuth2User(String email, String sub, String name, String department) { }


}
