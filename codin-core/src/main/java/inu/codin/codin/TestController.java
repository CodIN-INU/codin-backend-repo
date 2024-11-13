package inu.codin.codin;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v3/api")
public class TestController {

    @Operation(summary = "기본 접근 테스트")
    @GetMapping("/test")
    public String test1() {
        return "test";
    }

    @Operation(summary = "기본 접근 테스트 - 로그인 데이터 확인")
    @GetMapping("/test2")
    public String test2() {
        // SecurityContextHolder를 이용하여 현재 사용자의 정보를 가져올 수 있습니다.
        String username = SecurityContextHolder // SecurityContextHolder를 이용하여 현재 사용자의 정보를 가져올 수 있습니다.
                .getContext()
                .getAuthentication()
                .getName();
        return "유저 이름: " + username;
    }

}
