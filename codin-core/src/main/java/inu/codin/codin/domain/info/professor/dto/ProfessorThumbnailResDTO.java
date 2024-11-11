package inu.codin.codin.domain.info.professor.dto;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.professor.Professor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfessorThumbnailResDTO {

    private final Department department;

    private final String name;

    private final String image;

    private final String number;

    private final String email;

    private final String site;

    private final String field;

    private final String subject;


    @Builder
    public ProfessorThumbnailResDTO(Department department, String name, String image, String number, String email, String site, String field, String subject) {
        this.department = department;
        this.name = name;
        this.image = image;
        this.number = number;
        this.email = email;
        this.site = site;
        this.field = field;
        this.subject = subject;
    }

    public static ProfessorThumbnailResDTO of(Professor professor){
        return ProfessorThumbnailResDTO.builder()
                .department(professor.getDepartment())
                .name(professor.getName())
                .image(professor.getImage())
                .number(professor.getNumber())
                .email(professor.getEmail())
                .site(professor.getSite())
                .field(professor.getField())
                .subject(professor.getSubject())
                .build();
    }
}
