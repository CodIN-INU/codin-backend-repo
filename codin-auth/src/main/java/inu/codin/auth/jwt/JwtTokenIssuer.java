package inu.codin.auth.jwt;


import inu.codin.auth.infra.RedisStorageService;
import inu.codin.auth.feign.UserInternalAuthClient;
import inu.codin.auth.dto.user.UserTokenInfo;
import inu.codin.security.exception.JwtException;
import inu.codin.security.exception.SecurityErrorCode;
import inu.codin.security.jwt.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * JWT 토큰 관련 비즈니스 로직을 처리하는 서비스
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JwtTokenIssuer {

    private final RedisStorageService redisStorageService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtils jwtUtils;
    private final UserInternalAuthClient userInternalAuthClient;

    @Value("${server.domain}")
    private String BASEURL;

    private final String REFRESH_TOKEN = "x-refresh-token";
    private final String ACCESS_TOKEN = "Authorization";
    private final String ACCESS_TOKEN_PREFIX = "Bearer ";

    /**
     * OAuth 로그인 성공 시: 이미 받은 사용자 정보로 토큰 발급
     * Feign 통신 없이 바로 토큰 생성 (성능 최적화)
     */
    public void createTokenForOAuth(String email, String userId, String authorities, HttpServletResponse response) {
        createBothToken(email, userId, authorities, response);
        log.info("[createTokenForOAuth] OAuth 로그인 토큰 발급 완료: email={}", email);
    }

    /**
     * Refresh Token 재발급 시: Feign 통신을 통해 사용자 정보 조회 후 토큰 발급
     * Phase 2/3: SecurityContext 의존성 제거 완료 (파라미터화)
     */
    public void createTokenForRefresh(String email, HttpServletResponse response) {
        // Feign 통신을 통해 사용자 정보 조회
        UserTokenInfo userInfo = getUserTokenInfo(email);
        
        createBothToken(userInfo.email(), userInfo.userId(), userInfo.authorities(), response);
        log.info("[createTokenForRefresh] 재발급 토큰 발급 완료: email={}", email);
    }

    /**
     * Refresh Token 기반 재발급
     * userDetails 로딩 없음
     * SecurityContext 세팅 없음
     * refreshToken에서 subject를 뽑아서 토큰 재발급
     * @param request
     * @param response
     */
    public void checkRefreshTokenAndReissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshToken(request);
        if (refreshToken == null) {
            log.error("[reissueToken] Refresh Token이 없습니다.");
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "Refresh Token이 없습니다.");
        }

        String username = jwtTokenProvider.getRefreshTokenUsername(refreshToken);
        // TODO: Auth/Resource 분리 - Access Token 검증 단계 제거 (Refresh Token 검증만으로 충분)
        // validateRefreshTokenWithAccessToken(request, username);

        // userDetails 로딩/인증 세팅 제거 바로 재발급만 수행

        reissueToken(refreshToken, response);
    }

    // TODO: Auth/Resource 분리 - Auth 서버가 Access Token 검증을 수행하는 것은 책임 경계 위반
    // TODO: Refresh Token 검증만으로 충분함, Access Token 추가 검증은 중복 방어
    /*
    //만료된 accessToken과 username을 비교
    private void validateRefreshTokenWithAccessToken(HttpServletRequest request, String username) {
        String accessToken = getAccessToken(request);
        String accessUsername;
        try {
            // codin-security의 JwtTokenValidator 사용
            accessUsername = jwtTokenValidator.getUsername(accessToken);
        } catch (ExpiredJwtException e) {
            Claims expiredClaims = e.getClaims();
            accessUsername = expiredClaims.getSubject();
        } catch (Exception e) {
            log.error("[validateRefreshTokenWithAccessToken] Access Token에서 사용자명 추출 실패: {}", e.getMessage());
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "Access Token이 유효하지 않습니다.");
        }
        
        if (!accessUsername.equals(username)) {
            log.error("[reissueToken] Access Token의 username과 Refresh Token가 일치하지 않습니다.");
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "Access Token의 username과 Refresh Token가 일치하지 않습니다.");
        }
    }
    */

    /**
     * Refresh Token을 이용하여 Access Token, Refresh Token 재발급
     * @param refreshToken
     * @param response
     */
    public void reissueToken(String refreshToken, HttpServletResponse response) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.error("[reissueToken] Refresh Token이 유효하지 않습니다. : {}", refreshToken);
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "Refresh Token이 유효하지 않습니다.");
        }

        // 기존 구조에 맞춰 수정: Refresh Token에서 email만 추출, 나머지는 Feign 통신
        String email = jwtTokenProvider.getRefreshTokenUsername(refreshToken);
        
        // 재발급 전용 메서드 사용 (Feign 통신 포함)
        createTokenForRefresh(email, response);
        log.info("[reissueToken] Access Token, Refresh Token 재발급 완료");
    }

    /**
     * Access Token, Refresh Token 생성
     * Phase 2: SecurityContext 의존성 제거됨
     */
    private void createBothToken(String email, String userId, String authorities, HttpServletResponse response) {
        // Phase 2: 파라미터로 필요 정보 전달하는 방식으로 변경됨
        JwtTokenProvider.TokenDto newToken = jwtTokenProvider.createToken(email, userId, authorities);

    // TODO: Phase 2 - 기존 SecurityContext 의존 방식 (제거 예정)
    /*
    private void createBothToken(HttpServletResponse response) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtTokenProvider.TokenDto newToken = jwtTokenProvider.createToken(authentication);
    */

        // Authorization 헤더에 Access Token 추가
        response.setHeader(ACCESS_TOKEN, ACCESS_TOKEN_PREFIX + newToken.getAccessToken());

        String domain = BASEURL.replaceFirst("https?://", "").split(":")[0];

        // todo: x-access-token 쿠키에 Access Token 추가 - 추후 제거
        Cookie newAccessToken = new Cookie("x-access-token", newToken.getAccessToken());
        newAccessToken.setHttpOnly(true);
        newAccessToken.setSecure(true);
        newAccessToken.setPath("/");
        newAccessToken.setMaxAge(10 * 24 * 60 * 60); // 10일
        newAccessToken.setDomain(domain);
        newAccessToken.setAttribute("SameSite", "None");
        response.addCookie(newAccessToken);

        // x-rfresh-token 쿠키에 Refresh Token 추가
        Cookie newRefreshToken = new Cookie(REFRESH_TOKEN, newToken.getRefreshToken());
        newRefreshToken.setHttpOnly(true);
        newRefreshToken.setSecure(true);
        newRefreshToken.setPath("/");
        newRefreshToken.setMaxAge(10 * 24 * 60 * 60); // 10일
        newRefreshToken.setDomain(domain);
        newRefreshToken.setAttribute("SameSite", "None");
        response.addCookie(newRefreshToken);

        log.info("[createBothToken] Access Token, Refresh Token 발급 완료, email = {}, Access: {}", email, newToken.getAccessToken());
    }


    /**
     * 로그아웃 -
     * Access,Refresh Token 제거/ 서버측 RT 삭제
     */
    public void deleteToken(HttpServletResponse response) {
        // TODO: Phase 2 - 로그아웃도 SecurityContext 의존성 제거 고려
        // 어차피 JwtAuthenticationFilter 단에서 토큰을 검증하여 인증을 처리하므로
        // SecurityContext에 Authentication 객체가 없는 경우는 없다.
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName()!=null){
            redisStorageService.deleteRefreshToken(authentication.getName());
            deleteCookie(response);
            log.info("[deleteToken] Refresh Token 삭제 완료");
        }
    }

    private void deleteCookie(HttpServletResponse response) {

        String domain = BASEURL.replaceFirst("https?://", "").split(":")[0];
        log.info("[deleteCookie] BASEURL={}, domain={}", BASEURL, domain);

        Cookie refreshCookie = new Cookie(REFRESH_TOKEN, "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setDomain(domain);
        refreshCookie.setMaxAge(0);
        refreshCookie.setAttribute("SameSite","None");
        response.addCookie(refreshCookie);

        log.info("[deleteCookie] refreshCookie info => name={}, domain={}, path={}, secure={}, httpOnly={}, maxAge={}, sameSite=None",
                refreshCookie.getName(), refreshCookie.getDomain(), refreshCookie.getPath(),
                refreshCookie.getSecure(), refreshCookie.isHttpOnly(), refreshCookie.getMaxAge());

        Cookie accessCookie = new Cookie("x-access-token", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setDomain(domain);
        accessCookie.setMaxAge(0);
        refreshCookie.setAttribute("SameSite","None");
        response.addCookie(accessCookie);

        log.info("[deleteCookie] accessCookie info => name={}, domain={}, path={}, secure={}, httpOnly={}, maxAge={}, sameSite=None",
                accessCookie.getName(), accessCookie.getDomain(), accessCookie.getPath(),
                accessCookie.getSecure(), accessCookie.isHttpOnly(), accessCookie.getMaxAge());

        log.info("[deleteToken] Access/Refresh Cookie 삭제 완료");
    }

    // 제거됨: setAuthentication(), getUserDetailsAndSetAuthentication(), getAccessToken()
    // 이 기능들은 Resource Server(codin-security)의 역할
    // Authorization Server는 토큰 발급/갱신/무효화만 담당
    

    private String getAccessToken(HttpServletRequest request) {
        return jwtUtils.getAccessToken(request);
    }

    public String getRefreshToken(HttpServletRequest request) {
        String refreshToken = jwtUtils.getRefreshToken(request);
        if (!jwtTokenProvider.validType(refreshToken, "refresh")) {
            log.error("[getRefreshToken] Refresh Token이 아닙니다.");
            throw new JwtException(SecurityErrorCode.INVALID_TYPE, "Refresh Token이 아닙니다.");
        }
        return refreshToken;
    }

    /**
     * Phase 3: Feign 통신을 통한 사용자 정보 조회
     * Auth 서버의 도메인 의존성 완전 분리
     */
    private UserTokenInfo getUserTokenInfo(String email) {
        try {
            return userInternalAuthClient.getUserTokenInfo(email);
        } catch (Exception e) {
            log.error("[getUserTokenInfo] 사용자 정보 조회 실패: email={}, error={}", email, e.getMessage());
            throw new JwtException(SecurityErrorCode.INVALID_TOKEN, "사용자 정보를 찾을 수 없습니다.");
        }
    }
}
