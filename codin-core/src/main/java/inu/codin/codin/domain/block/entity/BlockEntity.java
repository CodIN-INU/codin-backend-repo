package inu.codin.codin.domain.block.entity;

import inu.codin.codin.common.dto.BaseTimeEntity;
import inu.codin.codin.domain.block.exception.AlreadyBlockedException;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


@Getter
@Document(collection = "blocks")
public class BlockEntity extends BaseTimeEntity {

    @Id
    private ObjectId id;

    @Indexed
    private ObjectId userId;

    private List<ObjectId> blockedUsers = new ArrayList<>();

    @Builder
    public BlockEntity(ObjectId userId, ObjectId blockedUser) {
        this.userId = userId;
        this.blockedUsers.add(blockedUser);
    }

    public static BlockEntity ofNew(ObjectId userId) {
        return BlockEntity.builder()
                .userId(userId)
                .build();
    }

    public BlockEntity addBlockedUser(ObjectId blockedUser) {
        this.blockedUsers.add(blockedUser);
        return this;
    }

    public void removeBlockedUser(ObjectId blockedUser) {
        this.blockedUsers.remove(blockedUser);
    }
}