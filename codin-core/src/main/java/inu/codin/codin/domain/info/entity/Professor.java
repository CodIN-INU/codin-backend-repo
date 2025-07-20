package inu.codin.codin.domain.info.entity;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.info.dto.request.ProfessorCreateUpdateRequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Professor extends Info {

    @NotBlank
    private String name;

    @NotBlank
    private String image;

    @NotBlank
    private String number;

    @NotBlank
    private String email;

    private String site;

    @NotBlank
    private String field;

    private String subject;

    private String labId;

    @Builder
    public Professor(ObjectId id, Department department, InfoType infoType, String name, String image, String number, String email, String site, String field, String subject, String labId) {
        super(id, department, infoType);
        this.name = name;
        this.image = image;
        this.number = number;
        this.email = email;
        this.site = site;
        this.field = field;
        this.subject = subject;
        this.labId = labId;
    }

    public static Professor of(ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto) {
        return Professor.builder()
                .department(professorCreateUpdateRequestDto.getDepartment())
                .infoType(InfoType.PROFESSOR)
                .name(professorCreateUpdateRequestDto.getName())
                .image(professorCreateUpdateRequestDto.getImage())
                .number(professorCreateUpdateRequestDto.getNumber())
                .email(professorCreateUpdateRequestDto.getEmail())
                .site(professorCreateUpdateRequestDto.getSite())
                .field(professorCreateUpdateRequestDto.getField())
                .subject(professorCreateUpdateRequestDto.getSubject())
                .labId(professorCreateUpdateRequestDto.getLabId())
                .build();
    }

    public void update(ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto) {
        this.department = professorCreateUpdateRequestDto.getDepartment();
        this.infoType = InfoType.PROFESSOR;
        this.name = professorCreateUpdateRequestDto.getName();
        this.image = professorCreateUpdateRequestDto.getImage();
        this.number = professorCreateUpdateRequestDto.getNumber();
        this.email = professorCreateUpdateRequestDto.getEmail();
        this.site = professorCreateUpdateRequestDto.getSite();
        this.field = professorCreateUpdateRequestDto.getField();
        this.subject = professorCreateUpdateRequestDto.getSubject();
        this.labId = professorCreateUpdateRequestDto.getLabId();
    }
}
