package inu.codin.codin.domain.post.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserInfoResponseDTO {
    private final boolean isLike;
    private final boolean isScrap;
    private final boolean isMine;

    @Builder
    private UserInfoResponseDTO(boolean isLike, boolean isScrap, boolean isMine) {
        this.isLike = isLike;
        this.isScrap = isScrap;
        this.isMine = isMine;
    }

    public static UserInfoResponseDTO of(boolean isLike, boolean isScrap, boolean isMine) {
        return UserInfoResponseDTO.builder()
                .isLike(isLike)
                .isScrap(isScrap)
                .isMine(isMine)
                .build();
    }
} 