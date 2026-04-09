package inu.codin.codin.domain.admin.controller;

import inu.codin.codin.domain.admin.dto.req.SetManagerUserInfo;
import inu.codin.codin.domain.admin.service.AdminService;
import inu.codin.common.response.SingleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/admins")
@Tag(name = "User Auth API", description = "유저 회원가입, 로그인, 로그아웃, 리이슈 API")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            summary = "MANAGER 계정 생성"
    )
    @PostMapping("/manager/signup")
    public ResponseEntity<SingleResponse<?>> addManager(@RequestBody @Valid SetManagerUserInfo setManagerUserInfo) {
        adminService.addManager(setManagerUserInfo);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "MANAGER 계정 생성 성공", "MANAGER 유저 생성 완료"));
    }
}
