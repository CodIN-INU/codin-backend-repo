package inu.codin.codin.domain.info.dto.request;

import inu.codin.codin.common.dto.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ProfessorCreateUpdateRequestDto {

        @NotNull
        @Schema(description = "학과", example = "COMPUTER_SCI")
        private Department department;

        @NotBlank @Schema(description = "성함", example = "홍길동")
        private String name;

        @NotBlank @Schema(description = "프로필 사진", example = "https://~")
        private String image;

        @NotBlank @Schema(description = "전화번호", example = "032-123-4567")
        private String number;

        @NotBlank @Schema(description = "이메일", example = "test@inu.ac.kr")
        private String email;

        @Schema(description = "연구실 홈페이지", example = "https://~")
        private String site;

        @Schema(description = "연구 분야", example = "무선 통신 및 머신런닝")
        private String field;

        @Schema(description = "담당 과목", example = "대학수학, 이동통신, ..")
        private String subject;

        @Schema(description = "연구실 _id", example = "6731a506c4ee25b3adf593ca")
        private String labId;

        public ProfessorCreateUpdateRequestDto(Department department, String name, String image, String number, String email, String site, String field, String subject, String labId) {
                this.department = department;
                this.name = name;
                this.image = image;
                this.number = number;
                this.email = email;
                this.site = site;
                this.field = field;
                this.subject = subject;
                this.labId = labId;
        }
}
