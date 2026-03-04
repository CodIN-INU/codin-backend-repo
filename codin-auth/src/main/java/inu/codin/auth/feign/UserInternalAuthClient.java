package inu.codin.auth.feign;

import inu.codin.auth.config.FeignMultipartConfig;
import inu.codin.auth.dto.user.*;
import inu.codin.common.entity.College;
import inu.codin.common.entity.Department;
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
            @RequestParam String email,
            @RequestParam String nickname,
            @RequestParam String name,
            @RequestParam College college,
            @RequestParam Department department,
            @RequestPart(value = "userImage", required = false) MultipartFile userImage
    );

    @GetMapping("/suspension-end-date")
    LocalDateTime getSuspensionEndDate(@RequestParam("email") String email);

    @GetMapping("/adminLogin-material")
    AdminLoginMaterial getLoginMaterial(@RequestParam("email") String email);

    @PostMapping("/oauth/decision")
    UserOAuthDecision oauthDecision(@RequestBody UserOAuthDecisionRequest request);

    //  토큰 재발급을 위한 사용자 정보 조회
    @GetMapping("/user-info")
    UserTokenInfo getUserTokenInfo(@RequestParam("email") String email);

}
