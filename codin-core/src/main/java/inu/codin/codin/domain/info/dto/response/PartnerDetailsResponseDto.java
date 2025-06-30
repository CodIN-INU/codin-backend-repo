package inu.codin.codin.domain.info.dto.response;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.info.entity.Partner;
import inu.codin.codin.domain.info.entity.PartnerImg;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public class PartnerDetailsResponseDto {

    private String name;

    private List<Department> tags;

    private List<String> benefits;

    private LocalDate startDate;

    private LocalDate endDate;

    private PartnerImg img;

    public static PartnerDetailsResponseDto from(Partner partner){
        return PartnerDetailsResponseDto.builder()
                .name(partner.getName())
                .tags(partner.getTags())
                .benefits(partner.getBenefits())
                .startDate(partner.getStartDate())
                .endDate(partner.getEndDate())
                .img(partner.getImg())
                .build();
    }
}
