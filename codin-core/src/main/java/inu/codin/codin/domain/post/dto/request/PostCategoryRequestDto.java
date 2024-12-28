package inu.codin.codin.domain.post.dto.request;

import inu.codin.codin.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostCategoryRequestDto {

    @NotNull
    @Schema(description = "게시글 카테고리", example = "REQEUST_STUDY")
    private PostCategory postCategory;
}
