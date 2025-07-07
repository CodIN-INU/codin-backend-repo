package inu.codin.codinticketingapi.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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

    @JsonCreator
    public static Department fromDescription(String description) {
        for (Department department : Department.values()) {
            if (department.name().equals(description) || department.getDescription().equals(description)) {
                return department;
            }
        }
        return OTHERS;
    }

    @JsonValue
    public String toValue(){
        return this.name();
    }
}