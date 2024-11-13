package inu.codin.codin.domain.info.office.dto;

import inu.codin.codin.domain.info.office.entity.Office;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OfficeMemberResDTO {
    @Schema(description = "사무실 평면도", example = "https://")
    private String img;

    @Schema(description = "학과사무실 직원")
    private List<OfficeMember> member;

    @Builder
    public OfficeMemberResDTO(String img, List<OfficeMember> member) {
        this.img = img;
        this.member = member;
    }

    public static OfficeMemberResDTO of(Office office) {
        return OfficeMemberResDTO.builder()
                .img(office.getImg())
                .member(office.getMember())
                .build();
    }
}
