package inu.codin.codin.domain.like.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LikeResponseType {
    ADD("추가"), RECOVER("복구"), REMOVE("삭제");

    private final String description;
}
