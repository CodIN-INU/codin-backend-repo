package inu.codin.codin.domain.info.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InfoType {

    PROFESSOR("교수님"),
    LAB("연구실"),
    OFFICE("학과 사무실");

    private final String description;
}
