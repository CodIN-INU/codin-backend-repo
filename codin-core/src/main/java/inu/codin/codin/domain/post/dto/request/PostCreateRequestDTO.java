package inu.codin.codin.domain.post.dto.request;

import inu.codin.codin.domain.post.domain.poll.dto.request.PollCreateRequestDTO;
import inu.codin.codin.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCreateRequestDTO {

    @Schema(description = "게시물 제목", example = "Example")
    @NotBlank
    private String title;

    @Schema(description = "게시물 내용", example = "example content")
    @NotBlank
    private String content;

    @Schema(description = "게시물 익명 여부 default = true (익명)", example = "true")
    @NotNull
    private boolean anonymous;

    @Schema(description = "게시물 종류", example = "REQUEST_STUDY")
    @NotNull
    private PostCategory postCategory;
    //STATUS 필드 - DEFAULT :: ACTIVE

    public PostCreateRequestDTO(String title, String content, boolean anonymous, PostCategory postCategory) {
        this.title = title;
        this.content = content;
        this.anonymous = anonymous;
        this.postCategory = postCategory;
    }

    public static PostCreateRequestDTO fromPoll(PollCreateRequestDTO pollCreateRequestDTO) {
        return new PostCreateRequestDTO(
                pollCreateRequestDTO.getTitle(),
                pollCreateRequestDTO.getContent(),
                pollCreateRequestDTO.isAnonymous(),
                PostCategory.POLL
        );
    }

}