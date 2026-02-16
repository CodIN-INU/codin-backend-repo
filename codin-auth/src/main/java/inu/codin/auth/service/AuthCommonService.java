package inu.codin.auth.service;

import inu.codin.auth.dto.user.*;
import inu.codin.auth.exception.AuthException;
import inu.codin.auth.feign.UserInternalAuthClient;
import inu.codin.auth.jwt.JwtTokenIssuer;
import inu.codin.auth.dto.SignUpAndLoginRequestDto;
import inu.codin.auth.dto.user.UserProfileRequestDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import inu.codin.auth.service.oauth2.AbstractAuthService;
import java.time.LocalDateTime;
import static inu.codin.auth.exception.AuthErrorCode.INVALID_CREDENTIALS;

@Service
@Slf4j
public class AuthCommonService extends AbstractAuthService {

    private final PasswordEncoder passwordEncoder;

    public AuthCommonService(JwtTokenIssuer jwtTokenIssuer, UserInternalAuthClient userInternalAuthClient, PasswordEncoder passwordEncoder) {
        super(jwtTokenIssuer, userInternalAuthClient);
        this.passwordEncoder = passwordEncoder;
    }

    public void completeUserProfile(UserProfileRequestDto userProfileRequestDto, MultipartFile userImage, HttpServletResponse response) {
        // User 에게 책임 분리 (auth -> user API 호출)
        CompleteProfileResponse completeProfileResponse = callUserCompleteProfile(userProfileRequestDto, userImage);
        
        issueJwtToken(completeProfileResponse.toTokenDecision(), response);
    }

    public LocalDateTime getSuspensionEndDate(OAuth2User oAuth2User){
        String email = (String) oAuth2User.getAttribute("email");
        return userInternalAuthClient.getSuspensionEndDate(email);
    }

    public void login(SignUpAndLoginRequestDto signUpAndLoginRequestDto, HttpServletResponse response) {
        AdminLoginMaterial adminLoginMaterial = userInternalAuthClient.getLoginMaterial(signUpAndLoginRequestDto.getEmail());

        if (passwordEncoder.matches(signUpAndLoginRequestDto.getPassword(), adminLoginMaterial.encodedPassword())) {
            issueJwtToken(adminLoginMaterial.toTokenDecision(), response);
        } else {
            throw new AuthException(INVALID_CREDENTIALS);
        }
    }

    private CompleteProfileResponse callUserCompleteProfile(UserProfileRequestDto userProfileRequestDto, MultipartFile userImage) {
        return userInternalAuthClient.completeProfile(userProfileRequestDto.getEmail(),userProfileRequestDto.getNickname(), userImage);
    }

}
