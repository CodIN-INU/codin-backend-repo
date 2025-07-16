package inu.codin.codin.domain.info.dto.response;

import inu.codin.codin.domain.info.entity.OfficeMember;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/*
    학과 사무실 직원 정보
 */
@Getter
public class OfficeMemberResponseDto {
    @NotBlank
    @Schema(description = "성명", example = "홍길동")
    private final String name;
    @NotBlank
    @Schema(description = "직위", example = "조교")
    private final String position;

    @Schema(description = "담당 업무", example = "학과사무실 업무")
    private final String role;

    @NotBlank
    @Schema(description = "연락처", example = "032-123-2345")
    private final String number;

    @NotBlank
    @Schema(description = "이메일", example = "test@inu.ac.kr")
    private final String email;

    @Builder
    public OfficeMemberResponseDto(String name, String position, String role, String number, String email) {
        this.name = name;
        this.position = position;
        this.role = role;
        this.number = number;
        this.email = email;
    }

    public static OfficeMemberResponseDto of(OfficeMember officeMember){
        return OfficeMemberResponseDto.builder()
                .name(officeMember.getName())
                .email(officeMember.getEmail())
                .position(officeMember.getPosition())
                .role(officeMember.getRole())
                .number(officeMember.getNumber())
                .email(officeMember.getEmail())
                .build();
    }
}
