package inu.codin.auth.service.oauth2;

import inu.codin.auth.dto.user.TokenIssuanceDecision;
import inu.codin.auth.dto.user.UserOAuthDecision;
import inu.codin.auth.dto.user.UserOAuthDecisionRequest;
import inu.codin.auth.enums.AuthResultStatus;
import inu.codin.auth.feign.UserInternalAuthClient;
import inu.codin.auth.jwt.JwtTokenIssuer;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractAuthService {
    protected final JwtTokenIssuer jwtTokenIssuer;
    protected final UserInternalAuthClient userInternalAuthClient;

    // 통일된 토큰 발급 메서드
    protected void issueJwtToken(TokenIssuanceDecision decision, HttpServletResponse response) {
        jwtTokenIssuer.deleteToken(response);
        jwtTokenIssuer.createTokenForOAuth(
            decision.email(), 
            decision.userId(), 
            decision.authorities(), 
            response
        );
    }

    protected AuthResultStatus mapToAuthResultStatus(UserOAuthDecision d) {
        if ("SUSPENDED".equals(d.userStatus())) return AuthResultStatus.SUSPENDED_USER;
        if (d.isNewUser()) return AuthResultStatus.NEW_USER_REGISTERED;
        if (!d.profileCompleted() || "DISABLED".equals(d.userStatus())) return AuthResultStatus.PROFILE_INCOMPLETE;
        return AuthResultStatus.LOGIN_SUCCESS;
    }

    protected boolean shouldIssueToken(UserOAuthDecision d) {
        return "ACTIVE".equals(d.userStatus()) && !d.isNewUser() && d.profileCompleted();
    }

    protected UserOAuthDecision callUserServiceForDecision(String provider, String identifier, String name, String department) {
        UserOAuthDecisionRequest request = new UserOAuthDecisionRequest(
                provider, identifier, name, department
        );
        return userInternalAuthClient.oauthDecision(request);
    }

}
