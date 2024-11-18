package inu.codin.codin.domain.info.lab.dto;

import inu.codin.codin.domain.info.lab.entity.Lab;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/*
    연구실 리스트 반환 DTO
    존재하는 모든 연구실들을 반환한다.
 */

@Getter
@Setter
public class LabListResDTO {
    @Schema(description = "Lab DB의 pk값", example = "b2jfbe432..")
    @NotBlank
    private final String id;

    @Schema(description = "연구실 이름", example = "떙땡연구실")
    @NotBlank
    private final String title;

    @Schema(description = "연구 내용", example = "00을 연구합니다.")
    private final String content;

    @Schema(description = "담당 교수", example = "홍길동")
    @NotBlank
    private final String professor;

    @Builder
    public LabListResDTO(String id, String title, String content, String professor) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.professor = professor;
    }

    public static LabListResDTO of(Lab lab){
        return LabListResDTO.builder()
                .id(lab.getId())
                .title(lab.getTitle())
                .content(lab.getContent())
                .professor(lab.getProfessor())
                .build();
    }
}
