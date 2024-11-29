package inu.codin.codin.domain.info.domain.professor.dto.request;

import inu.codin.codin.common.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfessorCreateUpdateRequestDto {

        @NotNull
        @Schema(description = "학과", example = "COMPUTER_SCI")
        Department department;

        @NotBlank @Schema(description = "성함", example = "홍길동")
        String name;

        @NotBlank @Schema(description = "프로필 사진", example = "https://~")
        String image;

        @NotBlank @Schema(description = "전화번호", example = "032-123-4567")
        String number;

        @NotBlank @Schema(description = "이메일", example = "test@inu.ac.kr")
        String email;

        @Schema(description = "연구실 홈페이지", example = "https://~")
        String site;

        @Schema(description = "연구 분야", example = "무선 통신 및 머신런닝")
        String field;

        @Schema(description = "담당 과목", example = "대학수학, 이동통신, ..")
        String subject;

        @Schema(description = "연구실 _id", example = "6731a506c4ee25b3adf593ca")
        String labId;
}
