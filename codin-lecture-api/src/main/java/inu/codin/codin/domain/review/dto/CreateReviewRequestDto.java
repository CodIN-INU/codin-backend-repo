package inu.codin.codin.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class CreateReviewRequestDto {

    @NotBlank
    @Schema(description = "수강 후기 내용", example = "완전 강추합니다!")
    private String content;

    @NotNull
    @Digits(integer = 1, fraction = 2)
    @Schema(description = "수강 평점 (0.25 ~ 5.0 사이의 값, 0.25 단위)")
    private double starRating;

    @NotBlank
    @Pattern(regexp = "^\\d{2}-[1-2]{1}$", message = "학기는 'YY-학기' 형식이어야 합니다. 예: 24-2")
    @Schema(description = "수강 학기 (년도-학기)", example = "24-2")
    private String semester;
}
