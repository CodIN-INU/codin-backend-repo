package inu.codin.codin.domain.post.entity;

import lombok.Getter;

@Getter
public enum PostCategory {
    REQUEST("구해요"),
    COMMUNICATION("소통해요"),
    EXTRACURRICULAR("비교과"),
    INFO("정보대소개"),
    USED_BOOK("중고책");

    private final String description;

    PostCategory(String description) {
        this.description = description;
    }

}
