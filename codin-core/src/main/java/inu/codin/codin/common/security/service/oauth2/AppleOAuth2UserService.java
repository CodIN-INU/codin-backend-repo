package inu.codin.codin.common.security.service.oauth2;


import com.fasterxml.jackson.core.JsonProcessingException;
import inu.codin.codin.common.security.dto.apple.AppleAuthRequest;
import inu.codin.codin.common.security.dto.apple.AppleOAuth2User;
import inu.codin.codin.common.security.feign.AppleAuthClient;
import inu.codin.codin.common.security.jwt.IdentityTokenValidator;
import inu.codin.codin.common.security.util.ApplePublicKeyGenerator;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AppleOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AppleAuthClient appleAuthClient;
    private final ApplePublicKeyGenerator applePublicKeyGenerator;
    private final IdentityTokenValidator identityTokenValidator;



    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        Map<String, Object> additionalParams = userRequest.getAdditionalParameters();
        log.info("추가 파라미터 전체: {}", additionalParams);

        // AppleAuthRequest 생성 (id_token, 사용자 정보)
        AppleAuthRequest appleAuthRequest = extractAppleAuthRequest(userRequest);

        // Apple ID Token 검증 및 사용자 ID (sub) 추출
        Map<String, Object> tokenClaims;
        try {
            tokenClaims = getAppleAccountId(appleAuthRequest.getIdentityToken());
            log.info("Apple id_token 검증 성공, tokenClaims: {}", tokenClaims);
        } catch (Exception e) {
            OAuth2Error oauth2Error = new OAuth2Error("invalid_token", "Apple id_token 검증 실패", null);
            throw new OAuth2AuthenticationException(oauth2Error, e);
        }

        // OAuth2User에 담을 속성 구성
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", tokenClaims.get("sub"));
        log.info("appleAuthRequest : {}" , appleAuthRequest );

        String email = appleAuthRequest.getEmail();
        attributes.put("email", tokenClaims.get("email"));

        String name;

        String tokenEmail = tokenClaims.get("email").toString();
        // email에서 '@' 앞부분을 추출하여 name 설정
        if (tokenEmail != null && tokenEmail.contains("@")) {
            name = tokenEmail.substring(0, tokenEmail.indexOf("@"));
        } else {
            name = "Unknown"; // 이메일이 없을 경우 기본값 설정
        }
        attributes.put("name", name);  // 'name' 키로 이름 정보 전달 (후에 successHandler에서 사용)

        log.info(" 로그인: email={}, name={}", email, name);


        log.info("AppleOAuth2User 생성, attributes: {}", attributes);
        return new AppleOAuth2User(attributes);
    }

    /**
     * AppleAuthRequest 생성 (OAuth2UserRequest에서 값 추출)
     */
    private AppleAuthRequest extractAppleAuthRequest(OAuth2UserRequest userRequest) {
        Map<String, Object> additionalParams = userRequest.getAdditionalParameters();

        // id_token과 code가 반드시 존재하는지 확인하고, 없으면 예외 처리 또는 기본값 할당
        Object idTokenObj = additionalParams.get("id_token");
        log.info("idTokenObj: {}", idTokenObj);
        if (idTokenObj == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request", "id_token is missing", null));
        }
        String identityToken = idTokenObj.toString();
        log.info("identityToken: {}", identityToken);

        // 'user' 파라미터는 최초 로그인 시에만 존재할 수 있음
        Map<String, Object> user = additionalParams.containsKey("user") && additionalParams.get("user") != null
                ? (Map<String, Object>) additionalParams.get("user")
                : Map.of();

        return new AppleAuthRequest(identityToken, user);
    }

    /**
     * Apple ID Token 검증 및 사용자 계정 ID(sub) 추출
     */
    public Map<String, Object> getAppleAccountId(String identityToken)
            throws JsonProcessingException, AuthenticationException, NoSuchAlgorithmException,
            InvalidKeySpecException {
        Map<String, String> headers = identityTokenValidator.parseHeaders(identityToken);
        PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(headers,appleAuthClient.getAppleAuthPublicKey());
        log.info("applePublicKey: {}", publicKey);
        Map<String, Object> tokenClaims = identityTokenValidator.getTokenClaims(identityToken, publicKey);
        return tokenClaims;
    }
}