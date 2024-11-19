package inu.codin.codin.domain.info.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.domain.professor.entity.Professor;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class Info extends BaseTimeEntity {

    @Id @NotBlank
    protected String id;

    @NotBlank
    protected Department department;

    @NotBlank
    protected InfoType infoType;

    public Info(String id, Department department, InfoType infoType) {
        this.id = id;
        this.department = department;
        this.infoType = infoType;
    }
}
