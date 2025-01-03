package inu.codin.codin.domain.post.domain.poll.controller;

import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.post.domain.poll.dto.PollCreateRequestDTO;
import inu.codin.codin.domain.post.domain.poll.dto.PollVotingRequestDTO;
import inu.codin.codin.domain.post.domain.poll.service.PollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/polls")
@Tag(name = "Poll API", description = "투표 API")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @Operation(summary = "투표 생성")
    @PostMapping
    public ResponseEntity<?> createPoll(
            @Valid @RequestBody PollCreateRequestDTO pollRequestDTO) {

        pollService.createPoll(pollRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(201, "투표 생성 완료", null));
    }

    @Operation(summary = "투표 실시")
    @PostMapping("/voting/{postId}")
    public ResponseEntity<?> votingPoll(
            @PathVariable String postId,
            @Valid @RequestBody PollVotingRequestDTO pollRequestDTO) {

        pollService.votingPoll(postId, pollRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SingleResponse<>(200, "투표 실시 완료", null));
    }
}