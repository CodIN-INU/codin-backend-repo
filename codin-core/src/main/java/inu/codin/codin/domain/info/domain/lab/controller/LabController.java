package inu.codin.codin.domain.info.domain.lab.controller;

import inu.codin.codin.common.ResponseUtils;
import inu.codin.codin.domain.info.domain.lab.dto.LabCreateUpdateRequestDto;
import inu.codin.codin.domain.info.domain.lab.dto.LabListResponseDto;
import inu.codin.codin.domain.info.domain.lab.dto.LabThumbnailResponseDto;
import inu.codin.codin.domain.info.domain.lab.service.LabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/info/lab")
@Tag(name = "Info API")
public class LabController {

    private final LabService labService;

    @Operation(summary = "연구실 썸네일 반환")
    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<LabThumbnailResponseDto> getLabThumbnail(@PathVariable("id") String id){
        return ResponseUtils.success(labService.getLabThumbnail(id));
    }

    @Operation(summary = "연구실 리스트 반환")
    @GetMapping
    public ResponseEntity<List<LabListResponseDto>> getAllLab(){
        return ResponseUtils.success(labService.getAllLab());
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 새로운 연구실 등록")
    @PostMapping(produces = "plain/text; charset=utf-8")
    public ResponseEntity<?> createLab(@RequestBody LabCreateUpdateRequestDto labCreateUpdateRequestDto){
        labService.createLab(labCreateUpdateRequestDto);
        return ResponseUtils.successMsg("새로운 LAB 등록 완료");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 연구실 정보 수정")
    @PutMapping(value = "/{id}", produces = "plain/text; charset=utf-8")
    public ResponseEntity<?> updateLab(@RequestBody LabCreateUpdateRequestDto labCreateUpdateRequestDto, @PathVariable("id") String id){
        labService.updateLab(labCreateUpdateRequestDto, id);
        return ResponseUtils.successMsg("LAB 정보 수정 완료");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 연구실 삭제")
    @DeleteMapping(value = "/{id}", produces = "plain/text; charset=utf-8")
    public ResponseEntity<?> deleteLab(@PathVariable("id") String id){
        labService.deleteLab(id);
        return ResponseUtils.successMsg("LAB 삭제 완료");
    }

}
