package inu.codin.codin.domain.info.dto.request;

import inu.codin.codin.common.dto.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabCreateUpdateRequestDto {
    @NotNull
    @Schema(description = "학과", example = "EMBEDDED")
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

}
