package inu.codin.codin.domain.info.lab.dto;

import inu.codin.codin.domain.info.lab.entity.Lab;
import inu.codin.codin.domain.user.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/*
    연구실 상세정보 반환 DTO
    해당하는 연구실에 대한 내용들을 모두 반환한다.
 */
@Data
public class LabThumbnailResDTO {

    @NotBlank
    @Schema(description = "학과", example = "CSE")
    private Department department;

    @NotBlank
    @Schema(description = "연구실 이름", example = "땡땡연구실")
    private String title;

    @Schema(description = "연구 내용", example = "이것저것 연구합니다.")
    private String content;

    @NotBlank
    @Schema(description = "담당 교수", example = "홍길동")
    private String professor;

    @Schema(description = "교수실 위치", example = "7호관 423호")
    private String professorLoc;

    @Schema(description = "교수실 전화번호", example = "032-123-4567")
    private String professorNumber;

    @Schema(description = "연구실 위치", example = "7호관 409호")
    private String labLoc;

    @Schema(description = "연구실 전화번호", example = "032-987-0653")
    private String labNumber;

    @Schema(description = "연구실 홈페이지", example = "http://~")
    private String site;

    @Builder
    public LabThumbnailResDTO(Department department, String title, String content, String professor,
                              String professorLoc, String professorNumber, String labLoc,
                              String labNumber, String site) {
        this.department = department;
        this.title = title;
        this.content = content;
        this.professor = professor;
        this.professorLoc = professorLoc;
        this.professorNumber = professorNumber;
        this.labLoc = labLoc;
        this.labNumber = labNumber;
        this.site = site;
    }

    public static LabThumbnailResDTO of(Lab lab) {
        return LabThumbnailResDTO.builder()
                .department(lab.getDepartment())
                .title(lab.getTitle())
                .content(lab.getContent())
                .professor(lab.getProfessor())
                .professorLoc(lab.getProfessorLoc())
                .professorNumber(lab.getProfessorNumber())
                .labLoc(lab.getLabLoc())
                .labNumber(lab.getLabNumber())
                .site(lab.getSite())
                .build();
    }
}
