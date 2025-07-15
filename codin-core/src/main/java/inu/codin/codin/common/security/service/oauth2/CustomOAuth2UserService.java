package inu.codin.codin.common.security.service.oauth2;


import inu.codin.codin.common.security.dto.AccessEmailProperties;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AccessEmailProperties accessEmailProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final String OAUTH2_ACCESS_TOKEN = "oauth2_access_token";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 요청에서 HttpSession 가져오기
        HttpSession httpSession = getSession();

        // Get User Email
        String email = oAuth2User.getAttribute("email");
        log.info("email: {}", email);

        if (email == null) {
            httpSession.setAttribute(OAUTH2_ACCESS_TOKEN, userRequest.getAccessToken().getTokenValue());
            OAuth2Error oauth2Error = new OAuth2Error(
                    "email_not_found",
                    "이메일 정보를 찾을 수 없습니다.",
                    null
            );
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.getDescription());
        }

        if (accessEmailProperties.getDomain().stream().anyMatch(url -> pathMatcher.match(url, email))) {
            log.info("접근 허용 email : {}", email);
            return oAuth2User;
        }

        // Only Allow @inu.ac.kr
        if (!email.trim().endsWith("@inu.ac.kr")) {
            httpSession.setAttribute(OAUTH2_ACCESS_TOKEN, userRequest.getAccessToken().getTokenValue());
            OAuth2Error oauth2Error = new OAuth2Error(
                    "invalid_email_domain",
                    "허용되지 않은 이메일 도메인입니다. @inu.ac.kr 이어야합니다",
                    null
            );
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.getDescription());
        }

        return oAuth2User;
    }

    public HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) {
            throw new IllegalStateException("No thread-bound request found. RequestContextListener 또는 RequestContextFilter를 설정하세요.");
        }
        return attr.getRequest().getSession();
    }
}
