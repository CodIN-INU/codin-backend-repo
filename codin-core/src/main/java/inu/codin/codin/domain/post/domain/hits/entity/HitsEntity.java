package inu.codin.codin.domain.post.domain.hits.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "hits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HitsEntity {

    @Id
    private ObjectId _id;

    private ObjectId userId;

    private ObjectId postId;

    @Builder
    public HitsEntity(ObjectId userId, ObjectId postId) {
        this.userId = userId;
        this.postId = postId;
    }
}
