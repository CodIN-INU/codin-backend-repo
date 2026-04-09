package inu.codin.codin.domain.admin.dto.req;

import inu.codin.common.entity.College;
import inu.codin.common.entity.Department;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SetManagerUserInfo(

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password,

        String studentId,

        @NotBlank
        String name,

        String nickname,

        String profileImageUrl,

        Department department,

        College college

) {
}