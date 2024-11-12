package inu.codin.codin.domain.info.lab.dto;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.lab.entity.Lab;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;


public record LabThumbnailResDTO(
        @NotBlank @Schema(description = "학과", example = "CSE")
        Department department,
        @NotBlank @Schema(description = "연구실 이름", example = "땡땡연구실")
        String title,
        @Schema(description = "연구 내용", example = "이것저것 연구합니다.")
        String content,
        @NotBlank @Schema(description = "담당 교수", example = "홍길동")
        String professor,
        @Schema(description = "교수실 위치", example = "7호관 423호")
        String professorLoc,
        @Schema(description = "교수실 전화번호", example = "032-123-4567")
        String professorNumber,
        @Schema(description = "연구실 위치", example = "7호관 409호")
        String labLoc,
        @Schema(description = "연구실 전화번호", example = "032-987-0653")
        String labNumber,
        @Schema(description = "연구실 홈페이지", example = "http://~")
        String site) {

    @Builder
    public LabThumbnailResDTO {
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
