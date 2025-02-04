package inu.codin.codin.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostPollDetailResponseDTO extends PostDetailResponseDTO {

    private final PollInfo poll;

    public PostPollDetailResponseDTO(PostDetailResponseDTO baseDTO, PollInfo poll) {
        super(baseDTO.getUserId(), baseDTO.get_id(), baseDTO.getTitle(), baseDTO.getContent(), baseDTO.getNickname(),
                baseDTO.getPostCategory(), baseDTO.getUserImageUrl(), baseDTO.getPostImageUrl(), baseDTO.isAnonymous(), baseDTO.getLikeCount(),
                baseDTO.getScrapCount(), baseDTO.getHits(), baseDTO.getCreatedAt(), baseDTO.getCommentCount(), baseDTO.getReportCount(), baseDTO.getUserInfo());
        this.poll = poll;
    }

    public static PostPollDetailResponseDTO of(PostDetailResponseDTO base, PollInfo poll) {
        return new PostPollDetailResponseDTO(base, poll);
    }

    @Getter
    public static class PollInfo {
        //투표 선택지
        private final List<String> pollOptions;

        //투표 종료시간
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private final LocalDateTime pollEndTime;

        //복수 투표 여부
        private final boolean multipleChoice;
        //투표 항목별 총 카운트
        private final List<Integer> pollVotesCounts;
        //유저가 선택한 항목
        private final List<Integer> userVotesOptions;
        //투표 참여자 수
        private final Long totalParticipants;
        //유저 투표 실시 여부
        private final boolean hasUserVoted;
        //투표 종료 여부
        private final boolean pollFinished;

        public PollInfo(List<String> pollOptions, LocalDateTime pollEndTime, boolean multipleChoice,
                        List<Integer> pollVotesCounts, List<Integer> userVotesOptions, Long totalParticipants, boolean hasUserVoted ,boolean PollFinished) {
            this.pollOptions = pollOptions;
            this.pollEndTime = pollEndTime;
            this.multipleChoice = multipleChoice;
            this.pollVotesCounts = pollVotesCounts;
            this.userVotesOptions = userVotesOptions;
            this.totalParticipants = totalParticipants;
            this.hasUserVoted = hasUserVoted;
            this.pollFinished = PollFinished;
        }
        public static PollInfo of(List<String> pollOptions, LocalDateTime pollEndTime, boolean multipleChoice,
                                  List<Integer> pollVotesCounts, List<Integer> userVotesOptions,
                                  Long totalParticipants, boolean hasUserVoted , boolean pollFinished) {
            return new PollInfo(pollOptions, pollEndTime, multipleChoice, pollVotesCounts, userVotesOptions, totalParticipants,hasUserVoted ,pollFinished);
        }

    }
}