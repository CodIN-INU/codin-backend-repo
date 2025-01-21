package inu.codin.codin.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Department {

    IT_COLLEGE("정보기술대학"),
    COMPUTER_SCI("컴퓨터공학부"),
    COMPUTER_SCI_NIGHT("컴퓨터공학부(야)"),
    INFO_COMM("정보통신공학과"),
    EMBEDDED("임베디드시스템공학과"),
    STAFF("교직원"),
    OTHERS("타과대");

    private final String description;

    public static Department fromDescription(String description) {
        for (Department department : Department.values()) {
            if (department.getDescription().equals(description)) {
                return department;
            }
        }
        return OTHERS;
    }

}
