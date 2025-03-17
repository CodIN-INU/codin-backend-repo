package inu.codin.codin.common.security.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IdentityTokenValidator {

    /**
     * apple Login
     *     identity token 에서 Header 추출
     */
    public Map<String, String> parseHeaders(String token) throws JsonProcessingException {
        String header = token.split("\\.")[0];
        return new ObjectMapper().readValue(decodeHeader(header), Map.class);
    }

    public String decodeHeader(String token) {
        return new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
    }

    public Claims getTokenClaims(String token, PublicKey publicKey) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
