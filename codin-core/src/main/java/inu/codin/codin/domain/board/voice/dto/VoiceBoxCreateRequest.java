package inu.codin.codin.domain.board.voice.dto;

import inu.codin.codin.common.dto.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class VoiceBoxCreateRequest {

    @Schema(description = "질문 학과", example = "COMPUTER_SCI")
    @NotNull(message = "학과 정보는 필수입니다.")
    private Department department;

    @Schema(description = "질문", example = "학회비 낸 사람은 얼마나 이득인가요?")
    @NotBlank(message = "질문 내용은 필수입니다.")
    private String question;

}
