package inu.codin.codin.domain.info.office.controller;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.office.service.OfficeService;
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

    @GetMapping("/{department}")
    public ResponseEntity<?> getOfficeByDepartment(@PathVariable("department") Department department){
        return ResponseEntity.ok()
                .body(officeService.getOfficeByDepartment(department));
    }


    @GetMapping
    public ResponseEntity<?> getAllOffice(){
        return ResponseEntity.ok()
                .body(officeService.getAllOffice());
    }
}
