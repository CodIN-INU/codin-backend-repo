package inu.codin.common.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum College {

    LAW("법학대학"),
    CONVERGENCE_LIBERAL_STUDIES("융합자유전공대학"),
    LIFE_SCIENCE_TECHNOLOGY("생명과학기술대학"),
    URBAN_SCIENCE("도시과학대학"),
    EDUCATION("사범대학"),
    ARTS_SPORTS("예술체육대학"),
    BUSINESS("경영대학"),
    INFORMATION_TECHNOLOGY("정보기술대학"),
    ENGINEERING("공과대학"),
    GLOBAL_PUBLIC_AFFAIRS_ECONOMICS("글로벌정경대학"),
    SOCIAL_SCIENCES("사회과학대학"),
    NATURAL_SCIENCES("자연과학대학"),
    HUMANITIES("인문대학"),
    NORTHEAST_ASIA_TRADE_LOGISTICS("동북아국제통상물류학부"),
    STUDENT_COUNCIL("총학생회"),
    OTHERS("타과대");

    private final String displayName;

    @JsonCreator
    public static College from(String value) {
        for (College c : values()) {
            if (c.name().equals(value) || c.getDisplayName().equals(value)) return c;
        }
        return OTHERS;
    }

    @JsonValue
    public String toValue() { return this.name(); }
}