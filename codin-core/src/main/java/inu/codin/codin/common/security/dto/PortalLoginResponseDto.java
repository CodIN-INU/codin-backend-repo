package inu.codin.codin.common.security.dto;

import inu.codin.codin.common.dto.Department;
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
    private String college;
    private Boolean undergraduate;
}
