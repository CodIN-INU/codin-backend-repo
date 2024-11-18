package inu.codin.codin.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    ACTIVE("활성"),
    DISABLED("비활성"),
    SUSPENDED("정지");

    private final String description;

}
