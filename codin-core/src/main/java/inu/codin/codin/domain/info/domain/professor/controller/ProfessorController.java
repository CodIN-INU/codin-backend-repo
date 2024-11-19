package inu.codin.codin.domain.info.domain.professor.controller;

import inu.codin.codin.common.ResponseUtils;
import inu.codin.codin.domain.info.domain.professor.dto.ProfessorListResponseDto;
import inu.codin.codin.domain.info.domain.professor.dto.ProfessorThumbnailResponseDto;
import inu.codin.codin.domain.info.domain.professor.dto.ProfessorCreateUpdateRequestDto;
import inu.codin.codin.domain.info.domain.professor.service.ProfessorService;
import inu.codin.codin.common.Department;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/info/professor")
@Tag(name = "Info API")
public class ProfessorController {

    private final ProfessorService professorService;

    @Operation(summary = "교수 리스트 반환")
    @GetMapping("/{department}")
    public ResponseEntity<List<ProfessorListResponseDto>> getProfessorList(@PathVariable("department") Department department){
        return ResponseUtils.success(professorService.getProfessorByDepartment(department));
    }

    @Operation(summary = "id값에 따른 교수 썸네일 반환")
    @GetMapping("/detail/{id}")
    public ResponseEntity<ProfessorThumbnailResponseDto> getProfessorThumbnail(@PathVariable("id") String id){
        return ResponseUtils.success(professorService.getProfessorThumbnail(id));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 새로운 교수 정보 생성")
    @PostMapping(produces = "plain/text; charset=utf-8")
    public ResponseEntity<?> createProfessor(@RequestBody ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto){
        professorService.createProfessor(professorCreateUpdateRequestDto);
        return ResponseUtils.successMsg("새로운 교수 정보 생성 완료");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 교수 정보 수정")
    @PatchMapping(value = "/{id}", produces = "plain/text; charset=utf-8")
    public ResponseEntity<?> updateProfessor(@PathVariable("id") String id, @RequestBody ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto){
        professorService.updateProfessor(id, professorCreateUpdateRequestDto);
        return ResponseUtils.successMsg("교수 정보 수정 완료");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "[ADMIN, MANAGER] 교수 정보 삭제")
    @DeleteMapping(value = "/{id}", produces = "plain/text; charset=utf-8")
    public ResponseEntity<?> deleteProfessor(@PathVariable("id") String id){
        professorService.deleteProfessor(id);
        return ResponseUtils.successMsg("교수 정보 삭제 완료");
    }


}
