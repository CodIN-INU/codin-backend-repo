package inu.codin.codin.domain.post.domain.poll.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "poll_votes")
@Getter
public class PollVoteEntity {
    @Id
    private ObjectId _id;

    @NotNull
    private ObjectId pollId;
    @NotNull
    private ObjectId userId;
    @NotNull
    private List<Integer> selectedOptions; // 선택한 옵션 인덱스

    @Builder
    public PollVoteEntity(ObjectId pollId, ObjectId userId, List<Integer> selectedOptions) {
        this.pollId = pollId;
        this.userId = userId;
        this.selectedOptions = selectedOptions;

    }

}
