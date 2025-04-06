package inu.codin.codin.domain.block.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Document(collection = "blocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockEntity extends BaseTimeEntity {
    @Id
    private ObjectId id;  // MongoDB의 기본 ID

    @NotNull
    private ObjectId blockingUserId;  // 차단한 사용자

    @NotNull
    private ObjectId blockedUserId;   // 차단된 사용자

    @Builder
    public BlockEntity(ObjectId blockingUserId, ObjectId blockedUserId) {
        this.blockingUserId = blockingUserId;
        this.blockedUserId = blockedUserId;
    }
}
