package inu.codin.codin.domain.post.domain.best;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "bests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BestEntity {

    @Id @NotBlank
    private ObjectId _id;

    private ObjectId postId;

    private int score;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public BestEntity(ObjectId postId, int score) {
        this.postId = postId;
        this.score = score;
    }
}
