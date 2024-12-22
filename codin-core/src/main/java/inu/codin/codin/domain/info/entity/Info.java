package inu.codin.codin.domain.info.entity;

import inu.codin.codin.common.BaseTimeEntity;
import inu.codin.codin.common.Department;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class Info extends BaseTimeEntity {

    @Id @NotBlank
    protected ObjectId _id;

    @NotBlank
    protected Department department;

    @NotBlank
    protected InfoType infoType;

    public Info(ObjectId id, Department department, InfoType infoType) {
        this._id = id;
        this.department = department;
        this.infoType = infoType;
    }
}
