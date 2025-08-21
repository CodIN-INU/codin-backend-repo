package inu.codin.codin.domain.post.domain.poll.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.post.domain.poll.dto.PollCreateRequestDTO;
import inu.codin.codin.domain.post.domain.poll.exception.PollErrorCode;
import inu.codin.codin.domain.post.domain.poll.exception.PollException;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Document(collection = "polls")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PollEntity extends BaseTimeEntity {
    @Id
    private ObjectId _id;

    private ObjectId postId; // PostEntity와의 관계를 유지하기 위한 필드
    private List<String> pollOptions; // 설문조사 선택지
    private List<Integer> pollVotesCounts; // 선택지별 투표 수

    private LocalDateTime pollEndTime; // 설문조사 종료 시간
    private boolean multipleChoice; // 복수 선택 가능 여부


    public PollEntity(ObjectId postId, List<String> pollOptions,
                      LocalDateTime pollEndTime, boolean multipleChoice) {
        this.postId = postId;
        this.pollOptions = new ArrayList<>(pollOptions);
        this.pollVotesCounts = new ArrayList<>(Collections.nCopies(this.pollOptions.size(), 0));
        this.pollEndTime = pollEndTime;
        this.multipleChoice = multipleChoice;
    }

    public static PollEntity from(ObjectId postId, PollCreateRequestDTO dto) {
        return new PollEntity(
                postId,
                dto.getPollOptions(),
                dto.getPollEndTime(),
                dto.isMultipleChoice()
        );
    }
}
