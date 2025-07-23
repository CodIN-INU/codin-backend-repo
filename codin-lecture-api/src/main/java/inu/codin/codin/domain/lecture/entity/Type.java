package inu.codin.codin.domain.lecture.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Type {
    MAJOR_CORE("전공핵심"),
    MAJOR_DEEPENING("전공심화"),
    MAJOR_BASIC("전공기초"),
    BASIC_GENERAL("기초교양");

    private final String description;

    @JsonValue
    public String getDescription(){
        return description;
    }

    @JsonCreator
    public static Type fromDescription(String description) {
        for (Type type : Type.values()) {
            if (type.getDescription().equals(description)) {
                return type;
            }
        }
        return null;
    }
}
