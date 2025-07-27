package inu.codin.codin.domain.lecture.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SortingOption {
    RATING("평점 높은 순"),
    LIKE("좋아요 많은 순"),
    HIT("조회수 순");

    private final String description;
}
