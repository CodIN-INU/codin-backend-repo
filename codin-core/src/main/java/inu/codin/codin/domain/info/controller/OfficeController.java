package inu.codin.codin.domain.info.controller;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.info.dto.request.OfficeMemberCreateUpdateRequestDto;
import inu.codin.codin.domain.info.dto.request.OfficeUpdateRequestDto;
import inu.codin.codin.domain.info.service.OfficeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/info/office")
@Tag(name = "Office API", description = "학과 사무실 정보 API")
public class OfficeController {

    private final OfficeService officeService;

    @Operation(summary = "학과별 사무실 정보 반환")
    @GetMapping("/{department}")
    public ResponseEntity<SingleResponse<?>> getOfficeByDepartment(@PathVariable("department") Department department){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "학과별 사무실 정보 반환 성공", officeService.getOfficeByDepartment(department)));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 학과사무실 정보 수정")
    @PatchMapping(value = "/{department}")
    public ResponseEntity<SingleResponse<?>> updateOffice(@PathVariable("department") Department department, @RequestBody @Valid OfficeUpdateRequestDto officeUpdateRequestDto){
        officeService.updateOffice(department, officeUpdateRequestDto);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "Office 정보 수정 완료", null));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 학과사무실 직원 추가")
    @PatchMapping(value = "/{department}/member")
    public ResponseEntity<SingleResponse<?>> createOfficeMember(@PathVariable("department") Department department,
                                                                @RequestBody @Valid OfficeMemberCreateUpdateRequestDto officeMemberCreateUpdateRequestDto){
        officeService.createOfficeMember(department, officeMemberCreateUpdateRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "Office Member 추가 완료", null));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 학과사무실 직원 정보 수정")
    @PatchMapping(value = "/{department}/member/{num}")
    public ResponseEntity<SingleResponse<?>> updateOfficeMember(@PathVariable("department") Department department, @PathVariable("num")  @Min(0) int num,
                                                                @RequestBody @Valid OfficeMemberCreateUpdateRequestDto officeMemberCreateUpdateRequestDto){
        officeService.updateOfficeMember(department, num, officeMemberCreateUpdateRequestDto);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "Office Member 정보 수정 완료", null));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 학과사무실 직원 정보 삭제")
    @DeleteMapping(value = "/{department}/member/{num}")
    public ResponseEntity<?> deleteOfficeMember(@PathVariable("department") Department department, @PathVariable("num") @Min(0) int num){
        officeService.deleteOfficeMember(department,num);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "Office Member 정보 삭제 완료", null));
    }
}