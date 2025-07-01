package inu.codin.codin.domain.info.entity;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.info.dto.request.PartnerCreateRequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

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
    private String location;
    private PartnerImg img;

    @Builder
    public Partner(String name, List<Department> tags, List<String> benefits, LocalDate startDate, LocalDate endDate, String location, PartnerImg img) {
        this.name = name;
        this.tags = tags;
        this.benefits = benefits;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.img = img;
    }

    public static Partner of(PartnerCreateRequestDto partnerCreateRequestDto, PartnerImg partnerImg){
        return Partner.builder()
                .name(partnerCreateRequestDto.getName())
                .tags(partnerCreateRequestDto.getTags())
                .benefits(partnerCreateRequestDto.getBenefits())
                .startDate(partnerCreateRequestDto.getStartDate())
                .endDate(partnerCreateRequestDto.getEndDate())
                .location(partnerCreateRequestDto.getLocation())
                .img(partnerImg)
                .build();
    }
}
