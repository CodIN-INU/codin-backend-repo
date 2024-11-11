package inu.codin.codin.domain.info.professor;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.lab.Lab;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "professor")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Professor {
    @Id @NotBlank
    private String id;

    @NotBlank
    private Department department;

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

}
