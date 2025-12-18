package inu.codin.auth.feign;

import inu.codin.auth.config.FeignMultipartConfig;
import inu.codin.auth.dto.user.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;


@FeignClient(
        name = "user-internal-client",
        url = "${feign.user-service.url:http://localhost:8080}",
        configuration = FeignMultipartConfig.class,
        path = "/internal/auth"
)
public interface UserInternalAuthClient {

    @PostMapping(value = "/complete-profile", consumes = "multipart/form-data")
    CompleteProfileResponse completeProfile(
            @RequestPart("completeProfileRequest") CompleteProfileRequest completeProfileRequest,
            @RequestPart(value = "image", required = false) MultipartFile image
    );

    @GetMapping("/suspension-end-date")
    LocalDateTime getSuspensionEndDate(@RequestParam("email") String email);

    @GetMapping("/adminLogin-material")
    AdminLoginMaterial getLoginMaterial(@RequestParam("email") String email);

    @PostMapping("/internal/auth/oauth/decision")
    UserOAuthDecision oauthDecision(@RequestBody UserOAuthDecisionRequest request);

}
