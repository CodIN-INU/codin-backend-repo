package inu.codin.codin.domain.info.domain.office.dto;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.domain.office.entity.Office;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfficeUpdateRequestDto {

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
    public OfficeUpdateRequestDto(String location, String open, String vacation, String office_number, String fax) {
        this.location = location;
        this.open = open;
        this.vacation = vacation;
        this.office_number = office_number;
        this.fax = fax;
    }



    public static OfficeUpdateRequestDto of(Office office){
        return OfficeUpdateRequestDto.builder()
                .location(office.getLocation())
                .open(office.getOpen())
                .vacation(office.getVacation())
                .office_number(office.getOffice_number())
                .fax(office.getFax())
                .build();
    }
}
