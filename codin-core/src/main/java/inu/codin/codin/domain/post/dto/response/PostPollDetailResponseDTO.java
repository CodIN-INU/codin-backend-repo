package inu.codin.codin.domain.post.dto.response;

import inu.codin.codin.domain.post.entity.PostCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostPollDetailResponseDTO extends PostDetailResponseDTO {

    // Poll 관련 필드
    private List<String> pollOptions;
    private LocalDateTime pollEndTime;
    private boolean multipleChoice;
    private List<Integer> pollVotes; // 선택지별 투표 수
    private boolean isPollFinished;

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
            List<String> pollOptions,
            LocalDateTime pollEndTime,
            boolean multipleChoice,
            List<Integer> pollVotes,
            boolean isPollFinished
    ) {
        // 상위 클래스(PostDetailResponseDTO)의 생성자 호출
        super(userId, postId, title, content, nickname, PostCategory.POLL, null, isAnonymous, likeCount, scrapCount, hitsCount, createdAt, commentCount, userInfo);

        this.pollOptions = pollOptions;
        this.pollEndTime = pollEndTime;
        this.multipleChoice = multipleChoice;
        this.pollVotes = pollVotes;
        this.isPollFinished = isPollFinished;
    }
}