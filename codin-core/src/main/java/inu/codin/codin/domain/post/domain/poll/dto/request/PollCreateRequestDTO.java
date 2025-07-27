package inu.codin.codin.domain.post.domain.poll.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.domain.post.entity.PostCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
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
    @Size(min = 2)
    private List<String> pollOptions;

    @Schema(description = "복수 선택 가능 여부", example = "true")
    private boolean multipleChoice;

    @Schema(description = "설문조사 종료 시간 (yyyy/MM/dd HH:mm format)", example = "2024/01/21 23:59")
    @NotNull
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "Asia/Seoul")  // Jackson에서 LocalDateTime 변환을 위한 어노테이션
    private LocalDateTime pollEndTime;

    @Schema(description = "게시물 익명 여부 default = true (익명)", example = "true")
    @NotNull
    private boolean anonymous;

    @Schema(description = "게시물 종류", example = "POLL")
    @NotNull
    private PostCategory postCategory;
}
