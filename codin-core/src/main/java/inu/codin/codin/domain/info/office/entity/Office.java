package inu.codin.codin.domain.info.office.entity;

import inu.codin.codin.domain.info.office.dto.OfficeMember;
import inu.codin.codin.common.Department;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "department_office")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Office {
    @Id @NotBlank
    private String id;

    @NotBlank
    private Department department;

    @NotBlank
    private String location;

    @NotBlank
    private String open;

    @NotBlank
    private String vacation;

    private String img;

    private List<OfficeMember> member;

    @NotBlank
    private String office_number;

    @NotBlank
    private String fax;
}
