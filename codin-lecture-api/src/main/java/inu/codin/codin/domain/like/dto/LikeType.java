package inu.codin.codin.domain.like.dto;

import lombok.Getter;

@Getter
public enum LikeType {
    REVIEW("수강 후기"),
    LECTURE("과목");

    private final String description;

    LikeType(String description) {
        this.description = description;
    }
}
