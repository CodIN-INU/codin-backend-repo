package inu.codin.codin.domain.block.controller;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.block.exception.AlreadyBlockedException;
import inu.codin.codin.domain.block.exception.SelfBlockedException;
import inu.codin.codin.domain.block.service.BlockService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockControllerTest {

    @InjectMocks
    private BlockController blockController;

    @Mock
    private BlockService blockService;

    private final String testUserId = "686373fdaa87fd9618a63b49";
    private final String blockedUserId = "6863740ceeb4a94ee959f592";

    @Test
    @DisplayName("사용자 차단 성공")
    void blockUser_성공() {
        //given
        doNothing().when(blockService).blockUser(anyString());

        //when
        ResponseEntity<?> response = blockController.blockUser(blockedUserId);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(SingleResponse.class);
        
        SingleResponse<?> singleResponse = (SingleResponse<?>) response.getBody();
        Assertions.assertNotNull(singleResponse);
        assertThat(singleResponse.getCode()).isEqualTo(201);
        assertThat(singleResponse.getMessage()).isEqualTo("사용자 차단 완료");
        assertThat(singleResponse.getData()).isNull();

        verify(blockService).blockUser(blockedUserId);
    }

    @Test
    @DisplayName("사용자 차단 실패 - 자기 자신 차단")
    void blockUser_실패_자기자신차단() {
        //given
        doThrow(new SelfBlockedException("자신을 차단할 수 없습니다."))
                .when(blockService).blockUser(anyString());

        //when & then
        assertThatThrownBy(() -> blockController.blockUser(testUserId))
                .isInstanceOf(SelfBlockedException.class)
                .hasMessage("자신을 차단할 수 없습니다.");

        verify(blockService).blockUser(testUserId);
    }

    @Test
    @DisplayName("사용자 차단 실패 - 이미 차단된 사용자")
    void blockUser_실패_이미차단된사용자() {
        //given
        doThrow(new AlreadyBlockedException("이미 차단한 유저입니다."))
                .when(blockService).blockUser(anyString());

        //when & then
        assertThatThrownBy(() -> blockController.blockUser(blockedUserId))
                .isInstanceOf(AlreadyBlockedException.class)
                .hasMessage("이미 차단한 유저입니다.");

        verify(blockService).blockUser(blockedUserId);
    }

    @Test
    @DisplayName("사용자 차단 실패 - 차단할 사용자를 찾을 수 없음")
    void blockUser_실패_사용자없음() {
        //given
        doThrow(new NotFoundException("차단할 사용자를 찾을 수 없습니다."))
                .when(blockService).blockUser(anyString());

        //when & then
        assertThatThrownBy(() -> blockController.blockUser(blockedUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("차단할 사용자를 찾을 수 없습니다.");

        verify(blockService).blockUser(blockedUserId);
    }

    @Test
    @DisplayName("사용자 차단 해제 성공")
    void unblockUser_성공() {
        //given
        doNothing().when(blockService).unblockUser(anyString());

        //when
        ResponseEntity<?> response = blockController.unblockUser(blockedUserId);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(SingleResponse.class);
        
        SingleResponse<?> singleResponse = (SingleResponse<?>) response.getBody();
        assertThat(singleResponse.getCode()).isEqualTo(200);
        assertThat(singleResponse.getMessage()).isEqualTo("사용자 차단 해제 완료");
        assertThat(singleResponse.getData()).isNull();

        verify(blockService).unblockUser(blockedUserId);
    }

    @Test
    @DisplayName("사용자 차단 해제 실패 - 자기 자신 차단 해제")
    void unblockUser_실패_자기자신차단해제() {
        //given
        doThrow(new SelfBlockedException("자신을 차단 해제할 수 없습니다."))
                .when(blockService).unblockUser(anyString());

        //when & then
        assertThatThrownBy(() -> blockController.unblockUser(testUserId))
                .isInstanceOf(SelfBlockedException.class)
                .hasMessage("자신을 차단 해제할 수 없습니다.");

        verify(blockService).unblockUser(testUserId);
    }

    @Test
    @DisplayName("사용자 차단 해제 실패 - 차단 정보 없음")
    void unblockUser_실패_차단정보없음() {
        //given
        doThrow(new NotFoundException("차단 정보가 존재하지 않습니다."))
                .when(blockService).unblockUser(anyString());

        //when & then
        assertThatThrownBy(() -> blockController.unblockUser(blockedUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("차단 정보가 존재하지 않습니다.");

        verify(blockService).unblockUser(blockedUserId);
    }
}