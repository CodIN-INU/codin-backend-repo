package inu.codin.codin.domain.block.entity;

import inu.codin.codin.common.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Document(collection = "blocks")
public class BlockEntity extends BaseTimeEntity {
    @Id
    private ObjectId id;  // MongoDB의 기본 ID

    private ObjectId blockingUserId;  // 차단한 사용자
    private ObjectId blockedUserId;   // 차단된 사용자

    @Builder
    public BlockEntity(ObjectId blockingUserId, ObjectId blockedUserId) {
        this.blockingUserId = blockingUserId;
        this.blockedUserId = blockedUserId;
    }
}
