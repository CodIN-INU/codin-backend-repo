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
class GoogleAuthServiceTest {

    @Mock
    private JwtTokenIssuer jwtTokenIssuer;

    @Mock
    private UserInternalAuthClient userInternalAuthClient;

    @InjectMocks
    private GoogleAuthService googleAuthService;

    @Mock
    private HttpServletResponse response;

    private OAuth2User createGoogleOAuth2User(String email, String familyName, String givenName, String fullName) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        attributes.put("family_name", familyName);
        attributes.put("given_name", givenName);
        attributes.put("name", fullName);
        attributes.put("sub", "google-sub-123");
        return new DefaultOAuth2User(Collections.emptyList(), attributes, "sub");
    }

    @Test
    @DisplayName("기존 활성 유저 - Google 로그인 성공 시 JWT 토큰 발급")
    void oauthLogin_existingActiveUser_returnsLoginSuccess() {
        // given
        OAuth2User oAuth2User = createGoogleOAuth2User("test@inu.ac.kr", "김", "컴퓨터공학부", "김테스트");
        UserOAuthDecision decision = new UserOAuthDecision(
                "ACTIVE", false, true, "test@inu.ac.kr", "userId123", "ROLE_USER"
        );
        when(userInternalAuthClient.oauthDecision(any(UserOAuthDecisionRequest.class))).thenReturn(decision);

        // when
        AuthResultStatus result = googleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(result).isEqualTo(AuthResultStatus.LOGIN_SUCCESS);
        verify(jwtTokenIssuer).deleteToken(response);
        verify(jwtTokenIssuer).createTokenForOAuth("test@inu.ac.kr", "userId123", "ROLE_USER", response);
    }

    @Test
    @DisplayName("신규 유저 - Google 로그인 시 토큰 미발급, NEW_USER_REGISTERED 반환")
    void oauthLogin_newUser_returnsNewUserRegistered() {
        // given
        OAuth2User oAuth2User = createGoogleOAuth2User("new@inu.ac.kr", "박", "정보통신공학부", "박신규");
        UserOAuthDecision decision = new UserOAuthDecision(
                "DISABLED", true, false, "new@inu.ac.kr", "userId456", "ROLE_USER"
        );
        when(userInternalAuthClient.oauthDecision(any())).thenReturn(decision);

        // when
        AuthResultStatus result = googleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(result).isEqualTo(AuthResultStatus.NEW_USER_REGISTERED);
        verify(jwtTokenIssuer, never()).createTokenForOAuth(any(), any(), any(), any());
    }

    @Test
    @DisplayName("프로필 미완료 유저 - PROFILE_INCOMPLETE 반환, 토큰 미발급")
    void oauthLogin_profileIncomplete_returnsProfileIncomplete() {
        // given
        OAuth2User oAuth2User = createGoogleOAuth2User("incomplete@inu.ac.kr", "이", "전기공학부", "이미완");
        UserOAuthDecision decision = new UserOAuthDecision(
                "ACTIVE", false, false, "incomplete@inu.ac.kr", "userId789", "ROLE_USER"
        );
        when(userInternalAuthClient.oauthDecision(any())).thenReturn(decision);

        // when
        AuthResultStatus result = googleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(result).isEqualTo(AuthResultStatus.PROFILE_INCOMPLETE);
        verify(jwtTokenIssuer, never()).createTokenForOAuth(any(), any(), any(), any());
    }

    @Test
    @DisplayName("정지 유저 - SUSPENDED_USER 반환, 토큰 미발급")
    void oauthLogin_suspendedUser_returnsSuspended() {
        // given
        OAuth2User oAuth2User = createGoogleOAuth2User("suspended@inu.ac.kr", "최", "건축학부", "최정지");
        UserOAuthDecision decision = new UserOAuthDecision(
                "SUSPENDED", false, true, "suspended@inu.ac.kr", "userId000", "ROLE_USER"
        );
        when(userInternalAuthClient.oauthDecision(any())).thenReturn(decision);

        // when
        AuthResultStatus result = googleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(result).isEqualTo(AuthResultStatus.SUSPENDED_USER);
        verify(jwtTokenIssuer, never()).createTokenForOAuth(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Google OAuth 파싱 - family_name을 name으로, given_name을 department로 매핑")
    void oauthLogin_parsesGoogleAttributes_correctly() {
        // given
        OAuth2User oAuth2User = createGoogleOAuth2User("test@inu.ac.kr", "김", "컴퓨터공학부", "김컴퓨터공학부");
        UserOAuthDecision decision = new UserOAuthDecision(
                "ACTIVE", false, true, "test@inu.ac.kr", "userId123", "ROLE_USER"
        );
        ArgumentCaptor<UserOAuthDecisionRequest> captor = ArgumentCaptor.forClass(UserOAuthDecisionRequest.class);
        when(userInternalAuthClient.oauthDecision(captor.capture())).thenReturn(decision);

        // when
        googleAuthService.oauthLogin(oAuth2User, response);

        // then
        UserOAuthDecisionRequest request = captor.getValue();
        assertThat(request.provider()).isEqualTo("GOOGLE");
        assertThat(request.identifier()).isEqualTo("test@inu.ac.kr");
        assertThat(request.name()).isEqualTo("김");              // family_name
        assertThat(request.department()).isEqualTo("컴퓨터공학부"); // given_name
    }

    @Test
    @DisplayName("Google OAuth 파싱 - family_name 없으면 fullName 사용")
    void oauthLogin_fallsBackToFullName_whenFamilyNameEmpty() {
        // given
        OAuth2User oAuth2User = createGoogleOAuth2User("test@inu.ac.kr", "", null, "John Doe");
        UserOAuthDecision decision = new UserOAuthDecision(
                "ACTIVE", false, true, "test@inu.ac.kr", "userId123", "ROLE_USER"
        );
        ArgumentCaptor<UserOAuthDecisionRequest> captor = ArgumentCaptor.forClass(UserOAuthDecisionRequest.class);
        when(userInternalAuthClient.oauthDecision(captor.capture())).thenReturn(decision);

        // when
        googleAuthService.oauthLogin(oAuth2User, response);

        // then
        UserOAuthDecisionRequest request = captor.getValue();
        assertThat(request.name()).isEqualTo("John Doe"); // fullName fallback
        assertThat(request.department()).isEqualTo("");    // null given_name -> ""
    }

    @Test
    @DisplayName("DISABLED 상태이고 신규가 아닌 유저 - PROFILE_INCOMPLETE 반환")
    void oauthLogin_disabledNotNew_returnsProfileIncomplete() {
        // given
        OAuth2User oAuth2User = createGoogleOAuth2User("disabled@inu.ac.kr", "정", "기계공학부", "정비활성");
        UserOAuthDecision decision = new UserOAuthDecision(
                "DISABLED", false, false, "disabled@inu.ac.kr", "userId111", "ROLE_USER"
        );
        when(userInternalAuthClient.oauthDecision(any())).thenReturn(decision);

        // when
        AuthResultStatus result = googleAuthService.oauthLogin(oAuth2User, response);

        // then
        assertThat(result).isEqualTo(AuthResultStatus.PROFILE_INCOMPLETE);
        verify(jwtTokenIssuer, never()).createTokenForOAuth(any(), any(), any(), any());
    }
}
