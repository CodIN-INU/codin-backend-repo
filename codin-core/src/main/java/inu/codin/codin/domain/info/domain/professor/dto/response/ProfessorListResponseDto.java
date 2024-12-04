package inu.codin.codin.domain.info.domain.professor.dto.response;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.domain.professor.entity.Professor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

/*
    교수님 리스트 반환 DTO
    모든 교수님들의 리스트를 반환한다.
 */

@Getter
@Setter
public class ProfessorListResponseDto{
    @NotBlank @Schema(description = "교수 _id", example = "67319fe3c4ee25b3adf593a0")
    String id;

    @NotBlank @Schema(description = "교수 성함", example = "홍길동")
    String name;

    @NotBlank @Schema(description = "학과", example = "CSE")
    Department department;

    @Builder
    public ProfessorListResponseDto(String id, String name, Department department) {
        this.id = id;
        this.name = name;
        this.department = department;
    }

    public static ProfessorListResponseDto of(Professor professor) {
        return ProfessorListResponseDto.builder()
                .id(professor.get_id().toString())
                .name(professor.getName())
                .department(professor.getDepartment())
                .build();
    }
}
