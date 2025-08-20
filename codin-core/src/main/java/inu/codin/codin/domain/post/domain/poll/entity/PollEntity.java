package inu.codin.codin.domain.post.domain.poll.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.post.domain.poll.exception.PollErrorCode;
import inu.codin.codin.domain.post.domain.poll.exception.PollException;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Document(collection = "polls")
@Getter
public class PollEntity extends BaseTimeEntity {
    @Id
    @NotBlank
    private ObjectId _id;

    private ObjectId postId; // PostEntity와의 관계를 유지하기 위한 필드
    private final List<String> pollOptions; // 설문조사 선택지
    private final List<Integer> pollVotesCounts; // 선택지별 투표 수

    private LocalDateTime pollEndTime; // 설문조사 종료 시간
    private boolean multipleChoice; // 복수 선택 가능 여부


    @Builder
    public PollEntity(ObjectId postId, List<String> pollOptions,
                      LocalDateTime pollEndTime, boolean multipleChoice) {
        this.postId = postId;
        this.pollOptions = new ArrayList<>(pollOptions);

        // pollVotesCounts가 null일 경우, pollOptions의 크기만큼 0으로 초기화
        this.pollVotesCounts = new ArrayList<>(Collections.nCopies(this.pollOptions.size(), 0));

        this.pollEndTime = pollEndTime;
        this.multipleChoice = multipleChoice;
    }


    //각 옵션의 투표 수 증가
    public void vote(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= this.pollOptions.size()) {
            throw new PollException(PollErrorCode.INVALID_OPTION);
        }
        this.pollVotesCounts.set(optionIndex, this.pollVotesCounts.get(optionIndex) + 1);
    }

    public void deleteVote(int optionIndex){
        if (this.pollVotesCounts.get(optionIndex) - 1 < 0)
            throw new PollException(PollErrorCode.INVALID_OPTION);
        this.pollVotesCounts.set(optionIndex, this.pollVotesCounts.get(optionIndex) - 1);
    }
}
