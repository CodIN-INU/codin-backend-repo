package inu.codin.codin.domain.scrap.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "scraps")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ScrapEntity extends BaseTimeEntity {
    @Id @NotNull
    private ObjectId _id;
    @NotNull
    private ObjectId postId;
    @NotNull
    private ObjectId userId;

    @Builder
    public ScrapEntity(ObjectId postId, ObjectId userId) {
        this.postId = postId;
        this.userId = userId;
    }
}
