package inu.codin.codin.domain.post.domain.poll.dto;

import inu.codin.codin.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@NoArgsConstructor
public class PollCreateRequestDTO {

    @Schema(description = "투표 제목", example = "투표 제목")
    @NotBlank
    //투표 제목 = 게시글 제목
    private String title;

    @Schema(description = "투표 내용", example = "투표 내용")
    @NotBlank
    //투표 내용 = 게시글 내용
    private String content;

    @Schema(description = "투표 옵션 리스트", example = "[\"a\", \"b\", \"c\"]")
    @NotNull
    private List<@NotBlank String> pollOptions;

    @Schema(description = "복수 선택 가능 여부", example = "false")
    private boolean multipleChoice;

    @Schema(description = "설문조사 종료 시간 (ISO8601 format)", example = "2024-01-21T23:59:59")
    @NotNull
    private LocalDateTime pollEndTime;

    @Schema(description = "게시물 익명 여부 default = true (익명)", example = "true")
    @NotNull
    private boolean anonymous;

    @Schema(description = "게시물 종류", example = "POLL")
    @NotNull
    private PostCategory postCategory;
}
