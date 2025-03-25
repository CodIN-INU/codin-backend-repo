package inu.codin.codin.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/***
 *  OAuth2 인증 요청을 가로채서 커스텀 로직을 적용하는 역할
 *  Apple OAuth2 요청을 감지 -> response_mode=form_post를 추가.
 */
@Component
@Slf4j
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;


    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        // 기본 엔드포인트가 /oauth2/authorization
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        log.debug("resolve(HttpServletRequest) 호출됨, authorizationRequest: {}", authorizationRequest);
        // registrationId가 없는 경우, redirect_uri를 통해 Apple 요청 여부를 유추
        String clientRegistrationId = null;
        if (authorizationRequest != null && authorizationRequest.getRedirectUri() != null
                && authorizationRequest.getRedirectUri().contains("/login/oauth2/code/apple")) {
            clientRegistrationId = "apple";
        }
        return customizeAuthorizationRequest(authorizationRequest, clientRegistrationId);
    }

    //특정 clientRegistrationId(Google, Apple 등)를 명시적으로 전달받아 OAuth2 인증 요청을 해석한
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        log.debug("resolve(HttpServletRequest, {}) 호출됨, authorizationRequest: {}", clientRegistrationId, authorizationRequest);
        return customizeAuthorizationRequest(authorizationRequest, clientRegistrationId);
    }


    //Apple 요청 감지 및 커스텀 처리
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, String clientRegistrationId) {
        if (authorizationRequest == null) {
            log.debug("authorizationRequest is null");
            return null;
        }

        String registrationId = clientRegistrationId;
        if (registrationId == null) {
            log.debug("clientRegistrationId is null; attempting to deduce registrationId from redirectUri");
            String redirectUri = authorizationRequest.getRedirectUri();
            if (redirectUri != null && redirectUri.contains("/login/oauth2/code/apple")) {
                registrationId = "apple";
            }
        }
        log.debug("Determined registrationId: {}", registrationId);

        if ("apple".equalsIgnoreCase(registrationId)) {
            log.info("Apple OAuth2 요청 감지, 추가 파라미터 적용: response_mode=form_post");
            Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());
            additionalParameters.put("response_mode", "form_post");
            OAuth2AuthorizationRequest customizedRequest = OAuth2AuthorizationRequest.from(authorizationRequest)
                    .additionalParameters(additionalParameters)
                    .build();
            log.debug("customizedRequest: {}", customizedRequest);
            return customizedRequest;
        }
        return authorizationRequest;
    }
}
