package inu.codin.codin.domain.info.dto.response;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.info.entity.Office;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/*
    학과 사무실 정보 반환 DTO
    학과 사무실 평면도 및 지원 정보들을 모두 반환한다.
 */
@Getter
public class OfficeDetailsResponseDto {

    @Schema(description = "학과", example = "IT_COLLEGE")
    @NotBlank
    private final Department department;

    @Schema(description = "위치", example = "7호관 329호")
    @NotBlank
    private final String location;

    @Schema(description = "오픈 시간", example = "09-21시")
    private final String open;

    @Schema(description = "방학 오픈 시간", example = "09-17시")
    private final String vacation;

    @Schema(description = "연락처", example = "032-123-2434")
    @NotBlank
    private final String officeNumber;

    @Schema(description = "팩스", example = "032-432-1234")
    @NotBlank
    private final String fax;

    @Schema(description = "사무실 평면도", example = "https://")
    private final String img;

    @Schema(description = "학과사무실 직원")
    private final List<OfficeMemberResponseDto> officeMember;

    @Builder
    public OfficeDetailsResponseDto(Department department, String location, String open, String vacation, String officeNumber, String fax, String img, List<OfficeMemberResponseDto> officeMember) {
        this.department = department;
        this.location = location;
        this.open = open;
        this.vacation = vacation;
        this.officeNumber = officeNumber;
        this.fax = fax;
        this.img = img;
        this.officeMember = officeMember;
    }

    public static OfficeDetailsResponseDto of(Office office) {
        return OfficeDetailsResponseDto.builder()
                .department(office.getDepartment())
                .location(office.getLocation())
                .open(office.getOpen())
                .vacation(office.getVacation())
                .officeNumber(office.getOfficeNumber())
                .fax(office.getFax())
                .img(office.getImg())
                .officeMember(office.getMember().stream().map(OfficeMemberResponseDto::of).toList())
                .build();
    }
}
