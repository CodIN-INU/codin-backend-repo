package inu.codin.codin.global;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/v3/api", produces = "plain/text; charset=utf-8")
@Tag(name = "Test API", description = "[관리자] 유저 테스트용 API")
public class TestController {

    @Operation(summary = "1 기본 접근 테스트 - 로그인 없이 접근 가능")
    @GetMapping("/test1")
    public String test1() {
        return "Test Success";
    }

    @Operation(summary = "2 기본 접근 테스트 - 로그인 데이터 확인")
    @GetMapping("/test2")
    public String test2() {
        return getUserData();
    }

    @Operation(summary = "3 유저 권한 테스트1 - USER 권한")
    @GetMapping("/test3")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public String test3() {
        return "USER 권한 접근 : " + getUserData();
    }

    @Operation(summary = "4 유저 권한 테스트2 - ADMIN 권한")
    @GetMapping("/test4")
    @PreAuthorize("hasRole('ADMIN')")
    public String test4() {
        return "ADMIN 권한 접근 : " + getUserData();
    }

    @Operation(summary = "5 유저 권한 테스트3 - MANAGER 권한")
    @GetMapping("/test5")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String test5() {
        return "MANAGER 권한 접근 : " + getUserData();
    }

    private static String getUserData() {
        // todo : HttpBasic을 통한 인증 X
        // 로그인 정보 인식 추가 필요
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            log.info("[TEST] 로그인 정보 없음");
            return "로그인 정보 없음";
        }
        // SecurityContextHolder를 이용하여 현재 사용자의 정보를 가져올 수 있습니다.
        String username = SecurityContextHolder // SecurityContextHolder를 이용하여 현재 사용자의 정보를 가져올 수 있습니다.
                .getContext()
                .getAuthentication()
                .getName();
        String userRole = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .toString();
        log.info("[TEST] 유저 이름 : {}, Role : {}", username, userRole);
        return "유저 이름 : " + username + ", Role : " + userRole;
    }
}
