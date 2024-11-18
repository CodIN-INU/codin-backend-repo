package inu.codin.codin.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    ADMIN("관리자"),
    MANAGER("교직원"),
    USER("사용자");

    private final String description;

}
