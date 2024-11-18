package inu.codin.codin.domain.info.office.dto;

import inu.codin.codin.domain.info.office.entity.Office;
import inu.codin.codin.domain.user.entity.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/*
    학과 사무실 리스트 DTO
    모든 학과 사무실의 리스트를 반환한다.
 */
@Getter
@Setter
public class OfficeListResDTO{

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
    private final String office_number;

    @Schema(description = "팩스", example = "032-432-1234")
    @NotBlank
    private final String fax;

    @Builder
    public OfficeListResDTO(Department department, String location, String open, String vacation, String office_number, String fax) {
        this.department = department;
        this.location = location;
        this.open = open;
        this.vacation = vacation;
        this.office_number = office_number;
        this.fax = fax;
    }

    public static OfficeListResDTO of(Office office){
        return OfficeListResDTO.builder()
                .department(office.getDepartment())
                .location(office.getLocation())
                .open(office.getOpen())
                .vacation(office.getVacation())
                .office_number(office.getOffice_number())
                .fax(office.getOffice_number())
                .build();
    }
}
