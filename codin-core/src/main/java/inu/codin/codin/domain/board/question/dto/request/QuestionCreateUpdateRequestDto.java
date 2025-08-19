package inu.codin.codin.domain.board.question.dto.request;

import inu.codin.codin.common.dto.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class QuestionCreateUpdateRequestDto {

    @Schema(description = "질문 내용", example = "코딘에 들어가려면 어떻게 해야하나요?")
    @NotBlank
    private String question;

    @Schema(description = "답변 내용", example = "지원서를 작성하면 됩니다.")
    @NotBlank
    private String answer;

    @Schema(description = "답변 내용", example = "COMPUTER_SCI")
    @NotNull
    private Department department;
}
