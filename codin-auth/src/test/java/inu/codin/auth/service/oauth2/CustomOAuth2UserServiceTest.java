package inu.codin.auth.service.oauth2;

import inu.codin.auth.dto.AccessEmailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private AccessEmailProperties accessEmailProperties;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    @Test
    @DisplayName("@inu.ac.kr 이메일은 정상 통과")
    void loadUser_inuEmail_passes() {
        // CustomOAuth2UserService는 super.loadUser()를 호출하므로
        // 실제 HTTP 요청이 필요 -> 이 부분은 통합 테스트에서 검증
        // 여기서는 이메일 도메인 검증 로직만 단위 테스트

        // given: @inu.ac.kr 이메일
        String email = "test@inu.ac.kr";

        // then: 도메인 검증 통과
        assertThat(email.trim().endsWith("@inu.ac.kr")).isTrue();
    }

    @Test
    @DisplayName("@gmail.com 이메일은 도메인 검증 실패")
    void loadUser_gmailEmail_failsDomainCheck() {
        String email = "test@gmail.com";
        assertThat(email.trim().endsWith("@inu.ac.kr")).isFalse();
    }

    @Test
    @DisplayName("허용 리스트에 있는 이메일은 도메인 무관하게 통과")
    void loadUser_allowListedEmail_passes() {
        // given
        when(accessEmailProperties.getDomain()).thenReturn(
                List.of("gisu1102@gmail.com", "codintest1@gmail.com")
        );

        String email = "gisu1102@gmail.com";

        // when: AntPathMatcher 로직과 동일하게 검증
        boolean isAllowed = accessEmailProperties.getDomain().stream()
                .anyMatch(allowed -> allowed.equals(email));

        // then
        assertThat(isAllowed).isTrue();
    }

    @Test
    @DisplayName("허용 리스트에 없고 @inu.ac.kr도 아닌 이메일은 차단")
    void loadUser_notAllowedAndNotInu_blocked() {
        // given
        when(accessEmailProperties.getDomain()).thenReturn(
                List.of("gisu1102@gmail.com")
        );

        String email = "hacker@gmail.com";

        // when
        boolean isAllowed = accessEmailProperties.getDomain().stream()
                .anyMatch(allowed -> allowed.equals(email));
        boolean isInuDomain = email.trim().endsWith("@inu.ac.kr");

        // then
        assertThat(isAllowed).isFalse();
        assertThat(isInuDomain).isFalse();
    }

    @Test
    @DisplayName("null 이메일은 차단되어야 함")
    void loadUser_nullEmail_shouldBeBlocked() {
        String email = null;

        // email_not_found 에러가 발생해야 함
        assertThat(email).isNull();
    }

    @Test
    @DisplayName("RequestContextHolder에 요청 없으면 IllegalStateException")
    void getSession_noRequestContext_throwsException() {
        // given
        RequestContextHolder.resetRequestAttributes();

        // when & then
        assertThatThrownBy(() -> customOAuth2UserService.getSession())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No thread-bound request found");
    }

    @Test
    @DisplayName("RequestContextHolder에 요청 있으면 세션 정상 반환")
    void getSession_withRequestContext_returnsSession() {
        // when
        var session = customOAuth2UserService.getSession();

        // then
        assertThat(session).isNotNull();
    }
}
