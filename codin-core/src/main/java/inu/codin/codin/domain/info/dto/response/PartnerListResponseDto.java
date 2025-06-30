package inu.codin.codin.domain.info.dto.response;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.info.entity.Partner;
import lombok.Builder;

import java.util.List;

@Builder
public class PartnerListResponseDto {

    private String name;

    private String mainImg;

    private List<Department> tags;

    public static PartnerListResponseDto from(Partner partner) {
        return PartnerListResponseDto.builder()
                .name(partner.getName())
                .mainImg(partner.getImg().getMain())
                .tags(partner.getTags())
                .build();
    }
}
