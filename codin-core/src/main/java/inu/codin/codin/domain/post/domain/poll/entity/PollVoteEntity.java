package inu.codin.codin.domain.post.domain.poll.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "poll_votes")
public class PollVoteEntity {
    @Id @NotNull
    private ObjectId _id;

    @NotNull
    private ObjectId pollId;
    @NotNull
    private ObjectId userId;
    @NotNull
    private List<Integer> selectedOptions; // 선택한 옵션 인덱스

    @Builder
    public PollVoteEntity(ObjectId _id, ObjectId pollId, ObjectId userId, List<Integer> selectedOptions) {
        this._id = _id;
        this.pollId = pollId;
        this.userId = userId;
        this.selectedOptions = selectedOptions;

    }

}
