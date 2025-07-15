package inu.codin.codin.common.security.service;

import inu.codin.codin.common.security.enums.AuthResultStatus;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface Oauth2AuthService {
    AuthResultStatus oauthLogin(OAuth2User oAuth2User, HttpServletResponse response);
}
