package inu.codin.codin.domain.block.service;

import inu.codin.codin.domain.block.entity.BlockEntity;
import org.bson.types.ObjectId;

public class BlockValidator {

    public static boolean validateBlockedUserExists(BlockEntity blockEntity, ObjectId blockedUser) {
        return blockEntity.getBlockedUsers().contains(blockedUser);
    }
}
