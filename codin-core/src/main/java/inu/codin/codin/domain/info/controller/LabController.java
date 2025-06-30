package inu.codin.codin.domain.info.controller;

import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.info.dto.request.LabCreateUpdateRequestDto;
import inu.codin.codin.domain.info.dto.response.LabListResponseDto;
import inu.codin.codin.domain.info.dto.response.LabThumbnailResponseDto;
import inu.codin.codin.domain.info.service.LabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/info/lab")
@Tag(name = "Lab API", description = "연구실 정보 API")
public class LabController {

    private final LabService labService;

    @Operation(summary = "연구실 썸네일 반환")
    @GetMapping("/thumbnail/{id}")
    public ResponseEntity<SingleResponse<LabThumbnailResponseDto>> getLabThumbnail(@PathVariable("id") String id){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "연구실 썸네일 반환 성공", labService.getLabThumbnail(id)));
    }

    @Operation(summary = "연구실 리스트 반환")
    @GetMapping
    public ResponseEntity<ListResponse<LabListResponseDto>> getAllLab(){
        return ResponseEntity.ok()
                .body(new ListResponse<>(200, "연구실 리스트 반환 성공", labService.getAllLab()));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 새로운 연구실 등록")
    @PostMapping
    public ResponseEntity<SingleResponse<?>> createLab(@RequestBody @Valid LabCreateUpdateRequestDto labCreateUpdateRequestDto){
        labService.createLab(labCreateUpdateRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "새로운 LAB 등록 완료", null));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 연구실 정보 수정")
    @PutMapping(value = "/{id}")
    public ResponseEntity<SingleResponse<?>> updateLab(@RequestBody @Valid LabCreateUpdateRequestDto labCreateUpdateRequestDto, @PathVariable("id") String id){
        labService.updateLab(labCreateUpdateRequestDto, id);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "LAB 정보 수정 완료", null));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 연구실 삭제")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<SingleResponse<?>> deleteLab(@PathVariable("id") String id){
        labService.deleteLab(id);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "LAB 삭제 완료", null));
    }

}