package inu.codin.auth.service.oauth2;

import inu.codin.auth.enums.AuthResultStatus;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface Oauth2AuthService {
    AuthResultStatus oauthLogin(OAuth2User oAuth2User, HttpServletResponse response);
}
