package inu.codin.codinticketingapi.domain.ticketing.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Campus {

    SONGDO_CAMPUS("송도 캠퍼스"),
    MICHUHOL_CAMPUS("미추홀 캠퍼스");

    private final String description;

    @JsonCreator
    public static Campus fromDescription(String description) {
        for (Campus campus : values()) {
            if (campus.description.equals(description) || campus.name().equals(description)) {
                return campus;
            }
        }
        return null;
    }

    @JsonValue
    public String toValue(){
        return this.name();
    }
}
