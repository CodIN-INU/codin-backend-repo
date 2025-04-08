package inu.codin.codin.domain.like.dto.request;

import inu.codin.codin.domain.like.entity.LikeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LikeRequestDto {

    @NotNull
    @Schema(description = "좋아요를 반영할 entity 타입(POST, COMMENT, REPLY, REVIEW)", example = "POST")
    private LikeType likeType;

    @NotBlank
    @Schema(description = "좋아요를 반영할 entity 의 _id 값", example = "111111")
    private String id;
}
