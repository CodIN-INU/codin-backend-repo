package inu.codin.codin.domain.info.dto.response;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.info.entity.Professor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/*
    교수님 상세 정보 반환 DTO
    해당되는 교수님의 상세 정보들을 반환한다.
 */

@Getter
public class ProfessorThumbnailResponseDto{

    @NotBlank @Schema(description = "학과", example = "CSE")
    private final Department department;
    @NotBlank @Schema(description = "성함", example = "홍길동")
    private final String name;
    @NotBlank @Schema(description = "프로필 사진", example = "https://~")
    private final String image;
    @NotBlank @Schema(description = "전화번호", example = "032-123-4567")
    private final String number;
    @NotBlank @Schema(description = "이메일", example = "test@inu.ac.kr")
    private final String email;
    @Schema(description = "연구실 홈페이지", example = "https://~")
    private final String site;
    @Schema(description = "연구 분야", example = "무선 통신 및 머신런닝")
    private final String field;
    @Schema(description = "담당 과목", example = "대학수학, 이동통신, ..")
    private final String subject;
    @Schema(description = "연구실 _id", example = "6731a506c4ee25b3adf593ca")
    private final String labId;

    @Builder
    public ProfessorThumbnailResponseDto(Department department, String name, String image, String number, String email, String site, String field, String subject, String labId) {
        this.department = department;
        this.name = name;
        this.image = image;
        this.number = number;
        this.email = email;
        this.site = site;
        this.field = field;
        this.subject = subject;
        this.labId = labId;
    }

    public static ProfessorThumbnailResponseDto of(Professor professor) {
        return ProfessorThumbnailResponseDto.builder()
                .department(professor.getDepartment())
                .name(professor.getName())
                .image(professor.getImage())
                .number(professor.getNumber())
                .email(professor.getEmail())
                .site(professor.getSite())
                .field(professor.getField())
                .subject(professor.getSubject())
                .labId(professor.getLabId())
                .build();
    }
}
