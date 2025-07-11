package inu.codin.codin.domain.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserInfo {
    private final boolean isLike;
    private final boolean isScrap;
    private final boolean isMine;

    @Builder
    private UserInfo(boolean isLike, boolean isScrap, boolean isMine) {
        this.isLike = isLike;
        this.isScrap = isScrap;
        this.isMine = isMine;
    }

    public static UserInfo of(boolean isLike, boolean isScrap, boolean isMine) {
        return UserInfo.builder()
                .isLike(isLike)
                .isScrap(isScrap)
                .isMine(isMine)
                .build();
    }
} 