package inu.codin.codin.common.security.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomOAuth2AccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final AppleOAuth2AccessTokenResponseClient appleClient;
    private final DefaultAuthorizationCodeTokenResponseClient googleClient; // Google 기본 클라이언트

    public CustomOAuth2AccessTokenResponseClient(AppleOAuth2AccessTokenResponseClient appleClient) {
        this.appleClient = appleClient;
        this.googleClient = new DefaultAuthorizationCodeTokenResponseClient(); // Spring Security 기본 Google 클라이언트
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        String registrationId = authorizationGrantRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2AccessTokenResponseClient - registrationId: {}", registrationId);

        if ("apple".equalsIgnoreCase(registrationId)) {
            log.info("Handling Apple OAuth2 token response...");
            return appleClient.getTokenResponse(authorizationGrantRequest);
        } else {
            log.info("Handling Google OAuth2 token response...");
            return googleClient.getTokenResponse(authorizationGrantRequest);
        }
    }
}
