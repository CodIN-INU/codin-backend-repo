package inu.codin.auth.service.oauth2;

import inu.codin.auth.dto.user.UserOAuthDecision;
import inu.codin.auth.enums.AuthResultStatus;
import inu.codin.auth.feign.UserInternalAuthClient;
import inu.codin.auth.jwt.JwtTokenIssuer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AbstractAuthServiceTest {

    @Mock
    private JwtTokenIssuer jwtTokenIssuer;

    @Mock
    private UserInternalAuthClient userInternalAuthClient;

    // 테스트용 구체 클래스
    private AbstractAuthService createService() {
        return new AbstractAuthService(jwtTokenIssuer, userInternalAuthClient) {};
    }

    @Nested
    @DisplayName("mapToAuthResultStatus 테스트")
    class MapToAuthResultStatusTest {

        @Test
        @DisplayName("SUSPENDED 상태 -> SUSPENDED_USER")
        void suspended_returnsSuspendedUser() {
            var service = createService();
            var decision = new UserOAuthDecision("SUSPENDED", false, true, "email", "id", "ROLE_USER");
            assertThat(service.mapToAuthResultStatus(decision)).isEqualTo(AuthResultStatus.SUSPENDED_USER);
        }

        @Test
        @DisplayName("신규 유저 -> NEW_USER_REGISTERED")
        void newUser_returnsNewUserRegistered() {
            var service = createService();
            var decision = new UserOAuthDecision("DISABLED", true, false, "email", "id", "ROLE_USER");
            assertThat(service.mapToAuthResultStatus(decision)).isEqualTo(AuthResultStatus.NEW_USER_REGISTERED);
        }

        @Test
        @DisplayName("프로필 미완료 -> PROFILE_INCOMPLETE")
        void profileNotCompleted_returnsProfileIncomplete() {
            var service = createService();
            var decision = new UserOAuthDecision("ACTIVE", false, false, "email", "id", "ROLE_USER");
            assertThat(service.mapToAuthResultStatus(decision)).isEqualTo(AuthResultStatus.PROFILE_INCOMPLETE);
        }

        @Test
        @DisplayName("DISABLED 상태이고 프로필 완료 -> PROFILE_INCOMPLETE")
        void disabled_profileCompleted_returnsProfileIncomplete() {
            var service = createService();
            var decision = new UserOAuthDecision("DISABLED", false, true, "email", "id", "ROLE_USER");
            assertThat(service.mapToAuthResultStatus(decision)).isEqualTo(AuthResultStatus.PROFILE_INCOMPLETE);
        }

        @Test
        @DisplayName("ACTIVE + 기존유저 + 프로필완료 -> LOGIN_SUCCESS")
        void active_existingUser_profileCompleted_returnsLoginSuccess() {
            var service = createService();
            var decision = new UserOAuthDecision("ACTIVE", false, true, "email", "id", "ROLE_USER");
            assertThat(service.mapToAuthResultStatus(decision)).isEqualTo(AuthResultStatus.LOGIN_SUCCESS);
        }

        @Test
        @DisplayName("SUSPENDED 우선순위 - 신규유저여도 SUSPENDED가 우선")
        void suspended_takePriority_overNewUser() {
            var service = createService();
            var decision = new UserOAuthDecision("SUSPENDED", true, false, "email", "id", "ROLE_USER");
            assertThat(service.mapToAuthResultStatus(decision)).isEqualTo(AuthResultStatus.SUSPENDED_USER);
        }
    }

    @Nested
    @DisplayName("shouldIssueToken 테스트")
    class ShouldIssueTokenTest {

        @Test
        @DisplayName("ACTIVE + 기존유저 + 프로필완료 -> true")
        void active_existingUser_profileCompleted_returnsTrue() {
            var service = createService();
            var decision = new UserOAuthDecision("ACTIVE", false, true, "email", "id", "ROLE_USER");
            assertThat(service.shouldIssueToken(decision)).isTrue();
        }

        @Test
        @DisplayName("신규 유저 -> false")
        void newUser_returnsFalse() {
            var service = createService();
            var decision = new UserOAuthDecision("ACTIVE", true, false, "email", "id", "ROLE_USER");
            assertThat(service.shouldIssueToken(decision)).isFalse();
        }

        @Test
        @DisplayName("프로필 미완료 -> false")
        void profileNotCompleted_returnsFalse() {
            var service = createService();
            var decision = new UserOAuthDecision("ACTIVE", false, false, "email", "id", "ROLE_USER");
            assertThat(service.shouldIssueToken(decision)).isFalse();
        }

        @Test
        @DisplayName("SUSPENDED -> false")
        void suspended_returnsFalse() {
            var service = createService();
            var decision = new UserOAuthDecision("SUSPENDED", false, true, "email", "id", "ROLE_USER");
            assertThat(service.shouldIssueToken(decision)).isFalse();
        }

        @Test
        @DisplayName("DISABLED -> false")
        void disabled_returnsFalse() {
            var service = createService();
            var decision = new UserOAuthDecision("DISABLED", false, true, "email", "id", "ROLE_USER");
            assertThat(service.shouldIssueToken(decision)).isFalse();
        }
    }
}
