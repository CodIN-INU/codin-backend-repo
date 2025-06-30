package inu.codin.codin.domain.info.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.info.dto.request.OfficeMemberCreateUpdateRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/*
    학과 사무실 직원 정보
 */
@Getter
public class OfficeMember extends BaseTimeEntity {
    @NotBlank
    @Schema(description = "성명", example = "홍길동")
    private String name;
    @NotBlank
    @Schema(description = "직위", example = "조교")
    private String position;

    @Schema(description = "담당 업무", example = "학과사무실 업무")
    private String role;

    @NotBlank
    @Schema(description = "연락처", example = "032-123-2345")
    private String number;

    @NotBlank
    @Schema(description = "이메일", example = "test@inu.ac.kr")
    private String email;

    @Builder
    public OfficeMember(String name, String position, String role, String number, String email) {
        this.name = name;
        this.position = position;
        this.role = role;
        this.number = number;
        this.email = email;
    }

    public static OfficeMember of(OfficeMemberCreateUpdateRequestDto officeMemberCreateUpdateRequestDto){
        return OfficeMember.builder()
                .name(officeMemberCreateUpdateRequestDto.getName())
                .position(officeMemberCreateUpdateRequestDto.getPosition())
                .role(officeMemberCreateUpdateRequestDto.getRole())
                .number(officeMemberCreateUpdateRequestDto.getNumber())
                .email(officeMemberCreateUpdateRequestDto.getEmail())
                .build();
    }

    public void update(OfficeMemberCreateUpdateRequestDto officeMemberCreateUpdateRequestDto) {
        this.name = officeMemberCreateUpdateRequestDto.getName();
        this.position = officeMemberCreateUpdateRequestDto.getPosition();
        this.role = officeMemberCreateUpdateRequestDto.getRole();
        this.number = officeMemberCreateUpdateRequestDto.getNumber();
        this.email = officeMemberCreateUpdateRequestDto.getEmail();
    }
}
