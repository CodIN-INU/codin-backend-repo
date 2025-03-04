package inu.codin.codin.domain.scrap.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "scraps")
@Getter
public class ScrapEntity extends BaseTimeEntity {
    @Id
    private ObjectId _id;
    private ObjectId postId;
    private ObjectId userId;

    @Builder
    public ScrapEntity(ObjectId postId, ObjectId userId) {
        this.postId = postId;
        this.userId = userId;
    }
}
