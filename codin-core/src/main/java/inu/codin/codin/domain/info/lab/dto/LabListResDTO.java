package inu.codin.codin.domain.info.lab.dto;

import inu.codin.codin.domain.info.lab.entity.Lab;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabListResDTO {
    private final String id;
    private final String title;
    private final String content;
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
