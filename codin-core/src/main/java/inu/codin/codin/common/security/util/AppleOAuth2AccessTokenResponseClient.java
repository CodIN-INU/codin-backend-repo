package inu.codin.codin.common.security.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

/***
 * Apple에서 받은 code를 사용하여 Access Token을 요청
 */
@Component
@Slf4j
public class AppleOAuth2AccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final AppleClientSecretGenerator appleClientSecretGenerator;
    private final RestTemplate restTemplate = new RestTemplate();

    public AppleOAuth2AccessTokenResponseClient(AppleClientSecretGenerator appleClientSecretGenerator) {
        this.appleClientSecretGenerator = appleClientSecretGenerator;
    }


    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        RequestEntity<MultiValueMap<String, String>> requestEntity = buildRequestEntity(authorizationGrantRequest);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(requestEntity, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponseParameters = responseEntity.getBody();
        return convertTokenResponse(tokenResponseParameters);
    }

    private RequestEntity<MultiValueMap<String, String>> buildRequestEntity(OAuth2AuthorizationCodeGrantRequest request) {
        // 클라이언트 등록 정보
        var clientRegistration = request.getClientRegistration();

        // 요청 파라미터 구성
        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add(OAuth2ParameterNames.GRANT_TYPE, "authorization_code");
        formParameters.add(OAuth2ParameterNames.CODE, request.getAuthorizationExchange().getAuthorizationResponse().getCode());
        formParameters.add(OAuth2ParameterNames.REDIRECT_URI, clientRegistration.getRedirectUri());
        formParameters.add(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());

        try {
            String dynamicClientSecret = appleClientSecretGenerator.generateAppleClientSecret();
            log.info("dynamicClientSecret : {}", dynamicClientSecret);
            formParameters.add(OAuth2ParameterNames.CLIENT_SECRET, dynamicClientSecret);
        } catch (Exception ex) {
            OAuth2Error error = new OAuth2Error("client_secret_generation_error",
                    "Failed to generate Apple client secret", null);
            throw new OAuth2AuthenticationException(error, ex);
        }

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        URI tokenUri = URI.create(clientRegistration.getProviderDetails().getTokenUri());
        return new RequestEntity<>(formParameters, headers, HttpMethod.POST, tokenUri);
    }

    private OAuth2AccessTokenResponse convertTokenResponse(Map<String, Object> tokenResponseParameters) {
        String accessToken = (String) tokenResponseParameters.get("access_token");
        String refreshToken = (String) tokenResponseParameters.get("refresh_token");
        Integer expiresIn = tokenResponseParameters.get("expires_in") instanceof Integer
                ? (Integer) tokenResponseParameters.get("expires_in") : null;

        return OAuth2AccessTokenResponse.withToken(accessToken)
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(expiresIn != null ? expiresIn.longValue() : 0L)
                .refreshToken(refreshToken)
                .additionalParameters(tokenResponseParameters)
                .build();
    }
}
