package inu.codin.codin.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Department {

    IT_COLLEGE("정보기술대학"),
    COMPUTER_SCI("컴퓨터공학과"),
    INFO_COMM("정보통신공학과"),
    EMBEDDED("임베디드시스템공학과"),
    STAFF("교직원");

    private final String description;

}
