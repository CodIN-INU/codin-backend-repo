package inu.codin.codin.domain.info.dto.request;

import inu.codin.codin.domain.info.entity.Office;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
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
    private final String officeNumber;

    @Schema(description = "팩스", example = "032-432-1234")
    @NotBlank
    private final String fax;

    @Builder
    public OfficeUpdateRequestDto(String location, String open, String vacation, String officeNumber, String fax) {
        this.location = location;
        this.open = open;
        this.vacation = vacation;
        this.officeNumber = officeNumber;
        this.fax = fax;
    }



    public static OfficeUpdateRequestDto of(Office office){
        return OfficeUpdateRequestDto.builder()
                .location(office.getLocation())
                .open(office.getOpen())
                .vacation(office.getVacation())
                .officeNumber(office.getOfficeNumber())
                .fax(office.getFax())
                .build();
    }
}
