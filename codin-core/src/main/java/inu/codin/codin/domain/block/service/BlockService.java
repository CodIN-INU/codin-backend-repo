package inu.codin.codin.domain.block.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.block.entity.BlockEntity;
import inu.codin.codin.domain.block.exception.BlockErrorCode;
import inu.codin.codin.domain.block.exception.BlockException;
import inu.codin.codin.domain.block.exception.SelfBlockedException;
import inu.codin.codin.domain.block.repository.BlockRepository;
import inu.codin.codin.domain.user.service.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockService {
    private final BlockRepository blockRepository;
    private final UserValidator userValidator;

    /**
     * 유저 차단
     * SecurityContextHolder에서 현재 유저를 가져옴
     * @param strBlockedUserId
     */
    public void blockUser(String strBlockedUserId) {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        ObjectId blockedId = ObjectIdUtil.toObjectId(strBlockedUserId);

        if (userId.equals(blockedId)) {
            log.error("");
            throw new BlockException(BlockErrorCode.SELF_BLOCKED);
        }
        userValidator.validateUserExists(userId, () -> new BlockException(BlockErrorCode.BLOCKING_USER_NOT_FOUND));
        userValidator.validateUserExists(blockedId, () -> new BlockException(BlockErrorCode.BLOCKED_USER_NOT_FOUND));

        blockRepository.findByUserId(userId)
                .ifPresentOrElse(blockEntity -> {
                    if (BlockValidator.validateBlockedUserExists(blockEntity, blockedId)) {
                        throw new BlockException(BlockErrorCode.ALREADY_BLOCKED);
                    }
                    blockEntity.addBlockedUser(blockedId);
                    blockRepository.save(blockEntity);
                }, () -> blockRepository.save(BlockEntity.ofNew(userId)
                        .addBlockedUser(blockedId))
                );
    }

    /**
     * 유저 차단해제
     * SecurityContextHolder에서 현재 유저를 가져옴
     * @param strBlockedUserId 차단 해제할 유저
     */
    public void unblockUser(String strBlockedUserId) {
        ObjectId userId    = SecurityUtils.getCurrentUserId();
        ObjectId blockedId = ObjectIdUtil.toObjectId(strBlockedUserId);

        if (userId.equals(blockedId)) {
            throw new BlockException(BlockErrorCode.SELF_UNBLOCKED);
        }
        userValidator.validateUserExists(userId, () -> new BlockException(BlockErrorCode.BLOCKING_USER_NOT_FOUND));
        userValidator.validateUserExists(blockedId, () -> new BlockException(BlockErrorCode.BLOCKED_USER_NOT_FOUND));

        blockRepository.findByUserId(userId)
                .ifPresentOrElse(blockEntity -> {
                    if (!BlockValidator.validateBlockedUserExists(blockEntity, blockedId)) {
                        throw new BlockException(BlockErrorCode.BLOCKED_USER_NOT_FOUND);
                    }
                    blockEntity.removeBlockedUser(blockedId);
                    blockRepository.save(blockEntity);
                }, () -> {
                    throw new BlockException(BlockErrorCode.BLOCKED_USER_NOT_FOUND);
                });
    }

    /**
     * 현재 유저의 차단된 유저 목록 반환
     * @return 차단한 유저 목록 (빈 리스트가 제공될 수 있음)
     */
    public List<ObjectId> getBlockedUsers() {
        return blockRepository.findByUserId(SecurityUtils.getCurrentUserId())
                .map(BlockEntity::getBlockedUsers)
                .orElse(List.of());
    }
}
