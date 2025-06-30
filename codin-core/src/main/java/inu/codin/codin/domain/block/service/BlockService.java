package inu.codin.codin.domain.block.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.block.entity.BlockEntity;
import inu.codin.codin.domain.block.exception.SelfBlockedException;
import inu.codin.codin.domain.block.repository.BlockRepository;
import inu.codin.codin.domain.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
            throw new SelfBlockedException("자신을 차단할 수 없습니다.");
        }
        userValidator.validateUserExists(userId, "차단하는 사용자를 찾을 수 없습니다.");
        userValidator.validateUserExists(blockedId, "차단할 사용자를 찾을 수 없습니다.");

        blockRepository.save(blockRepository.findByUserId(userId)
                .orElseGet(() -> BlockEntity.ofNew(userId))
                .addBlockedUser(blockedId));
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
            throw new SelfBlockedException("자신을 차단 해제할 수 없습니다.");
        }
        userValidator.validateUserExists(userId, "차단 해제하는 사용자를 찾을 수 없습니다.");
        userValidator.validateUserExists(blockedId, "차단 해제할 사용자를 찾을 수 없습니다.");

        blockRepository.save(blockRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("차단 정보가 존재하지 않습니다."))
                .removeBockedUser(blockedId));
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
