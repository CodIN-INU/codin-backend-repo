package inu.codin.codin.domain.info.professor.controller;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.professor.service.ProfessorService;
import inu.codin.codin.domain.info.professor.dto.ProfessorListResDTO;
import inu.codin.codin.domain.info.professor.dto.ProfessorThumbnailResDTO;
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
@RequestMapping("/api/professor")
@Tag(name = "Professor Info API")
public class ProfessorController {

    private final ProfessorService professorService;

    @Operation(summary = "교수 목록 반환")
    @GetMapping("/{department}")
    public ResponseEntity<List<ProfessorListResDTO>> getProfessorList(@PathVariable("department") Department department){
        return ResponseEntity.ok()
                .body(professorService.getProfessorByDepartment(department));
    }

    @Operation(summary = "id값에 따른 교수 썸네일 반환")
    @GetMapping("/detail/{id}")
    public ResponseEntity<ProfessorThumbnailResDTO> getProfessorThumbnail(@PathVariable("id") String id){
        return ResponseEntity.ok()
                .body(professorService.getProfessorThumbnail(id));
    }
}
