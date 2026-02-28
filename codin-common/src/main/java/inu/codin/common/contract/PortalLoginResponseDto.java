package inu.codin.common.contract;

import inu.codin.common.entity.College;
import inu.codin.common.entity.Department;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class PortalLoginResponseDto {
    private String email;
    private String password;
    private String studentId;
    private String name;
    private Department department;
    private College college;
    private Boolean undergraduate;
}
