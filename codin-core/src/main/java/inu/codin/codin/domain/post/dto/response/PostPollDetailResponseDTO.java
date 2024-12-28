package inu.codin.codin.domain.post.dto.response;

import inu.codin.codin.domain.post.entity.PostCategory;
import inu.codin.codin.domain.post.entity.PostEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostPollDetailResponseDTO extends PostDetailResponseDTO {

    private PollInfo poll;

    public PostPollDetailResponseDTO(
            String userId,
            String postId,
            String title,
            String content,
            String nickname,
            boolean isAnonymous,
            int likeCount,
            int scrapCount,
            int hitsCount,
            LocalDateTime createdAt,
            int commentCount,
            UserInfo userInfo,
            PollInfo poll
    ) {
        super(userId, postId, title, content, nickname,
                PostCategory.POLL, null, isAnonymous,
                likeCount, scrapCount, hitsCount, createdAt, commentCount,
                userInfo);

        this.poll = poll;
    }

    public static PostPollDetailResponseDTO of(PostEntity post, String nickname,
                                               int likeCount, int scrapCount, int hitsCount,
                                               int commentCount, UserInfo userInfo,
                                               List<String> pollOptions, LocalDateTime pollEndTime,
                                               boolean multipleChoice, List<Integer> pollVotesCounts,
                                               boolean isPollFinished, List<Integer> userVotes,
                                               Long totalVotes) {

        PollInfo pollInfo = new PollInfo(pollOptions, pollEndTime, multipleChoice, pollVotesCounts, userVotes, totalVotes, isPollFinished);
        return new PostPollDetailResponseDTO(
                post.getUserId().toString(),
                post.get_id().toString(),
                post.getTitle(),
                post.getContent(),
                nickname,
                post.isAnonymous(),
                likeCount,
                scrapCount,
                hitsCount,
                post.getCreatedAt(),
                commentCount,
                userInfo,
                pollInfo
        );
    }

    @Getter
    public static class PollInfo {
        private List<String> pollOptions;
        private LocalDateTime pollEndTime;
        private boolean multipleChoice;
        private List<Integer> pollVotesCounts;
        private List<Integer> userVotesOptions;
        private Long totalVotes;
        private boolean isPollFinished;

        public PollInfo(List<String> pollOptions, LocalDateTime pollEndTime, boolean multipleChoice,
                        List<Integer> pollVotesCounts, List<Integer> userVotesOptions, Long totalVotes, boolean isPollFinished) {
            this.pollOptions = pollOptions;
            this.pollEndTime = pollEndTime;
            this.multipleChoice = multipleChoice;
            this.pollVotesCounts = pollVotesCounts;
            this.userVotesOptions = userVotesOptions;
            this.totalVotes = totalVotes;
            this.isPollFinished = isPollFinished;
        }
    }
}