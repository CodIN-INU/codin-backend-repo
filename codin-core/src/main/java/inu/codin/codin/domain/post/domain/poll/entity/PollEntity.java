package inu.codin.codin.domain.post.domain.poll.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "polls")
@Getter
public class PollEntity {
    @Id
    @NotBlank
    private ObjectId _id;

    private ObjectId postId; // PostEntity와의 관계를 유지하기 위한 필드
    private List<String> pollOptions = new ArrayList<>(); // 설문조사 선택지
    private List<Integer> pollVotes = new ArrayList<>(); // 선택지별 투표 수
    //Map 구조로 변환
    private LocalDateTime pollEndTime; // 설문조사 종료 시간
    private boolean multipleChoice; // 복수 선택 가능 여부

    @Builder
    public PollEntity(ObjectId _id, ObjectId postId, List<String> pollOptions,
                      LocalDateTime pollEndTime, boolean multipleChoice) {
        this._id = _id;
        this.postId = postId;
        this.pollOptions = pollOptions;
        this.pollVotes = pollOptions != null ? new ArrayList<>(pollOptions.stream().map(option -> 0).toList()) : new ArrayList<>();
        this.pollEndTime = pollEndTime;
        this.multipleChoice = multipleChoice;
    }


    //각 옵션의 투표 수 증가
    public void vote(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= pollOptions.size()) {
            throw new IllegalArgumentException("잘못된 선택지입니다.");
        }
        pollVotes.set(optionIndex, pollVotes.get(optionIndex) + 1);
    }
}
