package inu.codin.codin.domain.user.internal.inbound;

import inu.codin.codin.domain.user.internal.inbound.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController {

    private final UserOauthLoginDecisionService userOauthLoginDecisionService;
    private final UserInternalService userInternalService;

    @PostMapping("/oauth/decision")
    public OauthDecisionResponse oauthDecision(@RequestBody OauthDecisionRequest request) {
        return userOauthLoginDecisionService.decideOauthAction(request);
    }

    @PostMapping(value="/complete-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompleteProfileResponse completeProfile(
            @RequestParam String email,
            @RequestParam String nickname,
            @RequestPart(value = "userImage", required = false) MultipartFile userImage
    ) {
        return userInternalService.completeProfile(email, nickname, userImage);
    }

    @GetMapping("/suspension-end-date")
    public LocalDateTime getSuspensionEndDate(@RequestParam("email") String email) {
        return userInternalService.getSuspensionEndDate(email);
    }

    @GetMapping("/adminLogin-material")
    public AdminLoginMaterialResponse getAdminLoginMaterial(@RequestParam("email") String email) {
        return userInternalService.getAdminLoginMaterial(email);
    }

    @GetMapping("/user-info")
    public UserTokenInfoResponse getUserInfo(@RequestParam("email") String email) {
        return userInternalService.getUserTokenInfo(email);
    }
}