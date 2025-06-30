package inu.codin.codin.domain.info.entity;

import inu.codin.codin.common.dto.Department;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Document(collection = "partner")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Partner {

    @Id @NotBlank
    private ObjectId _id;

    private String name;

    private List<Department> tags;

    private List<String> benefits;

    private LocalDate startDate;

    private LocalDate endDate;

    private PartnerImg img;
}
