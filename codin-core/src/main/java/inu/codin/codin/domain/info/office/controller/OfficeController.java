package inu.codin.codin.domain.info.office.controller;

import inu.codin.codin.common.ResponseUtils;
import inu.codin.codin.domain.info.office.dto.OfficeListResDTO;
import inu.codin.codin.domain.info.office.dto.OfficeMemberResDTO;
import inu.codin.codin.domain.info.office.service.OfficeService;
import inu.codin.codin.common.Department;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/info/office")
@Tag(name = "Info API")
public class OfficeController {

    private final OfficeService officeService;

    @Operation(summary = "학과별 사무실 직원 정보 반환")
    @GetMapping("/{department}")
    public ResponseEntity<List<OfficeMemberResDTO>> getOfficeByDepartment(@PathVariable("department") Department department){
        return ResponseUtils.success(officeService.getOfficeByDepartment(department));
    }

    @Operation(summary = "학과사무실 리스트 반환")
    @GetMapping
    public ResponseEntity<List<OfficeListResDTO>> getAllOffice(){
        return ResponseUtils.success(officeService.getAllOffice());
    }
}
