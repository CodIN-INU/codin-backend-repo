package inu.codin.codin.domain.lecture.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Evaluation {
    ACCEPTANCE("이수인정"),
    RELATIVE("상대평가"),
    ABSOLUTE("절대평가");

    private final String description;

    @JsonValue
    public String getDescription(){
        return this.description;
    }

    @JsonCreator
    public static Evaluation fromDescription(String description) {
        if (description == null) {
            return null;
        }

        for (Evaluation evaluation : Evaluation.values()) {
            if (evaluation.getDescription().equals(description)) {
                return evaluation;
            }
        }

        try {
            return Evaluation.valueOf(description);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown evaluation: " + description);
        }
    }
}
