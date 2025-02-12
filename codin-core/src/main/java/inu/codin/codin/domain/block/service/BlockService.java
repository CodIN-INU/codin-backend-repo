package inu.codin.codin.domain.block.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.block.entity.BlockEntity;
import inu.codin.codin.domain.block.exception.AlreadyBlockedException;
import inu.codin.codin.domain.block.exception.NotBlockedException;
import inu.codin.codin.domain.block.repository.BlockRepository;
import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockService {
    private final BlockRepository blockRepository;
    private final UserRepository userRepository;

    public void blockUser(String strBlockedUserId) {
        ObjectId blockingUserId = SecurityUtils.getCurrentUserId();
        // 유저 엔티티 조회
        UserEntity user = userRepository.findById(blockingUserId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));


        ObjectId blockedUserId = new ObjectId(strBlockedUserId);

        // 중복 차단 방지
        checkUserBlocked(blockingUserId,blockedUserId);


        // 차단 정보 저
        BlockEntity block = BlockEntity.builder()
                .blockingUserId(blockingUserId)
                .blockedUserId(blockedUserId)
                .build();

        blockRepository.save(block);

        //유저 차단 리스트에 추가 ( 조회 필터링 목적)
        user.getBlockedUsers().add(blockedUserId);
        userRepository.save(user);
    }

    public void unblockUser(String strBlockedUserId) {
        ObjectId blockingUserId = SecurityUtils.getCurrentUserId();
        // 유저 엔티티 조회
        UserEntity user = userRepository.findById(blockingUserId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        ObjectId blockedUserId = new ObjectId(strBlockedUserId);
        // 차단 정보 조회
        BlockEntity block = blockRepository.findByBlockingUserIdAndBlockedUserId(blockingUserId, blockedUserId)
                .orElseThrow(() -> new NotBlockedException("차단되지 않은 사용자입니다."));

        // 차단 해제
        blockRepository.delete(block);


        //유저 차단 리스트에서 삭제 ( 조회 필터링 목적)
        user.getBlockedUsers().remove(blockedUserId);
        userRepository.save(user);
    }

    public void checkUserBlocked(ObjectId blockingUserId, ObjectId blockedUserId) {
        if (blockRepository.existsByBlockingUserIdAndBlockedUserId(blockingUserId, blockedUserId)) {
            throw new AlreadyBlockedException("이미 상대방을 차단했습니다.");
        }
    }

    public List<ObjectId> getBlockedUsers() {
        ObjectId userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .map(UserEntity::getBlockedUsers)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }
}
