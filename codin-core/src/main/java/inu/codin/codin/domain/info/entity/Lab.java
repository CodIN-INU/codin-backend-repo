package inu.codin.codin.domain.info.entity;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.info.dto.request.LabCreateUpdateRequestDto;
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
public class Lab extends Info {

    @NotBlank
    private String professor;

    @NotBlank
    private String title;

    private String content;

    private String professorLoc;

    private String professorNumber;

    private String labLoc;

    private String labNumber;

    private String site;

    @Builder
    public Lab(ObjectId id, Department department, InfoType infoType, String professor, String title, String content, String professorLoc, String professorNumber, String labLoc, String labNumber, String site) {
        super(id, department, infoType);
        this.professor = professor;
        this.title = title;
        this.content = content;
        this.professorLoc = professorLoc;
        this.professorNumber = professorNumber;
        this.labLoc = labLoc;
        this.labNumber = labNumber;
        this.site = site;
    }

    public static Lab of(LabCreateUpdateRequestDto labCreateUpdateRequestDto){
        return Lab.builder()
                .department(labCreateUpdateRequestDto.getDepartment())
                .infoType(InfoType.LAB)
                .professor(labCreateUpdateRequestDto.getProfessor())
                .title(labCreateUpdateRequestDto.getTitle())
                .content(labCreateUpdateRequestDto.getContent())
                .professorLoc(labCreateUpdateRequestDto.getProfessorLoc())
                .professorNumber(labCreateUpdateRequestDto.getProfessorNumber())
                .labLoc(labCreateUpdateRequestDto.getLabLoc())
                .labNumber(labCreateUpdateRequestDto.getLabNumber())
                .site(labCreateUpdateRequestDto.getSite())
                .build();

    }

    public void update(LabCreateUpdateRequestDto labCreateUpdateRequestDto){
        this.department = labCreateUpdateRequestDto.getDepartment();
        this.title = labCreateUpdateRequestDto.getTitle();
        this.content = labCreateUpdateRequestDto.getContent();
        this.professor = labCreateUpdateRequestDto.getProfessor();
        this.professorLoc = labCreateUpdateRequestDto.getProfessorLoc();
        this.professorNumber = labCreateUpdateRequestDto.getProfessorNumber();
        this.labLoc = labCreateUpdateRequestDto.getLabLoc();
        this.labNumber = labCreateUpdateRequestDto.getLabNumber();
        this.site = labCreateUpdateRequestDto.getSite();
    }
}
