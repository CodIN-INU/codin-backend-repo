package inu.codin.codin.common.security.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import inu.codin.codin.common.security.dto.apple.AppleAuthRequest;
import inu.codin.codin.common.security.dto.apple.AppleOAuth2User;
import inu.codin.codin.common.security.feign.AppleAuthClient;
import inu.codin.codin.common.security.jwt.IdentityTokenValidator;
import inu.codin.codin.common.security.util.ApplePublicKeyGenerator;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AppleAuthClient appleAuthClient;
    private final ApplePublicKeyGenerator applePublicKeyGenerator;
    private final IdentityTokenValidator identityTokenValidator;



    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        //AppleAuthRequest 생성 (id_token, authorization code, 사용자 정보))
        AppleAuthRequest appleAuthRequest = extractAppleAuthRequest(userRequest);
        log.info("추출된 AppleAuthRequest: identityToken={}, authorizationCode={}, user={}",
                appleAuthRequest.getIdentityToken(), appleAuthRequest.getAuthorizationCode(), appleAuthRequest.getUser());


        //Apple ID Token 검증 및 사용자 ID (sub) 추출
        //OAuth2AuthenticationException에는 (String, Exception) 생성자가 없으므로, OAuth2Error를 생성하여 사용
        String appleAccountId;
        try {
            appleAccountId = getAppleAccountId(appleAuthRequest.getIdentityToken());
            log.info("Apple id_token 검증 성공, accountId(sub): {}", appleAccountId);
        } catch (Exception e) {
            OAuth2Error oauth2Error = new OAuth2Error("invalid_token", "Apple id_token 검증 실패", null);
            throw new OAuth2AuthenticationException(oauth2Error, e);
        }


        String email = appleAuthRequest.getEmail();

        // Apple OAuth2 정보를 기반으로 `AppleOAuth2User` 객체 반환
        Map<String, Object> attributes = new HashMap<>();
        log.info("AppleOAuth2User 생성, attributes: {}", attributes);
        attributes.put("sub", appleAccountId);
        attributes.put("email", email);

        return new AppleOAuth2User(attributes);
    }

    /**
     * AppleAuthRequest 생성 (OAuth2UserRequest에서 값 추출)
     */
    private AppleAuthRequest extractAppleAuthRequest(OAuth2UserRequest userRequest) {
        String identityToken = userRequest.getAdditionalParameters().get("id_token").toString();
        String authorizationCode = userRequest.getAdditionalParameters().get("code").toString();
        Map<String, Object> user = userRequest.getAdditionalParameters().containsKey("user") ?
                (Map<String, Object>) userRequest.getAdditionalParameters().get("user") : Map.of();

        return new AppleAuthRequest(identityToken, authorizationCode, user);
    }

    /**
     * Apple ID Token 검증 및 사용자 계정 ID(sub) 추출
     */
    public String getAppleAccountId(String identityToken)
            throws JsonProcessingException, AuthenticationException, NoSuchAlgorithmException,
            InvalidKeySpecException {
        Map<String, String> headers = identityTokenValidator.parseHeaders(identityToken);
        PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(headers,
                appleAuthClient.getAppleAuthPublicKey());

        return identityTokenValidator.getTokenClaims(identityToken, publicKey).getSubject();
    }
}