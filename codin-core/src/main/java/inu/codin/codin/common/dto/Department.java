package inu.codin.codin.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@RequiredArgsConstructor
@Slf4j
public enum Department {

    IT_COLLEGE("정보기술대학", "정보대"),
    COMPUTER_SCI("컴퓨터공학부", "컴공"),
    COMPUTER_SCI_NIGHT("컴퓨터공학부(야)", "컴공"),
    INFO_COMM("정보통신공학과", "정통"),
    EMBEDDED("임베디드시스템공학과", "임베"),
    STAFF("교직원", "교직원"),
    OTHERS("타과대", "타대");

    private final String description;
    private final String abbreviation;

    @JsonCreator
    public static Department fromDescription(String description) {
        for (Department department : Department.values()) {
            if (department.name().equals(description) || department.getDescription().equals(description)) {
                return department;
            }
        }

        log.warn("정보대 내의 학과가 아닙니다. description : " + description);
        return OTHERS;
    }

    @JsonValue
    public String toValue(){
        return this.name();
    }
}
