package inu.codin.codin.domain.info.office.controller;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.office.service.OfficeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/office")
public class OfficeController {

    private final OfficeService officeService;

    @Operation(summary = "학과별 사무실 직원 정보 반환")
    @GetMapping("/{department}")
    public ResponseEntity<?> getOfficeByDepartment(@PathVariable("department") Department department){
        return ResponseEntity.ok()
                .body(officeService.getOfficeByDepartment(department));
    }

    @Operation(summary = "학과사무실 리스트 반환")
    @GetMapping
    public ResponseEntity<?> getAllOffice(){
        return ResponseEntity.ok()
                .body(officeService.getAllOffice());
    }
}
