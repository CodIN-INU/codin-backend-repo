package inu.codin.codin.domain.info.domain.professor.dto;

import inu.codin.codin.domain.info.domain.professor.entity.Professor;
import inu.codin.codin.common.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/*
    교수님 리스트 반환 DTO
    모든 교수님들의 리스트를 반환한다.
 */

public record ProfessorListResponseDto(
        @NotBlank @Schema(description = "교수 _id", example = "67319fe3c4ee25b3adf593a0")
        String id,

        @NotBlank @Schema(description = "교수 성함", example = "홍길동")
        String name,

        @NotBlank @Schema(description = "학과", example = "CSE")
        Department department) {
    @Builder
    public ProfessorListResponseDto {
    }

    public static ProfessorListResponseDto of(Professor professor) {
        return ProfessorListResponseDto.builder()
                .id(professor.getId())
                .name(professor.getName())
                .department(professor.getDepartment())
                .build();
    }
}
