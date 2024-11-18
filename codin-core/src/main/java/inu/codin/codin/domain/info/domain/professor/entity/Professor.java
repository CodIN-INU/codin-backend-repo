package inu.codin.codin.domain.info.domain.professor.entity;

import inu.codin.codin.domain.info.domain.lab.entity.Lab;
import inu.codin.codin.domain.info.entity.Info;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    public void updateLab(Lab lab) {
        this.labId = lab.getId();
    }
}
