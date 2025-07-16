package inu.codin.codin.domain.post.domain.poll.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class PollVotingRequestDTO {

    @Schema(description = "사용자가 선택한 옵션 인덱스 리스트 (복수 투표 가능)", example = "[1, 2]")
    @NotNull
    private List<Integer> selectedOptions;
}
