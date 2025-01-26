package inu.codin.codin.domain.lecture.domain.review.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class CreateReviewRequestDto {

    @NotBlank
    private String content;

    @NotNull
    @Digits(integer = 1, fraction = 2)
    private double starRating;

    @NotBlank
    private String semester;
}
