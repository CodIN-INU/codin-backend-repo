package inu.codin.codin.domain.info.office.dto;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.office.entity.Office;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfficeListResDTO{
    private final Department department;

    private final String location;

    private final String open;

    private final String vacation;

    private final String office_number;

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
