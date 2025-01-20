package inu.codin.codin.domain.user.feign;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.user.dto.request.UserSignUpRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PortalController {

    private final PortalService portalService;

    @Operation(
            summary = "포탈 로그인 / 회원가입",
            description = "포탈 아이디, 비밀번호를 통해 회원가입 진행"
    )
    @PostMapping("/portal")
    public ResponseEntity<SingleResponse<?>> portalSignUp(@RequestBody @Valid UserSignUpRequestDto userSignUpRequestDto) throws Exception {
        portalService.signUp(userSignUpRequestDto);
        return null;

    }
}
