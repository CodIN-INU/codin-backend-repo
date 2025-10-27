package inu.codin.codin.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PollInfoResponseDTO {
    private final List<String> pollOptions;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private final LocalDateTime pollEndTime;
    private final boolean multipleChoice;
    private final List<Integer> pollVotesCounts;
    private final List<Integer> userVotesOptions;
    private final Long totalParticipants;
    private final boolean hasUserVoted;
    private final boolean pollFinished;

    @Builder
    private PollInfoResponseDTO(List<String> pollOptions, LocalDateTime pollEndTime, boolean multipleChoice,
                                List<Integer> pollVotesCounts, List<Integer> userVotesOptions, Long totalParticipants, boolean hasUserVoted, boolean pollFinished) {
        this.pollOptions = pollOptions;
        this.pollEndTime = pollEndTime;
        this.multipleChoice = multipleChoice;
        this.pollVotesCounts = pollVotesCounts;
        this.userVotesOptions = userVotesOptions;
        this.totalParticipants = totalParticipants;
        this.hasUserVoted = hasUserVoted;
        this.pollFinished = pollFinished;
    }

    public static PollInfoResponseDTO of(List<String> pollOptions, LocalDateTime pollEndTime, boolean multipleChoice,
                                         List<Integer> pollVotesCounts, List<Integer> userVotesOptions,
                                         Long totalParticipants, boolean hasUserVoted , boolean pollFinished) {
        return PollInfoResponseDTO.builder()
                .pollOptions(pollOptions)
                .pollEndTime(pollEndTime)
                .multipleChoice(multipleChoice)
                .pollVotesCounts(pollVotesCounts)
                .userVotesOptions(userVotesOptions)
                .totalParticipants(totalParticipants)
                .hasUserVoted(hasUserVoted)
                .pollFinished(pollFinished)
                .build();
    }
} 