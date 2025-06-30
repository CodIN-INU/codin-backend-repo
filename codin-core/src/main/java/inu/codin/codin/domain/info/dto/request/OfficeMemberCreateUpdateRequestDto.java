package inu.codin.codin.domain.info.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OfficeMemberCreateUpdateRequestDto {
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
}
