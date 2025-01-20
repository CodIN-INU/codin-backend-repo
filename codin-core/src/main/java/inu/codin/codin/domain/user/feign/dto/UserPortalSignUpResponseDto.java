package inu.codin.codin.domain.user.feign.dto;

import inu.codin.codin.common.Department;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class UserPortalSignUpResponseDto {
    private String email;
    private String password;
    private String studentId;
    private String name;
    private Department department;
}
