package inu.codin.codin.common.security.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
@Slf4j
public class AppleClientSecretGenerator {

    @Value("${apple.team-id}")
    private String teamId; // 예: 2KTK94265N

    @Value("${apple.key.id}")
    private String keyId; // 예: K74A3SVBD3

    @Value("${apple.aud}")
    private String audience; // 보통 "https://appleid.apple.com"

    @Value("${spring.security.oauth2.client.registration.apple.client-id}")
    private String clientId; // Apple 서비스 ID

    @Value("${apple.key.path}")
    private Resource keyResource; // 예: classpath:resources/key/AuthKey_K74A3SVBD3.p8

    /**
     * Apple client-secret (JWT)을 생성합니다.
     * Apple은 client-secret의 유효기간을 최대 6개월 미만으로 요구하므로, 이 예제에서는 약 6개월 유효기간으로 설정합니다.
     */
    public String generateAppleClientSecret() throws Exception {
        PrivateKey privateKey = getPrivateKeyFromP8();

        Instant now = Instant.now();
        // 약 6개월 유효 (Apple은 6개월 미만이어야 함)
        Instant expirationTime = now.plusSeconds(15777000L);

        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setIssuer(teamId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expirationTime))
                .setAudience(audience)
                .setSubject(clientId)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    /**
     * p8 파일을 읽어 PrivateKey 객체로 변환합니다.
     */
    private PrivateKey getPrivateKeyFromP8() throws Exception {
        try (InputStream is = keyResource.getInputStream()) {
            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8);
           // log.info("getPrivateKeyFromP8 :{}" ,key);
            key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("EC"); // Apple은 ES256(ECDSA with P-256) 알고리즘 사용
            return kf.generatePrivate(spec);
        }
    }
}


