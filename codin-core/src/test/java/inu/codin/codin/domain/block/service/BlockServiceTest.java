package inu.codin.codin.domain.block.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.block.entity.BlockEntity;
import inu.codin.codin.domain.block.exception.BlockErrorCode;
import inu.codin.codin.domain.block.exception.BlockException;
import inu.codin.codin.domain.block.repository.BlockRepository;
import inu.codin.codin.domain.user.service.UserValidator;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

    @InjectMocks
    BlockService blockService;

    @Mock
    BlockRepository blockRepository;
    @Mock
    UserValidator userValidator;

    private final ObjectId testUserId = ObjectIdUtil.toObjectId("686373fdaa87fd9618a63b49");
    private final ObjectId blockedUserId = ObjectIdUtil.toObjectId("6863740ceeb4a94ee959f592");

    @Test
    @DisplayName("유저 차단 성공")
    void blockUser_성공() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)){
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            BlockEntity existingBlock = BlockEntity.ofNew(testUserId);
            when(blockRepository.findByUserId(testUserId)).thenReturn(Optional.of(existingBlock));
            when(blockRepository.save(any(BlockEntity.class))).thenReturn(existingBlock);

            //when
            blockService.blockUser(blockedUserId.toString());

            //then
            verify(userValidator).validateUserExists(eq(testUserId), any());
            verify(userValidator).validateUserExists(eq(blockedUserId), any());
            verify(blockRepository).save(any(BlockEntity.class));
        }
    }

    @Test
    @DisplayName("유저 차단 성공 - 새로운 BlockEntity 생성")
    void blockUser_성공_새로운BlockEntity생성() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)) {
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            when(blockRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
            when(blockRepository.save(any(BlockEntity.class))).thenReturn(BlockEntity.ofNew(testUserId));

            //when
            blockService.blockUser(blockedUserId.toString());

            //then
            verify(blockRepository).save(any(BlockEntity.class));
        }
    }

    @Test
    @DisplayName("유저 차단 실패 - 자기 자신 차단")
    void blockUser_실패_자기자신차단() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)) {
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            //when & then
            assertThatThrownBy(() -> blockService.blockUser(testUserId.toString()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(BlockErrorCode.SELF_BLOCKED.message())
                    .hasFieldOrPropertyWithValue("errorCode", BlockErrorCode.SELF_BLOCKED);
        }
    }

    @Test
    @DisplayName("유저 차단 실패 - 차단유저 존재하지 않음")
    void blockUser_실패_차단유저X() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)) {
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            doNothing().when(userValidator).validateUserExists(eq(testUserId), any());
            doThrow(new BlockException(BlockErrorCode.BLOCKED_USER_NOT_FOUND))
                    .when(userValidator).validateUserExists(eq(blockedUserId), any());

            //when & then
            assertThatThrownBy(() -> blockService.blockUser(blockedUserId.toString()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(BlockErrorCode.BLOCKED_USER_NOT_FOUND.message())
                    .hasFieldOrPropertyWithValue("errorCode", BlockErrorCode.BLOCKED_USER_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("유저 차단 실패 - 차단 실행 유저 존재하지 않음")
    void blockUser_실패_차단실행유저X() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)) {
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            doThrow(new BlockException(BlockErrorCode.BLOCKING_USER_NOT_FOUND))
                    .when(userValidator).validateUserExists(eq(testUserId), any());

            //when & then
            assertThatThrownBy(() -> blockService.blockUser(blockedUserId.toString()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(BlockErrorCode.BLOCKING_USER_NOT_FOUND.message())
                    .hasFieldOrPropertyWithValue("errorCode", BlockErrorCode.BLOCKING_USER_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("유저 차단해제 성공")
    void unblockUser_성공() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)) {
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            BlockEntity existingBlock = BlockEntity.ofNew(testUserId);
            existingBlock.addBlockedUser(blockedUserId);
            when(blockRepository.findByUserId(testUserId)).thenReturn(Optional.of(existingBlock));
            when(blockRepository.save(any(BlockEntity.class))).thenReturn(existingBlock);

            //when
            blockService.unblockUser(blockedUserId.toString());

            //then
            verify(userValidator).validateUserExists(eq(testUserId), any());
            verify(userValidator).validateUserExists(eq(blockedUserId), any());
            verify(blockRepository).save(any(BlockEntity.class));
        }
    }

    @Test
    @DisplayName("유저 차단해제 실패 - 자기 자신 차단해제")
    void unblockUser_실패_자기자신차단해제() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)) {
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            //when & then
            assertThatThrownBy(() -> blockService.unblockUser(testUserId.toString()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(BlockErrorCode.SELF_UNBLOCKED.message())
                    .hasFieldOrPropertyWithValue("errorCode", BlockErrorCode.SELF_UNBLOCKED);
        }
    }

    @Test
    @DisplayName("유저 차단해제 실패 - 차단 정보 없음")
    void unblockUser_실패_차단정보없음() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)) {
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            when(blockRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> blockService.unblockUser(blockedUserId.toString()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(BlockErrorCode.BLOCKED_USER_NOT_FOUND.message())
                    .hasFieldOrPropertyWithValue("errorCode", BlockErrorCode.BLOCKED_USER_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("유저 차단 실패 - 이미 차단된 사용자")
    void blockUser_실패_이미차단된사용자() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)) {
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            BlockEntity existingBlock = BlockEntity.ofNew(testUserId);
            existingBlock.addBlockedUser(blockedUserId); // 이미 차단된 상태
            when(blockRepository.findByUserId(testUserId)).thenReturn(Optional.of(existingBlock));

            //when & then
            assertThatThrownBy(() -> blockService.blockUser(blockedUserId.toString()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(BlockErrorCode.ALREADY_BLOCKED.message())
                    .hasFieldOrPropertyWithValue("errorCode", BlockErrorCode.ALREADY_BLOCKED);

            // userValidator 호출 검증
            verify(userValidator).validateUserExists(eq(testUserId), any());
            verify(userValidator).validateUserExists(eq(blockedUserId), any());
            // save가 호출되지 않았는지 검증
            verify(blockRepository, never()).save(any(BlockEntity.class));
        }
    }

    @Test
    @DisplayName("유저 차단해제 실패 - 차단되지 않은 사용자")
    void unblockUser_실패_차단되지않은사용자() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)) {
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            // 다른 사용자는 차단되어 있지만, 요청한 사용자는 차단되지 않은 상태
            ObjectId otherBlockedUserId = ObjectIdUtil.toObjectId("507f1f77bcf86cd799439011");
            BlockEntity existingBlock = BlockEntity.ofNew(testUserId);
            existingBlock.addBlockedUser(otherBlockedUserId); // 다른 사용자만 차단됨
            when(blockRepository.findByUserId(testUserId)).thenReturn(Optional.of(existingBlock));

            //when & then
            assertThatThrownBy(() -> blockService.unblockUser(blockedUserId.toString()))
                    .isInstanceOf(BlockException.class)
                    .hasMessage(BlockErrorCode.BLOCKED_USER_NOT_FOUND.message())
                    .hasFieldOrPropertyWithValue("errorCode", BlockErrorCode.BLOCKED_USER_NOT_FOUND);

            // userValidator 호출 검증
            verify(userValidator).validateUserExists(eq(testUserId), any());
            verify(userValidator).validateUserExists(eq(blockedUserId), any());
            // save가 호출되지 않았는지 검증
            verify(blockRepository, never()).save(any(BlockEntity.class));
        }
    }

    @Test
    @DisplayName("차단된 유저 목록 조회 성공")
    void getBlockedUsers_성공() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)) {
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            BlockEntity blockEntity = spy(BlockEntity.ofNew(testUserId));
            when(blockEntity.getBlockedUsers()).thenReturn(List.of(blockedUserId));
            when(blockRepository.findByUserId(testUserId)).thenReturn(Optional.of(blockEntity));

            //when
            List<ObjectId> result = blockService.getBlockedUsers();

            //then
            assertThat(result).isNotNull()
                    .hasSize(1)
                    .doesNotContainNull()
                    .contains(blockedUserId);
            verify(blockRepository).findByUserId(testUserId);
        }
    }

    @Test
    @DisplayName("차단된 유저 목록 조회 - 차단 정보 없음")
    void getBlockedUsers_차단정보없음() {
        try (MockedStatic<SecurityUtils> mSecurityUtils = mockStatic(SecurityUtils.class)) {
            //given
            mSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            when(blockRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

            //when
            List<ObjectId> result = blockService.getBlockedUsers();

            //then
            assertThat(result).isEmpty();
            verify(blockRepository).findByUserId(testUserId);
        }
    }
}