package inu.codin.codin.domain.info.domain.office.dto;

import inu.codin.codin.domain.info.domain.office.entity.Office;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
    학과 사무실 정보 반환 DTO
    학과 사무실 평면도 및 지원 정보들을 모두 반환한다.
 */
@Getter
@Setter
public class OfficeDetailsResponseDto {
    @Schema(description = "사무실 평면도", example = "https://")
    private String img;

    @Schema(description = "학과사무실 직원")
    private List<OfficeMemberResponseDto> officeMemberResponseDtos;

    @Builder
    public OfficeDetailsResponseDto(String img, List<OfficeMemberResponseDto> officeMemberResponseDtos) {
        this.img = img;
        this.officeMemberResponseDtos = officeMemberResponseDtos;
    }

    public static OfficeDetailsResponseDto of(Office office) {
        return OfficeDetailsResponseDto.builder()
                .img(office.getImg())
                .officeMemberResponseDtos(office.getMember().stream().map(OfficeMemberResponseDto::of).toList())
                .build();
    }
}
