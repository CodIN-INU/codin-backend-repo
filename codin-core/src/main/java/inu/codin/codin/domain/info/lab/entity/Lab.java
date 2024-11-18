package inu.codin.codin.domain.info.lab.entity;

import inu.codin.codin.common.Department;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lab")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Lab {

    @Id @NotBlank
    private String id;

    @NotBlank
    private Department department;

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

}
