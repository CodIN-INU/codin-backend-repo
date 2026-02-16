package inu.codin.auth.handler;

import inu.codin.auth.enums.AuthResultStatus;
import inu.codin.auth.service.oauth2.AppleAuthService;
import inu.codin.auth.service.oauth2.GoogleAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private AppleAuthService appleAuthService;

    @Mock
    private GoogleAuthService googleAuthService;

    @InjectMocks
    private OAuth2LoginSuccessHandler successHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(successHandler, "BASEURL", "https://codin.inu.ac.kr");
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    private OAuth2AuthenticationToken createAuthToken(String provider) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@inu.ac.kr");
        attributes.put("sub", "sub-123");
        OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "sub");
        return new OAuth2AuthenticationToken(oAuth2User, Collections.emptyList(), provider);
    }

    @Test
    @DisplayName("Google 로그인 성공 -> /main으로 리다이렉트")
    void google_loginSuccess_redirectsToMain() throws IOException {
        // given
        OAuth2AuthenticationToken authToken = createAuthToken("google");
        when(googleAuthService.oauthLogin(any(), any())).thenReturn(AuthResultStatus.LOGIN_SUCCESS);

        // when
        successHandler.onAuthenticationSuccess(request, response, authToken);

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo("https://codin.inu.ac.kr/main");
        verify(googleAuthService).oauthLogin(any(), eq(response));
        verify(appleAuthService, never()).oauthLogin(any(), any());
    }

    @Test
    @DisplayName("Apple 로그인 성공 -> /main으로 리다이렉트")
    void apple_loginSuccess_redirectsToMain() throws IOException {
        // given
        OAuth2AuthenticationToken authToken = createAuthToken("apple");
        when(appleAuthService.oauthLogin(any(), any())).thenReturn(AuthResultStatus.LOGIN_SUCCESS);

        // when
        successHandler.onAuthenticationSuccess(request, response, authToken);

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo("https://codin.inu.ac.kr/main");
        verify(appleAuthService).oauthLogin(any(), eq(response));
        verify(googleAuthService, never()).oauthLogin(any(), any());
    }

    @Test
    @DisplayName("신규 유저 -> /auth/profile로 리다이렉트")
    void newUser_redirectsToProfile() throws IOException {
        // given
        OAuth2AuthenticationToken authToken = createAuthToken("google");
        when(googleAuthService.oauthLogin(any(), any())).thenReturn(AuthResultStatus.NEW_USER_REGISTERED);

        // when
        successHandler.onAuthenticationSuccess(request, response, authToken);

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo("https://codin.inu.ac.kr/auth/profile?email=test@inu.ac.kr");
    }

    @Test
    @DisplayName("프로필 미완료 -> /auth/profile로 리다이렉트")
    void profileIncomplete_redirectsToProfile() throws IOException {
        // given
        OAuth2AuthenticationToken authToken = createAuthToken("google");
        when(googleAuthService.oauthLogin(any(), any())).thenReturn(AuthResultStatus.PROFILE_INCOMPLETE);

        // when
        successHandler.onAuthenticationSuccess(request, response, authToken);

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo("https://codin.inu.ac.kr/auth/profile?email=test@inu.ac.kr");
    }

    @Test
    @DisplayName("정지 유저 -> /api/suspends로 리다이렉트")
    void suspendedUser_redirectsToSuspends() throws IOException {
        // given
        OAuth2AuthenticationToken authToken = createAuthToken("google");
        when(googleAuthService.oauthLogin(any(), any())).thenReturn(AuthResultStatus.SUSPENDED_USER);

        // when
        successHandler.onAuthenticationSuccess(request, response, authToken);

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo("https://codin.inu.ac.kr/api/suspends");
    }

    @Test
    @DisplayName("지원하지 않는 provider -> OAuth2AuthenticationException 발생")
    void unsupportedProvider_throwsException() {
        // given
        OAuth2AuthenticationToken authToken = createAuthToken("kakao");

        // when & then
        assertThatThrownBy(() -> successHandler.onAuthenticationSuccess(request, response, authToken))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    @DisplayName("redirect_host 세션 값 있으면 해당 호스트로 리다이렉트")
    void customRedirectHost_usesSessionValue() throws IOException {
        // given
        OAuth2AuthenticationToken authToken = createAuthToken("google");
        when(googleAuthService.oauthLogin(any(), any())).thenReturn(AuthResultStatus.LOGIN_SUCCESS);
        request.getSession().setAttribute("redirect_host", "https://custom.host.com");

        // when
        successHandler.onAuthenticationSuccess(request, response, authToken);

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo("https://custom.host.com/main");
    }

    @Test
    @DisplayName("redirect_path 세션 값 있으면 해당 경로로 리다이렉트")
    void customRedirectPath_usesSessionValue() throws IOException {
        // given
        OAuth2AuthenticationToken authToken = createAuthToken("google");
        when(googleAuthService.oauthLogin(any(), any())).thenReturn(AuthResultStatus.LOGIN_SUCCESS);
        request.getSession().setAttribute("redirect_host", "https://custom.host.com");
        request.getSession().setAttribute("redirect_path", "/custom/path");

        // when
        successHandler.onAuthenticationSuccess(request, response, authToken);

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo("https://custom.host.com/custom/path");
    }

    @Test
    @DisplayName("리다이렉트 후 세션에서 redirect 값 제거됨")
    void redirectAttributes_removedFromSession() throws IOException {
        // given
        OAuth2AuthenticationToken authToken = createAuthToken("google");
        when(googleAuthService.oauthLogin(any(), any())).thenReturn(AuthResultStatus.LOGIN_SUCCESS);
        HttpSession session = request.getSession();
        session.setAttribute("redirect_host", "https://custom.host.com");
        session.setAttribute("redirect_path", "/custom/path");

        // when
        successHandler.onAuthenticationSuccess(request, response, authToken);

        // then
        assertThat(session.getAttribute("redirect_host")).isNull();
        assertThat(session.getAttribute("redirect_path")).isNull();
    }
}
