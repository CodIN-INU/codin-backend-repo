package inu.codin.codin.common.security.jwt;

import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.infra.redis.RedisStorageService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰 생성 및 유효성 검사
 * - 토큰 생성
 * - 토큰 유효성 검사
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${spring.jwt.secret}")
    private String secret;
    @Value("${spring.jwt.expiration.access}")
    private String ACCESS_TOKEN_EXPIRATION;
    @Value("${spring.jwt.expiration.refresh}")
    private String REFRESH_TOKEN_EXPIRATION;

    /**
     * bytes[], String 키는 deprecated 되었기 때문에 Key 타입으로 변경
     */
    private Key SECRET_KEY;
    private final RedisStorageService redisStorageService;

    /**
     * 양방향 대칭키 방식인 HS512로 사용
     */
    @PostConstruct
    protected void init() {
        SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public TokenDto createToken(Authentication authentication) {
        // 권한을 authorities에 담아서 String으로 변환
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce((auth1, auth2) -> auth1 + "," + auth2)
                .orElse("");

        // 토큰 만료시간 설정
        Date now = new Date();
        Date accessTokenExpiration = new Date(now.getTime() + Long.parseLong(this.ACCESS_TOKEN_EXPIRATION) * 1000);
        Date refreshTokenExpiration = new Date(now.getTime() + Long.parseLong(this.REFRESH_TOKEN_EXPIRATION) * 1000);

        // 토큰 생성
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setIssuedAt(now)
                .setExpiration(accessTokenExpiration)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setIssuedAt(now)
                .setExpiration(refreshTokenExpiration)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        // Redis에 기존에 저장된 RefreshToken 삭제
        String beforeRefreshToken = redisStorageService.getStoredRefreshToken(authentication.getName());
        if (beforeRefreshToken != null) {
            redisStorageService.deleteRefreshToken(authentication.getName());
        }

        // Redis에 RefreshToken 저장
        redisStorageService.saveRefreshToken(
                authentication.getName(),
                refreshToken,
                Long.parseLong(this.REFRESH_TOKEN_EXPIRATION)
        );


        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 토큰 유효성 검사 (토큰 변조, 만료)
     * @param accessToken
     * @return true: 유효한 토큰, false: 유효하지 않은 토큰
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
            throw new JwtException(SecurityErrorCode.EXPIRED_TOKEN, "access_token");
        } catch (Exception e) { // 토큰 변조
            log.error("[validateAccessToken] 유효하지 않은 토큰 : {}", e.getMessage());
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "access_token");
        }
    }

    /**
     * Refresh Token 유효성 검사 : Redis에 저장된 Refresh Token과 비교
     * @param refreshToken
     * @return true: 유효한 토큰, false: 유효하지 않은 토큰
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            // Redis에 저장된 Refresh Token과 비교
            String storedRefreshToken = redisStorageService.getStoredRefreshToken(getClaims(refreshToken).getSubject());
            if (storedRefreshToken == null) {
                log.warn("[validateRefreshToken] 저장된 Refresh Token이 없음");
                return false;
            }
            if (!refreshToken.equals(storedRefreshToken)) {
                log.warn("[validateRefreshToken] 저장된 Refresh Token과 요청된 Refresh Token이 일치하지 않음");
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) { // 토큰 만료
            log.error("[validateRefreshToken] 토큰 만료 : {}", e.getMessage());
            throw new JwtException(SecurityErrorCode.EXPIRED_TOKEN, "refresh_token");
        } catch (Exception e) { // 토큰 변조
            log.error("[validateRefreshToken] 유효하지 않은 토큰 : {}", e.getMessage());
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "refresh_token");
        }
    }

    /** 토큰에서 username 추출 */
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TokenDto {

        private String accessToken;
        private String refreshToken;

        @Builder
        public TokenDto(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
