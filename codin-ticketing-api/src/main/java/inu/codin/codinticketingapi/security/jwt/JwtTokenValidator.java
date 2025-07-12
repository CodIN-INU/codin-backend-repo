package inu.codin.codinticketingapi.security.jwt;

import inu.codin.codinticketingapi.security.exception.SecurityErrorCode;
import inu.codin.codinticketingapi.security.exception.SecurityException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@Slf4j
public class JwtTokenValidator {

    @Value("${spring.jwt.secret}")
    private String secret;

    private Key SECRET_KEY;

    @PostConstruct
    protected void init() {
        log.info("[JwtTokenValidator] Set JWT Secret : {}", secret);
        SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 토큰 유효성 검사 (토큰 변조, 만료)
     * @param accessToken
     * @return true: 유효한 토큰
     * @throws SecurityException: 토큰 만료, 유효하지 않은 토큰
     */
    public boolean validateAccessToken(String accessToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(accessToken);
            return true;
        } catch (ExpiredJwtException e) { // 토큰 만료
            log.error("[validateAccessToken] 토큰 만료 : {}", e.getMessage());
            throw new SecurityException(SecurityErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) { // 토큰 변조
            log.error("[validateAccessToken] 유효하지 않은 토큰 : {}", e.getMessage());
            throw new SecurityException(SecurityErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰에서 사용자 권한 추출
     */
    public String getUserRole(String token) {
        return getClaims(token).get("auth", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}