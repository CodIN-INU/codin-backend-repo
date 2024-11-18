package inu.codin.codin.domain.info.domain.lab.entity;

import inu.codin.codin.domain.info.entity.Info;
import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.entity.InfoType;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
}
