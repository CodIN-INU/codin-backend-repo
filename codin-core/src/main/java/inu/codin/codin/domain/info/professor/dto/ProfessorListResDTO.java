package inu.codin.codin.domain.info.professor.dto;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.professor.Professor;
import lombok.Builder;


public record ProfessorListResDTO(String id, String name, Department department) {
    @Builder
    public ProfessorListResDTO {
    }

    public static ProfessorListResDTO of(Professor professor) {
        return ProfessorListResDTO.builder()
                .id(professor.getId())
                .name(professor.getName())
                .department(professor.getDepartment())
                .build();
    }
}
