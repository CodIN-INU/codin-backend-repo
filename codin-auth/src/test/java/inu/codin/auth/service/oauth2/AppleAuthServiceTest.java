package inu.codin.auth.service.oauth2;

import inu.codin.auth.dto.user.UserOAuthDecision;
import inu.codin.auth.dto.user.UserOAuthDecisionRequest;
import inu.codin.auth.enums.AuthResultStatus;
import inu.codin.auth.feign.UserInternalAuthClient;
import inu.codin.auth.jwt.JwtTokenIssuer;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppleAuthServiceTest {

    @Mock
    private JwtTokenIssuer jwtTokenIssuer;

    @Mock
    private UserInternalAuthClient userInternalAuthClient;

    @InjectMocks
    private AppleAuthService appleAuthService;

    @Mock
    private HttpServletResponse response;

    private OAuth2User createAppleOAuth2User(String email, String sub, String name) {
        Map<String, Object> attributes = new HashMap<>();
        if (email != null) attributes.put("email", email);
        attributes.put("sub", sub);
        if (name != null) attributes.put("name", name);
        return new DefaultOAuth2User(Collections.emptyList(), attributes, "sub");
    }

    @Test
    @DisplayName("Apple 로그인 - email 있는 기존 유저 정상 로그인")
    void oauthLogin_withEmail_existingUser_success() {
        // given
        OAuth2User oAuth2User = createAppleOAuth2User("test@inu.ac.kr", "apple-sub-123", "김테스트");
        UserOAuthDecision decision = new UserOAuthDecision(
                "ACTIVE", false, true, "test@inu.ac.kr", "userId123", "ROLE_USER"
        );
        when(userInternalAuthClient.oauthDecision(any())).thenReturn(decision);

        // when
        AuthResultStatus result = appleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(result).isEqualTo(AuthResultStatus.LOGIN_SUCCESS);
        verify(jwtTokenIssuer).deleteToken(response);
        verify(jwtTokenIssuer).createTokenForOAuth("test@inu.ac.kr", "userId123", "ROLE_USER", response);
    }

    @Test
    @DisplayName("Apple 로그인 - email 없으면 sub를 identifier로 사용")
    void oauthLogin_withoutEmail_usesSub() {
        // given
        OAuth2User oAuth2User = createAppleOAuth2User(null, "apple-sub-456", "박테스트");
        UserOAuthDecision decision = new UserOAuthDecision(
                "ACTIVE", false, true, "apple-sub-456", "userId789", "ROLE_USER"
        );
        ArgumentCaptor<UserOAuthDecisionRequest> captor = ArgumentCaptor.forClass(UserOAuthDecisionRequest.class);
        when(userInternalAuthClient.oauthDecision(captor.capture())).thenReturn(decision);

        // when
        AuthResultStatus result = appleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(result).isEqualTo(AuthResultStatus.LOGIN_SUCCESS);
        UserOAuthDecisionRequest request = captor.getValue();
        assertThat(request.provider()).isEqualTo("APPLE");
        assertThat(request.identifier()).isEqualTo("apple-sub-456"); // sub fallback
    }

    @Test
    @DisplayName("Apple 로그인 - email 빈 문자열이면 sub를 identifier로 사용")
    void oauthLogin_emptyEmail_usesSub() {
        // given
        OAuth2User oAuth2User = createAppleOAuth2User("", "apple-sub-789", "이테스트");
        UserOAuthDecision decision = new UserOAuthDecision(
                "ACTIVE", false, true, "apple-sub-789", "userId000", "ROLE_USER"
        );
        ArgumentCaptor<UserOAuthDecisionRequest> captor = ArgumentCaptor.forClass(UserOAuthDecisionRequest.class);
        when(userInternalAuthClient.oauthDecision(captor.capture())).thenReturn(decision);

        // when
        appleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(captor.getValue().identifier()).isEqualTo("apple-sub-789");
    }

    @Test
    @DisplayName("Apple 로그인 - 신규 유저 등록, 토큰 미발급")
    void oauthLogin_newUser_noTokenIssued() {
        // given
        OAuth2User oAuth2User = createAppleOAuth2User("new@inu.ac.kr", "apple-sub-new", "최신규");
        UserOAuthDecision decision = new UserOAuthDecision(
                "DISABLED", true, false, "new@inu.ac.kr", "userIdNew", "ROLE_USER"
        );
        when(userInternalAuthClient.oauthDecision(any())).thenReturn(decision);

        // when
        AuthResultStatus result = appleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(result).isEqualTo(AuthResultStatus.NEW_USER_REGISTERED);
        verify(jwtTokenIssuer, never()).createTokenForOAuth(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Apple 로그인 - name 없으면 family_name fallback")
    void oauthLogin_nameFallback_toFamilyName() {
        // given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@inu.ac.kr");
        attributes.put("sub", "apple-sub-fb");
        attributes.put("family_name", "김");
        // name 속성 없음
        OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "sub");

        UserOAuthDecision decision = new UserOAuthDecision(
                "ACTIVE", false, true, "test@inu.ac.kr", "userId123", "ROLE_USER"
        );
        ArgumentCaptor<UserOAuthDecisionRequest> captor = ArgumentCaptor.forClass(UserOAuthDecisionRequest.class);
        when(userInternalAuthClient.oauthDecision(captor.capture())).thenReturn(decision);

        // when
        appleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(captor.getValue().name()).isEqualTo("김"); // family_name fallback
    }

    @Test
    @DisplayName("Apple 로그인 - department는 항상 빈 문자열")
    void oauthLogin_departmentAlwaysEmpty() {
        // given
        OAuth2User oAuth2User = createAppleOAuth2User("test@inu.ac.kr", "apple-sub-dep", "테스트");
        UserOAuthDecision decision = new UserOAuthDecision(
                "ACTIVE", false, true, "test@inu.ac.kr", "userId123", "ROLE_USER"
        );
        ArgumentCaptor<UserOAuthDecisionRequest> captor = ArgumentCaptor.forClass(UserOAuthDecisionRequest.class);
        when(userInternalAuthClient.oauthDecision(captor.capture())).thenReturn(decision);

        // when
        appleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(captor.getValue().department()).isEmpty();
    }

    @Test
    @DisplayName("Apple 정지 유저 - SUSPENDED_USER 반환")
    void oauthLogin_suspendedUser() {
        // given
        OAuth2User oAuth2User = createAppleOAuth2User("suspended@inu.ac.kr", "apple-sub-sus", "정지유저");
        UserOAuthDecision decision = new UserOAuthDecision(
                "SUSPENDED", false, true, "suspended@inu.ac.kr", "userIdSus", "ROLE_USER"
        );
        when(userInternalAuthClient.oauthDecision(any())).thenReturn(decision);

        // when
        AuthResultStatus result = appleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(result).isEqualTo(AuthResultStatus.SUSPENDED_USER);
        verify(jwtTokenIssuer, never()).createTokenForOAuth(any(), any(), any(), any());
    }
}
