package inu.codin.codin.domain.info.domain.office.controller;

import inu.codin.codin.common.ResponseUtils;
import inu.codin.codin.domain.info.domain.office.dto.OfficeMemberCreateUpdateRequestDto;
import inu.codin.codin.domain.info.domain.office.dto.OfficeUpdateRequestDto;
import inu.codin.codin.domain.info.domain.office.service.OfficeService;
import inu.codin.codin.domain.info.domain.office.dto.OfficeDetailsResponseDto;
import inu.codin.codin.common.Department;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/info/office")
@Tag(name = "Info API", description = "연구실, 사무실, 교수 / 정보 API")
public class OfficeController {

    private final OfficeService officeService;

    @Operation(summary = "학과별 사무실 직원 정보 반환")
    @GetMapping("/{department}")
    public ResponseEntity<OfficeDetailsResponseDto> getOfficeByDepartment(@PathVariable("department") Department department){
        return ResponseUtils.success(officeService.getOfficeByDepartment(department));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 학과사무실 정보 수정")
    @PatchMapping(value = "/{department}" , produces = "plain/text; charset=utf-8")
    public ResponseEntity<?> updateOffice(@PathVariable("department") Department department, @RequestBody @Valid OfficeUpdateRequestDto officeUpdateRequestDto){
        officeService.updateOffice(department, officeUpdateRequestDto);
        return ResponseUtils.successMsg("Office 정보 수정 완료");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 학과사무실 직원 추가")
    @PatchMapping(value = "/{department}/member", produces = "plain/text; charset=utf-8")
    public ResponseEntity<?> createOfficeMember(@PathVariable("department") Department department,
                                                @RequestBody @Valid OfficeMemberCreateUpdateRequestDto officeMemberCreateUpdateRequestDto){
        officeService.createOfficeMember(department, officeMemberCreateUpdateRequestDto);
        return ResponseUtils.successMsg("Office Member 추가 완료");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 학과사무실 직원 정보 수정")
    @PatchMapping(value = "/{department}/member/{num}", produces = "plain/text; charset=utf-8")
    public ResponseEntity<?> updateOfficeMember(@PathVariable("department") Department department, @PathVariable("num")  @Min(0) int num,
                                                @RequestBody @Valid OfficeMemberCreateUpdateRequestDto officeMemberCreateUpdateRequestDto){
        officeService.updateOfficeMember(department, num, officeMemberCreateUpdateRequestDto);
        return ResponseUtils.successMsg("Office Member 정보 수정 완료");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 학과사무실 직원 정보 삭제")
    @DeleteMapping(value = "/{department}/member/{num}", produces = "plain/text; charset=utf-8")
    public ResponseEntity<?> deleteOfficeMember(@PathVariable("department") Department department, @PathVariable("num") @Min(0) int num){
        officeService.deleteOfficeMember(department,num);
        return ResponseUtils.successMsg("Office Member 정보 삭제 완료");
    }
}
