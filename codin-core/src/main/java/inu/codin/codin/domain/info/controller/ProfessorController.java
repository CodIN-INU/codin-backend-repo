package inu.codin.codin.domain.info.controller;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.info.dto.request.ProfessorCreateUpdateRequestDto;
import inu.codin.codin.domain.info.dto.response.ProfessorListResponseDto;
import inu.codin.codin.domain.info.dto.response.ProfessorThumbnailResponseDto;
import inu.codin.codin.domain.info.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/info/professor")
@Tag(name = "Professor API", description = "교수 정보 API")
public class ProfessorController {

    private final ProfessorService professorService;

    @Operation(summary = "교수 리스트 반환")
    @GetMapping("/{department}")
    public ResponseEntity<ListResponse<ProfessorListResponseDto>> getProfessorList(@PathVariable("department") Department department){
        return ResponseEntity.ok()
                .body(new ListResponse<>(200, "교수 리스트 반환 완료", professorService.getProfessorByDepartment(department)));
    }

    @Operation(summary = "id값에 따른 교수 썸네일 반환")
    @GetMapping("/detail/{id}")
    public ResponseEntity<SingleResponse<ProfessorThumbnailResponseDto>> getProfessorThumbnail(@PathVariable("id") String id){
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "교수 썸네일 반환 성공", professorService.getProfessorThumbnail(id)));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 새로운 교수 정보 생성")
    @PostMapping
    public ResponseEntity<SingleResponse<?>> createProfessor(@RequestBody @Valid ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto){
        professorService.createProfessor(professorCreateUpdateRequestDto);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "새로운 교수 정보 생성 완료", null));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 교수 정보 수정")
    @PatchMapping(value = "/{id}")
    public ResponseEntity<SingleResponse<?>> updateProfessor(@PathVariable("id") String id, @RequestBody @Valid ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto){
        professorService.updateProfessor(id, professorCreateUpdateRequestDto);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "교수 정보 수정 완료", null));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 교수 정보 삭제")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<SingleResponse<?>> deleteProfessor(@PathVariable("id") String id){
        professorService.deleteProfessor(id);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "교수 정보 삭제 완료", null));
    }


}