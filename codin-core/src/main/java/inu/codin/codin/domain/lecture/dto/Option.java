package inu.codin.codin.domain.lecture.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Option {
    LEC("과목명"), PROF("교수명");

    private final String description;
}
