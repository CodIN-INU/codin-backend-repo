package inu.codin.codin.domain.info.domain.office.entity;

import inu.codin.codin.domain.info.domain.office.dto.OfficeUpdateRequestDto;
import inu.codin.codin.domain.info.entity.Info;
import inu.codin.codin.domain.info.domain.office.dto.OfficeMemberResponseDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Office extends Info {

    @NotBlank
    private String location;

    @NotBlank
    private String open;

    @NotBlank
    private String vacation;

    private String img;

    private List<OfficeMember> member;

    @NotBlank
    private String office_number;

    @NotBlank
    private String fax;

    public void update(OfficeUpdateRequestDto officeUpdateRequestDto) {
        this.location=officeUpdateRequestDto.getLocation();
        this.open=officeUpdateRequestDto.getOpen();
        this.vacation=officeUpdateRequestDto.getVacation();
//        this.img = officeUpdateRequestDto.get
        this.office_number = officeUpdateRequestDto.getOffice_number();
        this.fax = officeUpdateRequestDto.getFax();
    }

    public void addOfficeMember(OfficeMember officeMember){
        this.member.add(officeMember);
    }
}
